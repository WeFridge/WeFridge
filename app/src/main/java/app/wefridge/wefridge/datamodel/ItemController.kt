package app.wefridge.wefridge.datamodel

import android.util.Log
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import kotlin.collections.ArrayList

class ItemController: ItemControllerInterface {
    private val TAG = "ItemsOnFirebase"
    private val db = FirebaseFirestore.getInstance()
    private val items = ArrayList<Item>()
    private val OWNER = "users/MnYhb6LQbRjdLRjvnYqt"

    /*
    * The function getItems is based on an example provided on
    * https://firebase.google.com/docs/firestore/query-data/get-data
    *
    * The functions deleteItem, overrideItem and addItem are partially
    * based on code snippets provided by the Firebase Documentation:
    * https://firebase.google.com/docs/firestore/manage-data/add-data
    * */
    override fun getItems(): ArrayList<Item> {
        db.collection("items")
            .get()
            .addOnSuccessListener { itemDocuments ->
                for (item in itemDocuments) {
                    try {
                        items.add(parse(item.data, item.id))
                    } catch (exc: ItemOwnerMissingException) {
                        item.data["owner"] = OWNER
                        items.add(parse(item.data, item.id))
                    }
                }
            }

            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        return items
    }

    override fun deleteItem(item: Item) {
        TODO("Not yet implemented")
    }

    override fun saveItem(item: Item) {
        if (item.firebaseId != null) overrideItem(item)
        else addItem(item)
    }

    private fun overrideItem(item: Item) {
        TODO("Write tests for overrideItem")
        if (item.firebaseId != null)
            db.collection("items").document(item.firebaseId)
                .set(item)
                .addOnSuccessListener { Log.d(TAG, "item successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing item to Firebase", e) }
    }

    private fun addItem(item: Item) {
        TODO("Write tests for addItem")
        if (item.firebaseId == null)
            db.collection("items").add(item.getHashMap())
                .addOnSuccessListener { itemDocument ->
                    Log.d(TAG, "item written to Firebase with id: ${itemDocument.id}")
                }
                .addOnFailureListener({ exception ->
                    Log.d(TAG, "Error adding item", exception)
                })
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
            val sharedEmail = itemData.getOrDefault("shared_email", null) as? String?
            val bestByTimestamp = (itemData.getOrDefault("best_by", null) as? Timestamp?)
            val bestByDate = bestByTimestamp?.toDate()
            val location = itemData.getOrDefault("location", null) as? GeoPoint?
            val geohash = itemData.getOrDefault("geohash", null) as? String?
            val contactName = itemData.getOrDefault("contact_name", null) as? String?
            val contactEmail = itemData.getOrDefault("contact_email", null) as? String?
            val ownerDocumentReference = itemData.getOrDefault("owner", null) as? DocumentReference ?: throw ItemOwnerMissingException("Cannot get DocumentReference from owner field.")

            return Item(itemId, name, description, isShared, quantity, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerDocumentReference)
        }
    }
}