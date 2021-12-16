package app.wefridge.wefridge

import app.wefridge.wefridge.application.model.Item
import app.wefridge.wefridge.application.model.ItemController
import app.wefridge.wefridge.application.model.Unit
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.util.*


class ItemControllerUnitTest {

    // TODO: consider to delete getItems in ItemController

    // Mock objects have the same methods as the original class
    private lateinit var itemDocSnap: DocumentSnapshot
    private val ownerDocRef = Mockito.mock(DocumentReference::class.java)

    // dummy data
    private val dummyUserName = "Karen Anderson"
    private val dummyUserEmail = "karen@anderson.de"
    private val dummyFirebaseId = "demo_id"
    private var dummyGeoPoint = GeoPoint(59.3294,18.0686)
    private val dummyValues = mapOf<String, Any?>(
        ITEM_OWNER to ownerDocRef,
        ITEM_NAME to "Gouda Cheese",
        ITEM_DESCRIPTION to "So good!",
        ITEM_IS_SHARED to true,
        ITEM_QUANTITY to 10L,
        ITEM_UNIT to Unit.KILOGRAM.value.toLong(),
        ITEM_BEST_BY to Timestamp(Date()),
        ITEM_LOCATION to dummyGeoPoint,
        ITEM_GEOHASH to GeoFireUtils.getGeoHashForLocation(GeoLocation(dummyGeoPoint.latitude, dummyGeoPoint.longitude)),
        ITEM_CONTACT_NAME to dummyUserName,
        ITEM_CONTACT_EMAIL to dummyUserEmail
    )

