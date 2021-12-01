package app.wefridge.wefridge.datamodel

import android.util.Log
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import app.wefridge.wefridge.placeholder.PlaceholderContent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import java.lang.Exception
import kotlin.collections.ArrayList

class ItemController: ItemControllerInterface {
    private val TAG = "ItemsOnFirebase"
    private val db = FirebaseFirestore.getInstance()
    private val items = ArrayList<Item>()

    /*
    * The function getItems is based on an example provided on
    * https://firebase.google.com/docs/firestore/query-data/get-data
    *
    * The functions deleteItem, overrideItem and addItem are partially
    * based on code snippets provided by the Firebase Documentation:
    * https://firebase.google.com/docs/firestore/manage-data/add-data
    * */
    override fun getItems(callbackOnSuccess: (ArrayList<Item>) -> kotlin.Unit, callbackOnFailure: (Exception) -> kotlin.Unit) {
        // TODO: only get items of specifc user and the groups participants
        val ownerController: OwnerControllerInterface = OwnerController()
        ownerController.getCurrentUser { owner ->
            db.collection("items")
                .whereEqualTo("owner", owner)
                .get()
                .addOnSuccessListener { itemDocuments ->
                    for (item in itemDocuments) {
                        try {
                            items.add(parse(item.data, item.id))
                        } catch (exc: ItemOwnerMissingException) {
                            // TODO: consider to remove this, because requests already filters by owner
                            item.data["owner"] = owner.id
                            items.add(parse(item.data, item.id))
                        }
                    }

                    callbackOnSuccess(items)
                }

                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents.", exception)
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
                    Log.d(TAG, "item successfully written!")
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
                    PlaceholderContent.items.add(item)
                    callbackOnSuccess()
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error adding item", exception)
                    callbackOnFailure(exception)
                }
    }

    companion object {

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
            val ownerDocumentReference = itemData.getOrDefault("owner", null) as? DocumentReference ?: throw ItemOwnerMissingException("Cannot get DocumentReference from owner field.")

            return Item(itemId, name, description, isShared, quantity, unit, bestByDate, location, geohash, contactName, contactEmail, ownerDocumentReference)
        }
    }
}