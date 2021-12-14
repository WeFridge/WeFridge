package app.wefridge.wefridge

import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.model.ItemController
import app.wefridge.wefridge.model.Unit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.*
import java.util.Date
import org.junit.Test
import org.mockito.Mockito


class ItemControllerUnitTest {

    @Test
    fun testParsing() {
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockk(relaxed = true)

        // Mock objects have the same methods as the original class
        val sn = Mockito.mock(DocumentSnapshot::class.java)
        val dr = Mockito.mock(DocumentReference::class.java)

        // dummy data
        val values = mapOf<String, Any>(
            ITEM_OWNER to dr,
            ITEM_UNIT to Unit.KILOGRAM.value.toLong()
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
}