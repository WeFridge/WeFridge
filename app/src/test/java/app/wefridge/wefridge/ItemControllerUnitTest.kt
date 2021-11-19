package app.wefridge.wefridge

import app.wefridge.wefridge.datamodel.Item
import app.wefridge.wefridge.datamodel.ItemController
import app.wefridge.wefridge.datamodel.Unit
import app.wefridge.wefridge.exceptions.ItemOwnerMissingException
import com.google.firebase.Timestamp
import org.junit.Assert.*
import java.util.Date
import org.junit.Test
import java.lang.reflect.Method

class ItemControllerUnitTest {

    private val firebaseId = "ABC"
    private val name = "Gouda Cheese"
    private val description = "So good!"
    private val isShared = false
    private val quantity: Long = 0
    private val unit = Unit.KILOGRAM
    private val unitValue: Long = unit.value.toLong()
    private val sharedEmail = "karen@gmail.com"
    private val best_by_date = Date()
    private val owner = "users/MnYhb6LQbRjdLRjvnYqt"

    private val itemData = mapOf<String, Any>(
        "name" to name,
        "description" to description,
        "is_shared" to isShared,
        "quantity" to quantity,
        "unit" to unitValue,
        "shared_email" to sharedEmail,
        "best_by" to Timestamp(best_by_date),
        "owner" to owner
    )

    @Test
    fun testParsingWithAllFieldsExisting() {
        val parsedItem = ItemController.parse(itemData, firebaseId)
        val expectedItem = Item(firebaseId, name, description, isShared, quantity.toInt(), unit, sharedEmail, best_by_date, owner)
        assertEquals(expectedItem, parsedItem)
    }

    @Test
    fun testParsingWithoutFirebaseId() {
        val parsedItem = ItemController.parse(itemData, null)
        val expectedItem = Item(null, name, description, isShared, quantity.toInt(), unit, sharedEmail, best_by_date, owner)
        assertEquals(expectedItem, parsedItem)
    }

    @Test
    fun testParsingWithoutName() {
        val modifiedItemData = mapWithNullValue(itemData, "name")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, null, description, isShared, quantity.toInt(), unit, sharedEmail, best_by_date, owner)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutDescription() {
        val modifiedItemData = mapWithNullValue(itemData, "description")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, null, isShared, quantity.toInt(), unit, sharedEmail, best_by_date, owner)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutIsShared() {
        val modifiedItemData = mapWithNullValue(itemData, "is_shared")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, null, quantity.toInt(), unit, sharedEmail, best_by_date, owner)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutQuantity() {
        val modifiedItemData = mapWithNullValue(itemData, "quantity")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, null, unit, sharedEmail, best_by_date, owner)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutUnit() {
        val modifiedItemData = mapWithNullValue(itemData, "unit")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, quantity.toInt(), null, sharedEmail, best_by_date, owner)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutSharedEmail() {
        val modifiedItemData = mapWithNullValue(itemData, "shared_email")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, quantity.toInt(), unit, null, best_by_date, owner)
        assertEquals(itemExpected, parsedItem)
    }

    @Test
    fun testParsingWithoutDate() {
        val modifiedItemData = mapWithNullValue(itemData, "best_by")
        val parsedItem = ItemController.parse(modifiedItemData, firebaseId)
        val itemExpected = Item(firebaseId, name, description, isShared, quantity.toInt(), unit, sharedEmail, null, owner)
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