package app.wefridge.wefridge

import app.wefridge.wefridge.model.Item
import app.wefridge.wefridge.model.Unit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test


class ItemUnitTest {
    private lateinit var item: Item

    @Before
    fun setup() {
        // arrange
        // set up Item with ownerReference
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

    }
}