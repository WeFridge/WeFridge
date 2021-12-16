package app.wefridge.wefridge

import app.wefridge.wefridge.application.model.Item
import app.wefridge.wefridge.application.model.Unit
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*


class ItemUnitTest {


    private lateinit var item: Item
    private lateinit var ownerRef: DocumentReference

    private val dummyFirebaseId = "ABC"
    private val dummyGeoPoint = GeoPoint(59.3294,18.0686)
    private val dummyHashMap = hashMapOf<String, Any>(
        ITEM_NAME to "Gouda Cheese",
        ITEM_DESCRIPTION to "So good!",
        ITEM_IS_SHARED to true,
        ITEM_QUANTITY to 10L,
        ITEM_UNIT to Unit.KILOGRAM,
        ITEM_BEST_BY to Date(),
        ITEM_LOCATION to dummyGeoPoint,
        ITEM_GEOHASH to GeoFireUtils.getGeoHashForLocation(GeoLocation(dummyGeoPoint.latitude, dummyGeoPoint.longitude)),
        ITEM_CONTACT_NAME to "Karen Anderson",
        ITEM_CONTACT_EMAIL to "karen@anderson.de"
    )

    @Before
    fun setUp() {
        // arrange
        // set up Item with ownerReference
        ownerRef = Mockito.mock(DocumentReference::class.java)

        item = Item(ownerReference = ownerRef)
    }

    @Test
    fun testPrimaryConstructor() {
        assertEquals(item.firebaseId, null)
        assertEquals(item.name, "")
        assertEquals(item.description, null)
        assertEquals(item.isShared, false)
        assertEquals(item.quantity, 0L)
        assertEquals(item.unit, Unit.PIECE)
        assertEquals(item.bestByDate, null)
        assertEquals(item.location, null)
        assertEquals(item.geohash, null)
        assertEquals(item.contactName, null)
        assertEquals(item.contactEmail, null)
        assertEquals(item.ownerReference, ownerRef)
    }

    @Test
    fun testGetHashMap() {

        // arrange
        item.firebaseId = dummyFirebaseId
        item.name = dummyHashMap[ITEM_NAME] as String
        item.description = dummyHashMap[ITEM_DESCRIPTION] as String
        item.isShared = dummyHashMap[ITEM_IS_SHARED] as Boolean
        item.quantity = dummyHashMap[ITEM_QUANTITY] as Long
        item.unit = dummyHashMap[ITEM_UNIT] as Unit
        item.bestByDate = dummyHashMap[ITEM_BEST_BY] as Date
        item.location = dummyHashMap[ITEM_LOCATION] as GeoPoint
        item.geohash = dummyHashMap[ITEM_GEOHASH] as String
        item.contactName = dummyHashMap[ITEM_CONTACT_NAME] as String
        item.contactEmail = dummyHashMap[ITEM_CONTACT_EMAIL] as String


        // act
        val itemAsHashMap = item.getHashMap()

        // assert
        assertEquals(item.firebaseId, dummyFirebaseId)
        assertEquals(item.name, itemAsHashMap[ITEM_NAME])
        assertEquals(item.description, itemAsHashMap[ITEM_DESCRIPTION])
        assertEquals(item.isShared, itemAsHashMap[ITEM_IS_SHARED])
        assertEquals(item.quantity, itemAsHashMap[ITEM_QUANTITY])
        assertEquals(item.unit.value, itemAsHashMap[ITEM_UNIT])
        assertEquals(Timestamp(item.bestByDate!!), itemAsHashMap[ITEM_BEST_BY])
        assertEquals(item.location, itemAsHashMap[ITEM_LOCATION])
        assertEquals(item.geohash, itemAsHashMap[ITEM_GEOHASH])
        assertEquals(item.contactName, itemAsHashMap[ITEM_CONTACT_NAME])
        assertEquals(item.contactEmail, itemAsHashMap[ITEM_CONTACT_EMAIL])
        assertEquals(item.ownerReference, ownerRef)
    }
}