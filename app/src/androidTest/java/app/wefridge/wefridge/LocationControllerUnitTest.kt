package app.wefridge.wefridge

import android.content.Context
import app.wefridge.wefridge.model.LocationController
import com.google.firebase.firestore.GeoPoint
import org.junit.Test
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before


/*
* NOTE: Run the following tests with device language set to English.
* */
@RunWith(AndroidJUnit4::class)
class LocationControllerUnitTest {

    // TODO: add more test cases

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().context
    }

    @Test
    fun testGetGeoPointFrom() {
        val result = LocationController.getGeoPointFrom("Berlin", context)
        assertEquals(GeoPoint(52.520006599999995,13.404954), result)
    }

    @Test
    fun testBuildAddressStringFrom() {
        val result = LocationController.buildAddressStringFrom(GeoPoint(52.520006599999995, 13.404954), context)
        assertEquals("B2 7, 10178 Berlin, Germany, 10178 Berlin, Berlin", result)
    }
}