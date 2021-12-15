package app.wefridge.wefridge

import junit.framework.TestCase

class UtilsTest : TestCase() {

    fun testMd5() {
        // examples from https://gravatar.com/site/implement/hash/
        assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", "myemailaddress@example.com".md5())
        assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", "MyEmailAddress@example.com".md5())
        assertEquals("0bc83cb571cd1c50ba6f3e8a78ef1346", "  MyEmailAddress@example.com   ".md5())
    }

    fun testFormatDistance() {
        // below 1000m
        assertEquals("0m", formatDistance(0.0))
        assertEquals("10m", formatDistance(10.0))
        assertEquals("10m", formatDistance(10.4))
        assertEquals("11m", formatDistance(10.5))

        // below 100km
        assertEquals("1.5km", formatDistance(1500.0))
        assertEquals("1.5km", formatDistance(1540.69))
        assertEquals("1.6km", formatDistance(1550.0))

        // above 100km
        assertEquals("101km", formatDistance(101000.0))
        assertEquals("9453km", formatDistance(9453000.0))
    }
}