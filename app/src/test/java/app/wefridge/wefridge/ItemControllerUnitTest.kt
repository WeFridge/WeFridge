package app.wefridge.wefridge

import app.wefridge.wefridge.datamodel.Item
import app.wefridge.wefridge.datamodel.ItemController
import app.wefridge.wefridge.datamodel.Unit
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.*
import org.junit.BeforeClass
import java.util.Date
import org.junit.Test

class ItemControllerUnitTest {

    // TODO: fix this unit tests (problem with FirebaseFirestore.getInstance() because it's asynchronous)


    companion object {
        private val firebaseId = "ABC"
        private val name = "Gouda Cheese"
        private val description = "So good!"
        private val isShared = false
        private val quantity: Long = 0
        private val unit = Unit.KILOGRAM
        private val unitValue: Long = unit.value.toLong()
        private val sharedEmail = "karen@gmail.com"
        private val bestByDate = Date()
        private val location = GeoPoint(59.3294,18.0686)
        private val geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude))
        private val contactName = "Karen Anderson"
        private val contactEmail = "karen@anderson.de"
        private val ownerID = "MnYhb6LQbRjdLRjvnYqt"

        private lateinit var ownerRef: DocumentReference
        private lateinit var itemData: Map<String, Any>

        @BeforeClass @JvmStatic
        fun setUpItemData() {
            val documentSnapshotTask: Task<DocumentSnapshot> =
                FirebaseFirestore.getInstance().document("users/${ownerID}").get()
            val ownerDocumentSnapshot: DocumentSnapshot = Tasks.await(documentSnapshotTask)
            ownerRef = ownerDocumentSnapshot.reference

            itemData = mapOf<String, Any>(
                "name" to name,
                "description" to description,
                "is_shared" to isShared,
                "quantity" to quantity,
                "unit" to unitValue,
                "shared_email" to sharedEmail,
                "best_by" to Timestamp(bestByDate),
                "location" to location,
                "geohash" to geohash,
                "contact_name" to contactName,
                "contact_email" to contactEmail,
                "owner" to ownerRef
            )
        }
    }

    @Test
    fun testParsingWithAllFieldsExisting() {
        val parsedItem = ItemController.parse(itemData, firebaseId)

        val expectedItem = Item(firebaseId, name, description, isShared, quantity.toInt(), unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(expectedItem, parsedItem)
    }

    @Test
    fun testParsingWithoutFirebaseId() {
        val parsedItem = ItemController.parse(itemData, null)
        val expectedItem = Item(null, name, description, isShared, quantity.toInt(), unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(expectedItem, parsedItem)
    }

    @Test
    fun testParsingWithoutName() {
        val modifiedItemData = mapWithNullValue(itemData, "name")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, null, description, isShared, quantity.toInt(), unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutDescription() {
        val modifiedItemData = mapWithNullValue(itemData, "description")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, null, isShared, quantity.toInt(), unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutIsShared() {
        val modifiedItemData = mapWithNullValue(itemData, "is_shared")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, null, quantity.toInt(), unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutQuantity() {
        val modifiedItemData = mapWithNullValue(itemData, "quantity")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, null, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutUnit() {
        val modifiedItemData = mapWithNullValue(itemData, "unit")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, quantity.toInt(), null, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutSharedEmail() {
        val modifiedItemData = mapWithNullValue(itemData, "shared_email")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, quantity.toInt(), unit, null, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutDate() {
        val modifiedItemData = mapWithNullValue(itemData, "best_by")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, quantity.toInt(), unit, sharedEmail, null, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(itemExpected, parsedItem)
    }

    @Test(expected = ItemOwnerMissingException::class)
    fun testParsingWithoutOwner() {
        val modifiedItemData = mapWithNullValue(itemData, "owner")
        ItemController.parse(modifiedItemData, firebaseId)
    }

    private fun mapWithNullValue(mutableMap: Map<String, Any>, field: String): MutableMap<String, Any> {
        var modifiedHashMap = HashMap<String, Any?>()
        for ((key, value) in mutableMap) {
            if (key == field) modifiedHashMap[key] = null
            else modifiedHashMap[key] = value
        }
        return modifiedHashMap as MutableMap<String, Any>
    }
}