    @Before
    fun setUp() {
        // the following instructions regarding the mockk library were inspired by
        // https://stackoverflow.com/questions/58158711/android-local-unit-test-mock-firebaseauth-with-mockk/58158712#58158712
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)
        itemDocSnap = mockDocumentSnapshotWith(dummyValues, dummyFirebaseId)
    }

    @Test
    fun testParsingAllFieldsValid() {
        // The resulting DocumentSnapshot can be used as if it came from Firestore.
        val item = ItemController.tryParse(itemDocSnap)
        assertNotNull("Item is null, parsing failed", item)

        // Filling a Item "by hand" to compare it to the parsed Item.
        assertEquals(dummyFirebaseId, item?.firebaseId)
        assertEquals(dummyValues[ITEM_NAME], item?.name)
        assertEquals(dummyValues[ITEM_DESCRIPTION], item?.description)
        assertEquals(dummyValues[ITEM_IS_SHARED], item?.isShared)
        assertEquals(dummyValues[ITEM_QUANTITY], item?.quantity)
        assertEquals(Unit.getByValue((dummyValues[ITEM_UNIT] as Long).toInt()), item?.unit)
        assertEquals((dummyValues[ITEM_BEST_BY] as Timestamp).toDate(), item?.bestByDate)
        assertEquals(dummyValues[ITEM_LOCATION], item?.location)
        assertEquals(dummyValues[ITEM_GEOHASH], item?.geohash)
        assertEquals(dummyValues[ITEM_CONTACT_NAME], item?.contactName)
        assertEquals(dummyValues[ITEM_CONTACT_EMAIL], item?.contactEmail)
        assertEquals(dummyValues[ITEM_OWNER], item?.ownerReference)

    }

    @Test
    fun testParsingItemOwnerNull() {
        val modifiedDummyValues = dummyValues.mapValues { entry -> if(entry.key == ITEM_OWNER) null else entry.value }
        itemDocSnap = mockDocumentSnapshotWith(modifiedDummyValues, dummyFirebaseId)

        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item)
    }

    @Test
    fun testParsingAllFieldsNullExceptItemOwner() {
        val modifiedDummyValues = dummyValues.mapValues { entry -> if (entry.key == ITEM_OWNER) entry.value else null }
        itemDocSnap = mockDocumentSnapshotWith(modifiedDummyValues, dummyFirebaseId)
        Mockito.`when`(itemDocSnap.getLong(ITEM_UNIT)).thenReturn(null)

        val expectedFirebaseId = dummyFirebaseId

        val item = ItemController.tryParse(itemDocSnap)

        assertEquals(expectedFirebaseId, item?.firebaseId)
        assertEquals("", item?.name)
        assertEquals(null, item?.description)
        assertEquals(false, item?.isShared)
        assertEquals(0L, item?.quantity)
        assertEquals(Unit.PIECE, item?.unit)
        assertEquals(null, item?.bestByDate)
        assertEquals(null, item?.location)
        assertEquals(null, item?.geohash)
        assertEquals(null, item?.contactName)
        assertEquals(null, item?.contactEmail)
        assertEquals(ownerDocRef, item?.ownerReference)

    }

    @Test
    fun testParsingItemDescriptionNotString() {
        Mockito.`when`(itemDocSnap.get(ITEM_DESCRIPTION)).thenReturn(1)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item?.description)

    }

    @Test
    fun testParsingItemIsSharedNotBoolean() {
        Mockito.`when`(itemDocSnap.get(ITEM_IS_SHARED)).thenReturn(1)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(false, item?.isShared)

    }

    @Test
    fun testParsingItemQuantityNotLong() {
        Mockito.`when`(itemDocSnap.get(ITEM_QUANTITY)).thenReturn("some string")
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(0L, item?.quantity)

    }

    @Test
    fun testParsingItemUnitValueNotLong() {
        Mockito.`when`(itemDocSnap.get(ITEM_UNIT)).thenReturn(false)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(Unit.PIECE, item?.unit)

    }
    @Test
    fun testParsingItemBestByNotTimestamp() {
        Mockito.`when`(itemDocSnap.get(ITEM_BEST_BY)).thenReturn("some string")
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item?.bestByDate)
    }

    @Test
    fun testParsingItemLocationNotGeoPoint() {
        Mockito.`when`(itemDocSnap.get(ITEM_LOCATION)).thenReturn(false)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item?.location)
    }

    @Test
    fun testParsingItemGeohashNotString() {
        Mockito.`when`(itemDocSnap.get(ITEM_GEOHASH)).thenReturn(1)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item?.geohash)
    }

    @Test
    fun testParsingItemContactNameNotString() {
        Mockito.`when`(itemDocSnap.get(ITEM_CONTACT_NAME)).thenReturn(1)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item?.contactName)
    }

    @Test
    fun testParsingItemContactEmailNotString() {
        Mockito.`when`(itemDocSnap.get(ITEM_CONTACT_EMAIL)).thenReturn(false)
        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item?.contactEmail)
    }

    @Test
    fun testParsingItemOwnerNotDocumentReference() {
        Mockito.`when`(itemDocSnap.get(ITEM_OWNER)).thenReturn("null")

        val item = ItemController.tryParse(itemDocSnap)

        assertEquals(null, item)
    }

    @Test
    fun testParsingAllFieldsMissingExceptItemOwner() {
        val minimalValues = mapOf<String, Any>(ITEM_OWNER to ownerDocRef)
        itemDocSnap = mockDocumentSnapshotWith(minimalValues, dummyFirebaseId)
        val item = ItemController.tryParse(itemDocSnap)


        assertEquals(Item(firebaseId = dummyFirebaseId, ownerReference = ownerDocRef), item)
    }

    @Test
    fun testParsingItemOwnerMissing() {
        val valuesWithoutOwnerField = dummyValues.filter { entry -> entry.key != ITEM_OWNER }
        itemDocSnap = mockDocumentSnapshotWith(valuesWithoutOwnerField, dummyFirebaseId)

        val item = ItemController.tryParse(itemDocSnap)

        assertEquals(null, item)
    }

    // the following code lines were inspired by
    // https://stackoverflow.com/questions/66275642/mockk-spy-on-top-level-private-function-in-kotlin
    // verify, that saveItem calls addItem when a new Item has to be saved
    @Test
    fun testSaveItemNewItem() {
        val dr = Mockito.mock(DocumentReference::class.java)
        val foo = spyk(ItemController, recordPrivateCalls = true)
        val item = Item(ownerReference = dr)
        foo.saveItem(item, callbackOnSuccess = {}, callbackOnFailure = {_ -> })
        verify(exactly = 1) { foo["addItem"](any<Item>(), any<() -> kotlin.Unit>(), any<(Exception) -> kotlin.Unit>()) }
    }

    // verify, that saveItem calls overrideItem when an existing Item has to be saved
    @Test
    fun testSaveItemExistingItem() {
        val dr = Mockito.mock(DocumentReference::class.java)
        val itemController = spyk(ItemController, recordPrivateCalls = true)
        val item = Item(firebaseId = "some_id", ownerReference = dr)
        itemController.saveItem(item, callbackOnSuccess = {}, callbackOnFailure = { _ -> })
        verify(exactly = 1) { itemController["overrideItem"](any<Item>(), any<() -> kotlin.Unit>(), any<(Exception) -> kotlin.Unit>()) }
    }

    private fun mockDocumentSnapshotWith(values: Map<String, Any?>, id: String): DocumentSnapshot {
        // Mockito works by intercepting method calls.
        // With "Mockito.`when`(<method call>).thenReturn(<return value>)" a return value can be replaced.
        // If a method isn't replaced, it will return null.
        //
        // Using the values map, the DocumentSnapshot can be "filled" with dummy data,
        // as long as only those methods below will be used.
        val dc = Mockito.mock(DocumentSnapshot::class.java)
        values.forEach { (key, value) ->
            Mockito.`when`(dc.get(key)).thenReturn(value)
        }
        Mockito.`when`(dc.id).thenReturn(id)

        return dc
    }
}