package app.wefridge.wefridge.model

import android.util.Log
import app.wefridge.wefridge.*
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.google.firebase.firestore.*

class ItemController {
    private val TAG = "ItemsOnFirebase"
    private val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)


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

    fun deleteItem(item: Item) {
        TODO("Not yet implemented")
    }

    fun saveItem(item: Item, callbackOnSuccess: () -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
        // TODO: insert condition: when isShared == true location coordinates have to be != null!!!
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

    companion object {
        var items: MutableList<Item> = ArrayList()
        private var onItemsChangedListeners: MutableList<OnItemsChangeListener> = ArrayList()
        private var snapshotListenerSetUp = false

        fun tryParse(item: DocumentSnapshot): Item? {
            return try {
                parse(item)
            } catch (exception: ItemOwnerMissingException) {
                return null
            }
        }

        // TODO: set this function to private and adapt UnitTests appropriately
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
                    getDocumentReference(ITEM_OWNER) ?: throw ItemOwnerMissingException("Cannot get DocumentReference from owner field."))
            }
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
            val itemsRef = FirebaseFirestore.getInstance().collection(ITEMS_COLLECTION_NAME)
            val ownerRef = UserController.getCurrentUserRef()
            itemsRef
                .whereEqualTo(ITEM_OWNER, ownerRef)
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
                                val addedItem = tryParse(documentSnapshot.document)
                                if (addedItem != null) {
                                    items.add(documentSnapshot.newIndex, addedItem)

                                    notifyOnItemChangedListeners(
                                        DocumentChange.Type.ADDED,
                                        documentSnapshot.newIndex
                                    )
                                }
                            }

                            DocumentChange.Type.MODIFIED -> {
                                val modifiedItem = tryParse(documentSnapshot.document)
                                if (modifiedItem != null) {
                                    items[documentSnapshot.newIndex] = modifiedItem
                                    notifyOnItemChangedListeners(
                                        DocumentChange.Type.MODIFIED,
                                        documentSnapshot.newIndex
                                    )
                                }

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

        private fun notifyOnItemChangedListeners(type: DocumentChange.Type, atIndex: Int) {
            for (listener in onItemsChangedListeners) {
                listener.onItemChanged(type, atIndex)
            }
        }
    }
}