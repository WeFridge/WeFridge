package app.wefridge.wefridge.model

import android.util.Log
import app.wefridge.wefridge.*
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import kotlinx.android.parcel.RawValue
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*


class ItemController {

    companion object {
        private const val TAG = "ItemController"
        private val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)

        /*
        * The functions deleteItem, overrideItem and addItem are partially
        * based on code snippets provided by the Firebase Documentation:
        * https://firebase.google.com/docs/firestore/manage-data/add-data
        * */
        fun deleteItem(
            item: Item,
            callbackOnSuccess: () -> kotlin.Unit,
            callbackOnFailure: (Exception) -> kotlin.Unit
        ) {
            val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)

            if (item.firebaseId == null)
                return callbackOnFailure(Exception())

            itemsRef
                .document(item.firebaseId!!)
                .delete()
                .addOnSuccessListener { callbackOnSuccess() }
                .addOnFailureListener { exception -> callbackOnFailure(exception) }
        }

        fun saveItem(
            item: Item,
            callbackOnSuccess: () -> kotlin.Unit,
            callbackOnFailure: (Exception) -> kotlin.Unit
        ) {
            if (item.firebaseId != null) overrideItem(
                item,
                { callbackOnSuccess() },
                { exception -> callbackOnFailure(exception) })
            else addItem(
                item,
                { callbackOnSuccess() },
                { exception -> callbackOnFailure(exception) })
        }

        private fun overrideItem(
            item: Item,
            callbackOnSuccess: () -> kotlin.Unit,
            callbackOnFailure: (Exception) -> kotlin.Unit
        ) {
            if (item.firebaseId != null)
                itemsRef.document(item.firebaseId!!)
                    .set(item.getHashMap())
                    .addOnSuccessListener {
                        Log.d(TAG, "item successfully overridden!")
                        callbackOnSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error writing item to Firebase", exception)
                        callbackOnFailure(exception)
                    }
        }

        private fun addItem(
            item: Item,
            callbackOnSuccess: () -> kotlin.Unit,
            callbackOnFailure: (Exception) -> kotlin.Unit
        ) {
            if (item.firebaseId == null)
                itemsRef.add(item.getHashMap())
                    .addOnSuccessListener { itemDocument ->
                        Log.d(TAG, "item written to Firebase with id: ${itemDocument.id}")
                        item.firebaseId = itemDocument.id
                        callbackOnSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error adding item", exception)
                        callbackOnFailure(exception)
                    }
        }

        fun tryParse(item: DocumentSnapshot): Item? {
            return try {
                parse(item)
            } catch (exception: ItemOwnerMissingException) {
                null
            }
        }

        private fun parse(item: DocumentSnapshot): Item {
            with(item) {
                return Item(
                    id,
                    get(ITEM_NAME) as? String ?: "",
                    get(ITEM_DESCRIPTION) as? String,
                    get(ITEM_IS_SHARED) as? Boolean ?: false,
                    get(ITEM_QUANTITY) as? Long ?: 0,
                    Unit.getByValue((item.get(ITEM_UNIT) as? Long)?.toInt()) ?: Unit.PIECE,
                    (get(ITEM_BEST_BY) as? Timestamp)?.toDate(),
                    get(ITEM_LOCATION) as? @RawValue GeoPoint,
                    get(ITEM_GEOHASH) as? String,
                    get(ITEM_CONTACT_NAME) as? String,
                    get(ITEM_CONTACT_EMAIL) as? String,
                    get(ITEM_OWNER) as? @RawValue DocumentReference
                        ?: throw ItemOwnerMissingException("Cannot get DocumentReference from owner field.")
                )
            }
        }

        fun getItemsSnapshot(
            onSuccess: (ListenerRegistration, DocumentReference) -> kotlin.Unit,
            listener: (Item?, DocumentChange.Type, Int, Int) -> kotlin.Unit
        ) {
            UserController.getUser({ user ->
                val ownerRef = user.ownerReference ?: user.ref

                val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)
                val snapshotListener = itemsRef
                    .whereEqualTo(ITEM_OWNER, ownerRef)
                    .addSnapshotListener { snapshots, exception ->
                        if (exception != null) {
                            Log.e("ItemController", "Error in SnapshotListener: ", exception)
                            // TODO: add visual feedback or reload listener
                            // will throw PERMISSION_DENIED when kicked out of pantry
                            return@addSnapshotListener
                        }

                        if (snapshots == null)
                            return@addSnapshotListener

                        for (documentSnapshot in snapshots.documentChanges) {
                            with(documentSnapshot) {
                                val item = tryParse(document)
                                listener(item, type, oldIndex, newIndex)
                            }
                        }
                    }
                onSuccess(snapshotListener, ownerRef)
            }, {})
        }

        fun getNearbyItems(
            onSuccess: (MutableList<Item>) -> kotlin.Unit,
            onFailure: (Exception) -> kotlin.Unit,
            radius: Double,
            center: GeoLocation
        ) {
            UserController.getUser({ user ->
                val ownerRef = user.ownerReference ?: user.ref

                val actualRadius = if (radius == 0.0) 500.0 else radius

                val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)

                val bounds = GeoFireUtils.getGeoHashQueryBounds(center, actualRadius)
                val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
                for (b in bounds) {
                    val q: Query = itemsRef.whereEqualTo(ITEM_IS_SHARED, true)
                        .orderBy(ITEM_GEOHASH)
                        .startAt(b.startHash)
                        .endAt(b.endHash)
                    tasks.add(q.get())
                }
                Tasks.whenAllComplete(tasks)
                    .addOnCompleteListener {
                        val matchingDocs: MutableList<Item> = ArrayList()
                        for (task in tasks) {
                            val snap = task.result
                            for (doc in snap.documents) {
                                val item = parse(doc)

                                if (item.ownerReference == ownerRef)
                                    continue

                                val lat = item.location?.latitude ?: continue
                                val lng = item.location?.longitude ?: continue

                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                val docLocation = GeoLocation(lat, lng)
                                val distanceInM =
                                    GeoFireUtils.getDistanceBetween(docLocation, center)
                                if (distanceInM <= actualRadius) {
                                    item.distance = distanceInM
                                    matchingDocs.add(item)
                                }
                            }
                        }
                        matchingDocs.sortBy { it.distance }
                        onSuccess(matchingDocs)
                    }
                    .addOnFailureListener { onFailure(it) }

            }, onFailure)
        }
    }
}