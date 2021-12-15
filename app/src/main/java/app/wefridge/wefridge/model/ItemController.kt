package app.wefridge.wefridge.model

import android.util.Log
import app.wefridge.wefridge.*
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class ItemController {

    companion object {
        private const val TAG = "ItemController"
        private val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)
        private var items: MutableList<Item> = ArrayList()


        /*
    * The function getItems is based on an example provided on
    * https://firebase.google.com/docs/firestore/query-data/get-data
    *
    * The functions deleteItem, overrideItem and addItem are partially
    * based on code snippets provided by the Firebase Documentation:
    * https://firebase.google.com/docs/firestore/manage-data/add-data
    * */
    fun getItems(callbackOnSuccess: (MutableList<Item>) -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
        val ownerRef = UserController.getCurrentUserRef()
        itemsRef
            .whereEqualTo(ITEM_OWNER, ownerRef)
            .get()
            .addOnSuccessListener { itemDocuments ->
                items.clear()
                for (itemDocument in itemDocuments) {
                    val item = tryParse(itemDocument)
                    if (item != null) items.add(item)
                }
                callbackOnSuccess(items)
            }

            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting items.", exception)
                callbackOnFailure(exception)
            }
        }

        fun deleteItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
            val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)

            if (item.firebaseId == null)
                return callbackOnFailure(Exception())

            itemsRef
                .document(item.firebaseId!!)
                .delete()
                .addOnSuccessListener { callbackOnSuccess() }
                .addOnFailureListener { exception -> callbackOnFailure(exception) }
        }

        fun saveItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
            if (item.firebaseId != null) overrideItem(item, { callbackOnSuccess() }, { exception -> callbackOnFailure(exception) })
            else addItem(item, { callbackOnSuccess() }, { exception -> callbackOnFailure(exception) })
        }

        private fun overrideItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
            if (item.firebaseId != null)
                itemsRef.document(item.firebaseId!!)
                    .set(item.getHashMap())
                    .addOnSuccessListener {
                        Log.d(TAG, "item successfully overridden!")
                        val itemIndex = items.indexOf(item)
                        if (itemIndex != -1) items[itemIndex] = item
                        else items.add(item)
                        callbackOnSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error writing item to Firebase", exception)
                        callbackOnFailure(exception)
                    }
        }

        private fun addItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
            if (item.firebaseId == null)
                itemsRef.add(item.getHashMap())
                    .addOnSuccessListener { itemDocument ->
                        Log.d(TAG, "item written to Firebase with id: ${itemDocument.id}")
                        item.firebaseId = itemDocument.id
                        items.add(item)
                        callbackOnSuccess()
                    }
                    .addOnFailureListener { exception ->
                        Log.d(TAG, "Error adding item", exception)
                        callbackOnFailure(exception)
                    }
        }

        // TODO: adapt UnitTests appropriately
        fun tryParse(item: DocumentSnapshot): Item? {
            return try {
                parse(item)
            } catch (exception: ItemOwnerMissingException) {
                null
            }
        }

        private fun parse(item: DocumentSnapshot): Item {
            with(item) {
                return Item(id,
                    getString(ITEM_NAME) ?: "",
                    getString(ITEM_DESCRIPTION),
                    getBoolean(ITEM_IS_SHARED) ?: false,
                    getLong(ITEM_QUANTITY) ?: 0,
                    Unit.getByValue(getLong(ITEM_UNIT)?.toInt()) ?: Unit.PIECE,
                    getTimestamp(ITEM_BEST_BY)?.toDate(),
                    getGeoPoint(ITEM_LOCATION),
                    getString(ITEM_GEOHASH),
                    getString(ITEM_CONTACT_NAME),
                    getString(ITEM_CONTACT_EMAIL),
                    getDocumentReference(ITEM_OWNER)
                        ?: throw ItemOwnerMissingException("Cannot get DocumentReference from owner field.")
                )
            }
        }

        fun getItemsSnapshot(
            onSuccess: (ListenerRegistration) -> kotlin.Unit,
            listener: (Item, DocumentChange.Type, Int, Int) -> kotlin.Unit
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
                                val item = parse(document)
                                listener(item, type, oldIndex, newIndex)
                            }
                        }
                    }
                onSuccess(snapshotListener)
            }, {})
        }
    }
}