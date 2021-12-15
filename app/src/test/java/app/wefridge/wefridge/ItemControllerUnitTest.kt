package app.wefridge.wefridge

import app.wefridge.wefridge.model.*
import app.wefridge.wefridge.model.Unit
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import java.util.Date
import org.junit.Test
import org.mockito.Mockito


class ItemControllerUnitTest {

    // TODO: test implementation of listeners in deleteItem (when related branch gets pulled into main)
    // TODO: consider to delete getItems in ItemController

    // Mock objects have the same methods as the original class
    private lateinit var itemDocSnap: DocumentSnapshot
    private val ownerDocRef = Mockito.mock(DocumentReference::class.java)

    // dummy data
    private var dummyUserName = "Karen Anderson"
    private var dummyUserEmail = "karen@anderson.de"
    private var dummyFirebaseId = "demo_id"
    private var dummyGeoPoint = GeoPoint(59.3294,18.0686)
    private var dummyValues = mapOf<String, Any>(
        ITEM_OWNER to ownerDocRef,
        ITEM_NAME to "Gouda Cheese",
        ITEM_DESCRIPTION to "So good!",
        ITEM_IS_SHARED to true,
        ITEM_QUANTITY to 10L,
        ITEM_UNIT to Unit.KILOGRAM.value.toLong(),
        ITEM_BEST_BY to Date(),
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
    fun testParsing() {
        // The resulting DocumentSnapshot can be used as if it came from Firestore.
        val item = ItemController.tryParse(itemDocSnap)
        assertNotNull("Item is null, parsing failed", item)

        // Filling a Item "by hand" to compare it to the parsed Item.
        val item1 = Item(dummyFirebaseId, unit = Unit.KILOGRAM, ownerReference = ownerDocRef)
        assertEquals("Parsing unit failed", item!!.unit, item1.unit)
        assertEquals("Parsing id failed", item.firebaseId, item1.firebaseId)
    }

    @Test
    fun testParsingWithoutItemOwner() {
        dummyValues = dummyValues.filter { itemAttribute -> itemAttribute.key != ITEM_OWNER }
        itemDocSnap = mockDocumentSnapshotWith(dummyValues, dummyFirebaseId)

        val item = ItemController.tryParse(itemDocSnap)
        assertEquals(null, item)

    }

    // the following code lines were inspired by
    // https://stackoverflow.com/questions/66275642/mockk-spy-on-top-level-private-function-in-kotlin
    // verify, that saveItem calls addItem when a new Item has to be saved
    @Test
    fun testSaveItemWithNewItem() {
        val dr = Mockito.mock(DocumentReference::class.java)
        val foo = spyk(ItemController, recordPrivateCalls = true)
        val item = Item(ownerReference = dr)
        foo.saveItem(item, callbackOnSuccess = {}, callbackOnFailure = {_ -> })
        verify(exactly = 1) { foo["addItem"](any<Item>(), any<() -> kotlin.Unit>(), any<(Exception) -> kotlin.Unit>()) }
    }

    // verify, that saveItem calls overrideItem when an existing Item has to be saved
    @Test
    fun testSaveItemWithExistingItem() {
        val dr = Mockito.mock(DocumentReference::class.java)
        val itemController = spyk(ItemController, recordPrivateCalls = true)
        val item = Item(firebaseId = "some_id", ownerReference = dr)
        itemController.saveItem(item, callbackOnSuccess = {}, callbackOnFailure = { _ -> })
        verify(exactly = 1) { itemController["overrideItem"](any<Item>(), any<() -> kotlin.Unit>(), any<(Exception) -> kotlin.Unit>()) }
    }

    private fun mockDocumentSnapshotWith(values: Map<String, Any>, id: String): DocumentSnapshot {
        // Mockito works by intercepting method calls.
        // With "Mockito.`when`(<method call>).thenReturn(<return value>)" a return value can be replaced.
        // If a method isn't replaced, it will return null.
        //
        // Using the values map, the DocumentSnapshot can be "filled" with dummy data,
        // as long as only those methods below will be used.
        val dc = Mockito.mock(DocumentSnapshot::class.java)
        values.forEach { (key, value) ->
            Mockito.`when`(when (value) {
                is DocumentReference -> dc.getDocumentReference(key)
                is Boolean -> dc.getBoolean(key)
                is Long -> dc.getLong(key)
                is GeoPoint -> dc.getGeoPoint(key)
                is Timestamp -> dc.getTimestamp(key)
                is Date -> dc.getDate(key)
                is Blob -> dc.getBlob(key)
                else -> dc.getString(key)
            }).thenReturn(value)
        }
        Mockito.`when`(dc.id).thenReturn(id)

        return dc
    }
}