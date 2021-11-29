package app.wefridge.wefridge

import app.wefridge.wefridge.datamodel.Item
import app.wefridge.wefridge.datamodel.Unit
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

class ItemUnitTest {
    private val firebaseId = "ABC"
    private val name = "Gouda Cheese"
    private val description = "So good!"
    private val isShared = false
    private val quantity = 0
    private val unit = Unit.KILOGRAM
    private val sharedEmail = "karen@gmail.com"
    private val bestByDate = Date()
    private val location = GeoPoint(59.3294,18.0686)
    private val geohash = GeoFireUtils.getGeoHashForLocation(GeoLocation(location.latitude, location.longitude))
    private val contactName = "Karen Anderson"
    private val contactEmail = "karen@anderson.de"
    private val ownerID = "MnYhb6LQbRjdLRjvnYqt"
    private lateinit var ownerRef: DocumentReference

    @BeforeClass
    fun setUpItemData() {
        val documentSnapshotTask: Task<DocumentSnapshot> = FirebaseFirestore.getInstance().document("users/${ownerID}").get()
        val ownerDocumentSnapshot: DocumentSnapshot = Tasks.await(documentSnapshotTask)
        ownerRef = ownerDocumentSnapshot.reference

    }

    @Test
    fun testInitWithoutFirebaseId() {
        val item = Item(null, name, description, isShared, quantity, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(null, item.firebaseId)
    }

    @Test
    fun testInitWithoutName() {
        val item = Item(firebaseId, null, description, isShared, quantity, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(null, item.name)
    }

    @Test
    fun testInitWithoutDescription() {
        val item = Item(firebaseId, name, null, isShared, quantity, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(null, item.description)
    }

    @Test
    fun testInitWithoutIsShared() {
        val item = Item(firebaseId, name, description, null, quantity, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(false, item.isShared)
    }

    @Test
    fun testInitWithoutQuantity() {
        val item = Item(firebaseId, name, description, isShared, null, unit, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(0, item.quantity)
    }

    @Test
    fun testInitWithoutUnit() {
        val item = Item(firebaseId, name, description, isShared, quantity, null, sharedEmail, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(null, item.unit)
    }

    @Test
    fun testInitWithoutSharedEmail() {
        val item = Item(firebaseId, name, description, isShared, quantity, unit, null, bestByDate, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(null, item.sharedEmail)
    }

    @Test
    fun testInitWithoutBestByDate() {
        val item = Item(firebaseId, name, description, isShared, quantity, unit, sharedEmail, null, location, geohash, contactName, contactEmail, ownerRef)
        assertEquals(null, item.bestByDate)
    }
}