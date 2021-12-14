package app.wefridge.wefridge

import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.model.ItemController
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.w3c.dom.Document


class ItemControllerUnitTest {

    @Test
    fun testParsing() {
        // the following instructions regarding the mockk library were inspired by
        // https://stackoverflow.com/questions/58158711/android-local-unit-test-mock-firebaseauth-with-mockk/58158712#58158712
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        // Mock objects have the same methods as the original class
        val sn = Mockito.mock(DocumentSnapshot::class.java)
        val dr = Mockito.mock(DocumentReference::class.java)

        // dummy data
        val dummyGeoPoint = GeoPoint(59.3294,18.0686)
        val values = mapOf<String, Any>(
            ITEM_OWNER to dr,
            ITEM_NAME to "Gouda Cheese",
            ITEM_DESCRIPTION to "So good!",
            ITEM_IS_SHARED to true,
            ITEM_QUANTITY to 10L,
            ITEM_UNIT to Unit.KILOGRAM.value.toLong(),
            ITEM_BEST_BY to Date(),
            ITEM_LOCATION to dummyGeoPoint,
            ITEM_GEOHASH to GeoFireUtils.getGeoHashForLocation(GeoLocation(dummyGeoPoint.latitude, dummyGeoPoint.longitude)),
            ITEM_CONTACT_NAME to "Karen Anderson",
            ITEM_CONTACT_EMAIL to "karen@anderson.de"
        )
        val id = "demo_id"

        // Mockito works, by intercepting method calls.
        // With "Mockito.`when`(<method call>).thenReturn(<return value>)" a return value can be replaced.
        // If a method isn't replaced, it will return null.
        //
        // Using the values map, the DocumentSnapshot can be "filled" with dummy data,
        // as long as only those methods below will be used.
        values.forEach { (key, value) ->
            Mockito.`when`(when (value) {
                is DocumentReference -> sn.getDocumentReference(key)
                is Boolean -> sn.getBoolean(key)
                is Long -> sn.getLong(key)
                is GeoPoint -> sn.getGeoPoint(key)
                is Timestamp -> sn.getTimestamp(key)
                is Date -> sn.getDate(key)
                is Blob -> sn.getBlob(key)
                else -> sn.getString(key)
            }).thenReturn(value)
        }
        Mockito.`when`(sn.id).thenReturn(id)


        // The resulting DocumentSnapshot can be used as if it came from Firestore.
        val item = ItemController.tryParse(sn)
        assertNotNull("Item is null, parsing failed", item)

        // Filling a Item "by hand" to compare it to the parsed Item.
        val item1 = Item(id, unit = Unit.KILOGRAM, ownerReference = dr)
        assertEquals("Parsing unit failed", item!!.unit, item1.unit)
        assertEquals("Parsing id failed", item.firebaseId, item1.firebaseId)
    }

    @Test
    fun testParsing_withoutItemOwner() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)
        val itemDocSnap = Mockito.mock(DocumentSnapshot::class.java)
        val dummyGeoPoint = GeoPoint(59.3294,18.0686)
        val values = mapOf<String, Any>(
            ITEM_NAME to "Gouda Cheese",
            ITEM_DESCRIPTION to "So good!",
            ITEM_IS_SHARED to true,
            ITEM_QUANTITY to 10L,
            ITEM_UNIT to Unit.KILOGRAM.value.toLong(),
            ITEM_BEST_BY to Timestamp(Date()),
            ITEM_LOCATION to dummyGeoPoint,
            ITEM_GEOHASH to GeoFireUtils.getGeoHashForLocation(GeoLocation(dummyGeoPoint.latitude, dummyGeoPoint.longitude)),
            ITEM_CONTACT_NAME to "Karen Anderson",
            ITEM_CONTACT_EMAIL to "karen@anderson.de"
        )

        values.forEach { (key, value) ->
            Mockito.`when`(when (value) {
                is DocumentReference -> itemDocSnap.getDocumentReference(key)
                is Boolean -> itemDocSnap.getBoolean(key)
                is Long -> itemDocSnap.getLong(key)
                is GeoPoint -> itemDocSnap.getGeoPoint(key)
                is Timestamp -> itemDocSnap.getTimestamp(key)
                is Date -> itemDocSnap.getDate(key)
                is Blob -> itemDocSnap.getBlob(key)
                else -> itemDocSnap.getString(key)
            }).thenReturn(value)
        }
        Mockito.`when`(itemDocSnap.id).thenReturn("some_id")

        val item = ItemController.tryParse(itemDocSnap)

        assertEquals(null, item)
    }

    // the following code lines were inspired by
    // https://stackoverflow.com/questions/66275642/mockk-spy-on-top-level-private-function-in-kotlin
    // verify, that saveItem calls addItem when a new Item has to be saved
    @Test
    fun testSaveItem_newItem() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)
        val dr = Mockito.mock(DocumentReference::class.java)
        val foo = spyk(ItemController, recordPrivateCalls = true)
        val item = Item(ownerReference = dr)
        foo.saveItem(item, callbackOnSuccess = {}, callbackOnFailure = {_ -> })
        verify(exactly = 1) { foo["addItem"](any<Item>(), any<() -> kotlin.Unit>(), any<(Exception) -> kotlin.Unit>()) }
    }

    // verify, that saveItem calls overrideItem when an existing Item has to be saved
    @Test
    fun testSaveItem_existingItem() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)
        val dr = Mockito.mock(DocumentReference::class.java)
        val foo = spyk(ItemController, recordPrivateCalls = true)
        val item = Item(firebaseId = "some_id", ownerReference = dr)
        foo.saveItem(item, callbackOnSuccess = {}, callbackOnFailure = {_ -> })
        verify(exactly = 1) { foo["overrideItem"](any<Item>(), any<() -> kotlin.Unit>(), any<(Exception) -> kotlin.Unit>()) }
    }
}