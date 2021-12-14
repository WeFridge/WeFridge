package app.wefridge.wefridge

import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.model.Unit
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.w3c.dom.Document


class ItemUnitTest {
    private lateinit var item: Item
    private lateinit var ownerRef: DocumentReference

    @Before
    fun setup() {
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
        assertEquals(item.contactName, null)
        assertEquals(item.contactEmail, null)
        assertEquals(item.ownerReference, ownerRef)
    }
}