package app.wefridge.wefridge

import app.wefridge.wefridge.datamodel.Item
import app.wefridge.wefridge.datamodel.Unit
import org.junit.Assert.*
import java.util.Date
import org.junit.Test

class ItemUnitTest {
    private val firebaseId = "ABC"
    private val name = "Gouda Cheese"
    private val description = "So good!"
    private val isShared = false
    private val quantity = 0
    private val unit = Unit.KILOGRAM
    private val sharedEmail = "karen@gmail.com"
    private val date = Date()
    private val owner = "users/MnYhb6LQbRjdLRjvnYqt"

    @Test
    fun testInitWithoutFirebaseId() {
        val item = Item(null, name, description, isShared, quantity, unit, sharedEmail, date, owner)
        assertEquals(null, item.firebaseId)
    }

    @Test
    fun testInitWithoutName() {
        val item = Item(firebaseId, null, description, isShared, quantity, unit, sharedEmail, date, owner)
        assertEquals(null, item.name)
    }

    @Test
    fun testInitWithoutDescription() {
        val item = Item(firebaseId, name, null, isShared, quantity, unit, sharedEmail, date, owner)
        assertEquals(null, item.description)
    }

    @Test
    fun testInitWithoutIsShared() {
        val item = Item(firebaseId, name, description, null, quantity, unit, sharedEmail, date, owner)
        assertEquals(false, item.isShared)
    }

    @Test
    fun testInitWithoutQuantity() {
        val item = Item(firebaseId, name, description, isShared, null, unit, sharedEmail, date, owner)
        assertEquals(0, item.quantity)
    }

    @Test
    fun testInitWithoutUnit() {
        val item = Item(firebaseId, name, description, isShared, quantity, null, sharedEmail, date, owner)
        assertEquals(null, item.unit)
    }

    @Test
    fun testInitWithoutSharedEmail() {
        val item = Item(firebaseId, name, description, isShared, quantity, unit, null, date, owner)
        assertEquals(null, item.sharedEmail)
    }

    @Test
    fun testInitWithoutBestByDate() {
        val item = Item(firebaseId, name, description, isShared, quantity, unit, sharedEmail, null, owner)
        assertEquals(null, item.bestByDate)
    }
}