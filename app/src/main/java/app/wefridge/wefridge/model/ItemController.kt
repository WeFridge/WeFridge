package app.wefridge.wefridge.model

import android.util.Log
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.lang.Exception
import kotlin.collections.ArrayList

class ItemController: ItemControllerInterface {
    private val TAG = "ItemsOnFirebase"
    private val db = FirebaseFirestore.getInstance()


    /*
    * The function getItems is based on an example provided on
    * https://firebase.google.com/docs/firestore/query-data/get-data
    *
    * The functions deleteItem, overrideItem and addItem are partially
    * based on code snippets provided by the Firebase Documentation:
    * https://firebase.google.com/docs/firestore/manage-data/add-data
    * */
    override fun getItems(callbackOnSuccess: (MutableList<Item>) -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
        val ownerController: OwnerControllerInterface = OwnerController()
        ownerController.getCurrentUser { owner ->
            db.collection("items")
                .whereEqualTo("owner", owner)
                .get()
                .addOnSuccessListener { itemDocuments ->
                    items.clear()
                    for (itemDocument in itemDocuments) {
                        try {
                            items.add(parse(itemDocument.data, itemDocument.id))

                        } catch (exc: ItemOwnerMissingException) {
                            // TODO: consider to remove this, because requests already filters by owner
                            itemDocument.data["owner"] = owner.id
                            items.add(parse(itemDocument.data, itemDocument.id))
                        }
                    }
                    callbackOnSuccess(items)
                }

                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting items.", exception)
                    callbackOnFailure(exception)
                }
        }
    }

    override fun deleteItem(item: Item) {
        TODO("Not yet implemented")
    }

    override fun saveItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
        // TODO: insert condition: when isShared == true location coordinates have to be != null!!!
        if (item.firebaseId != null) overrideItem(item, { callbackOnSuccess() }, { exception -> callbackOnFailure(exception) })
        else addItem(item, { callbackOnSuccess() }, { exception -> callbackOnFailure(exception) })
    }

    private fun overrideItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
        if (item.firebaseId != null)
            db.collection("items").document(item.firebaseId!!)
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
            db.collection("items").add(item.getHashMap())
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

    companion object {
        var items: MutableList<Item> = ArrayList()
        private var onItemsChangedListeners: MutableList<OnItemsChangeListener> = ArrayList()
        private var snapshotListenerSetUp = false

        // TODO: set this function to private and adapt UnitTests appropriately
        fun parse(itemData: Map<String, Any>, itemId: String?): Item {
            val name = itemData.getOrDefault("name", null) as? String?
            val description = itemData.getOrDefault("description", null) as? String?
            val isShared = itemData.getOrDefault("is_shared", null) as? Boolean?
            val quantity = (itemData.getOrDefault("quantity", null) as? Long?)?.toInt()
            val unitNumber = (itemData.getOrDefault("unit", null) as? Long)?.toInt()
            val unit = Unit.getByValue(unitNumber)
            val bestByTimestamp = (itemData.getOrDefault("best_by", null) as? Timestamp?)
            val bestByDate = bestByTimestamp?.toDate()
            val location = itemData.getOrDefault("location", null) as? GeoPoint?
            val geohash = itemData.getOrDefault("geohash", null) as? String?
            val contactName = itemData.getOrDefault("contact_name", null) as? String?
            val contactEmail = itemData.getOrDefault("contact_email", null) as? String?
            val ownerDocumentReference = itemData.getOrDefault("owner", null) as? DocumentReference
                ?: throw ItemOwnerMissingException("Cannot get DocumentReference from owner field.")

            return Item(itemId, name, description, isShared, quantity, unit, bestByDate, location, geohash, contactName, contactEmail, ownerDocumentReference)
        }

        fun addOnItemChangedListener(listener: OnItemsChangeListener) {
            if (!snapshotListenerSetUp) {
                setUpSnapshotListener()
                snapshotListenerSetUp = true
            }
            if (!onItemsChangedListeners.contains(listener)) {
                onItemsChangedListeners.add(listener)
            }
        }

        fun deleteOnItemChangedListener(listener: OnItemsChangeListener) {
            onItemsChangedListeners.remove(listener)
        }

        private fun setUpSnapshotListener() {
            val db = FirebaseFirestore.getInstance()
            val ownerController: OwnerControllerInterface = OwnerController()
            ownerController.getCurrentUser { owner ->
                db.collection("items")
                    .whereEqualTo("owner", owner)
                    .addSnapshotListener { snapshots, exception ->
                        if (exception != null) {
                            Log.e("ItemController", "Error in SnapshotListener: ", exception)
                            return@addSnapshotListener
                        }

                        if (snapshots == null)
                            return@addSnapshotListener

                        for (documentSnapshot in snapshots.documentChanges) {
                            when (documentSnapshot.type) {
                                DocumentChange.Type.ADDED -> {
                                    items.add(
                                        documentSnapshot.newIndex,
                                        parse(
                                            documentSnapshot.document.data,
                                            documentSnapshot.document.id
                                        )
                                    )
                                    notifyOnItemChangedListeners(
                                        DocumentChange.Type.ADDED,
                                        documentSnapshot.newIndex
                                    )
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    val modifiedItem = parse(
                                        documentSnapshot.document.data,
                                        documentSnapshot.document.id
                                    )
                                    items[documentSnapshot.newIndex] = modifiedItem
                                    notifyOnItemChangedListeners(
                                        DocumentChange.Type.MODIFIED,
                                        documentSnapshot.newIndex
                                    )
                                }

                                DocumentChange.Type.REMOVED -> {
                                    items.removeAt(documentSnapshot.oldIndex)
                                    notifyOnItemChangedListeners(
                                        DocumentChange.Type.REMOVED,
                                        documentSnapshot.oldIndex
                                    )
                                }
                            }
                        }
                    }
            }
        }

        private fun notifyOnItemChangedListeners(type: DocumentChange.Type, atIndex: Int) {
            for (listener in onItemsChangedListeners) {
                listener.onItemChanged(type, atIndex)
            }
        }
    }
}