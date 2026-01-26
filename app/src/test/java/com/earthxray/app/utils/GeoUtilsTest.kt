package com.earthxray.app.utils

import com.earthxray.app.data.Place
import com.earthxray.app.data.PlaceType
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class GeoUtilsTest {

    companion object {
        private const val EPSILON = 0.0001 // Tolerance for floating point comparisons
    }

    // Vector3D Tests
    @Test
    fun testVector3D_normalize() {
        val v = GeoUtils.Vector3D(3.0, 4.0, 0.0)
        val normalized = v.normalize()

        assertEquals(0.6, normalized.x, EPSILON)
        assertEquals(0.8, normalized.y, EPSILON)
        assertEquals(0.0, normalized.z, EPSILON)

        // Magnitude should be 1
        assertEquals(1.0, normalized.magnitude(), EPSILON)
    }

    @Test
    fun testVector3D_dotProduct() {
        val v1 = GeoUtils.Vector3D(1.0, 2.0, 3.0)
        val v2 = GeoUtils.Vector3D(4.0, 5.0, 6.0)

        val dot = v1.dot(v2)
        assertEquals(32.0, dot, EPSILON) // 1*4 + 2*5 + 3*6 = 32
    }

    @Test
    fun testVector3D_crossProduct() {
        val v1 = GeoUtils.Vector3D(1.0, 0.0, 0.0)
        val v2 = GeoUtils.Vector3D(0.0, 1.0, 0.0)

        val cross = v1.cross(v2)
        assertEquals(0.0, cross.x, EPSILON)
        assertEquals(0.0, cross.y, EPSILON)
        assertEquals(1.0, cross.z, EPSILON)
    }

    @Test
    fun testVector3D_addition() {
        val v1 = GeoUtils.Vector3D(1.0, 2.0, 3.0)
        val v2 = GeoUtils.Vector3D(4.0, 5.0, 6.0)

        val sum = v1.plus(v2)
        assertEquals(5.0, sum.x, EPSILON)
        assertEquals(7.0, sum.y, EPSILON)
        assertEquals(9.0, sum.z, EPSILON)
    }

    @Test
    fun testVector3D_subtraction() {
        val v1 = GeoUtils.Vector3D(4.0, 5.0, 6.0)
        val v2 = GeoUtils.Vector3D(1.0, 2.0, 3.0)

        val diff = v1.minus(v2)
        assertEquals(3.0, diff.x, EPSILON)
        assertEquals(3.0, diff.y, EPSILON)
        assertEquals(3.0, diff.z, EPSILON)
    }

    @Test
    fun testVector3D_scalarMultiplication() {
        val v = GeoUtils.Vector3D(1.0, 2.0, 3.0)
        val scaled = v.times(2.0)

        assertEquals(2.0, scaled.x, EPSILON)
        assertEquals(4.0, scaled.y, EPSILON)
        assertEquals(6.0, scaled.z, EPSILON)
    }

    @Test
    fun testVector3D_magnitude() {
        val v = GeoUtils.Vector3D(3.0, 4.0, 0.0)
        assertEquals(5.0, v.magnitude(), EPSILON)

        val v2 = GeoUtils.Vector3D(1.0, 1.0, 1.0)
        assertEquals(sqrt(3.0), v2.magnitude(), EPSILON)
    }

    // Coordinate Conversion Tests
    @Test
    fun testLatLonToVector_equator() {
        // Point on equator at prime meridian
        val v = GeoUtils.latLonToVector(0.0, 0.0)

        assertEquals(1.0, v.x, EPSILON)
        assertEquals(0.0, v.y, EPSILON)
        assertEquals(0.0, v.z, EPSILON)
    }

    @Test
    fun testLatLonToVector_northPole() {
        val v = GeoUtils.latLonToVector(90.0, 0.0)

        assertEquals(0.0, v.x, EPSILON)
        assertEquals(0.0, v.y, EPSILON)
        assertEquals(1.0, v.z, EPSILON)
    }

    @Test
    fun testLatLonToVector_southPole() {
        val v = GeoUtils.latLonToVector(-90.0, 0.0)

        assertEquals(0.0, v.x, EPSILON)
        assertEquals(0.0, v.y, EPSILON)
        assertEquals(-1.0, v.z, EPSILON)
    }

    @Test
    fun testVectorToLatLon_roundTrip() {
        val testCases = listOf(
            Pair(40.7128, -74.0060), // New York
            Pair(51.5074, -0.1278),   // London
            Pair(-33.8688, 151.2093), // Sydney
            Pair(0.0, 0.0),           // Equator, Prime Meridian
            Pair(90.0, 0.0),          // North Pole
            Pair(-90.0, 0.0)          // South Pole
        )

        for ((lat, lon) in testCases) {
            val vector = GeoUtils.latLonToVector(lat, lon)
            val result = GeoUtils.vectorToLatLon(vector)

            assertEquals(lat, result.latitude, EPSILON)
            assertEquals(lon, result.longitude, EPSILON)
        }
    }

    // Antipodal Point Tests
    @Test
    fun testCalculateAntipodal_primeMeridian() {
        val result = GeoUtils.calculateAntipodal(0.0, 0.0)

        assertEquals(0.0, result.latitude, EPSILON)
        assertEquals(180.0, result.longitude, EPSILON)
    }

    @Test
    fun testCalculateAntipodal_newYork() {
        // New York: 40.7128°N, 74.0060°W
        val result = GeoUtils.calculateAntipodal(40.7128, -74.0060)

        assertEquals(-40.7128, result.latitude, EPSILON)
        assertEquals(105.994, result.longitude, EPSILON) // -74.006 + 180
    }

    @Test
    fun testCalculateAntipodal_sydney() {
        // Sydney: 33.8688°S, 151.2093°E
        val result = GeoUtils.calculateAntipodal(-33.8688, 151.2093)

        assertEquals(33.8688, result.latitude, EPSILON)
        assertEquals(-28.7907, result.longitude, EPSILON) // 151.2093 - 180
    }

    @Test
    fun testCalculateAntipodal_northPole() {
        val result = GeoUtils.calculateAntipodal(90.0, 0.0)

        assertEquals(-90.0, result.latitude, EPSILON)
        assertEquals(180.0, result.longitude, EPSILON)
    }

    // Distance Calculation Tests (Haversine)
    @Test
    fun testCalculateDistance_samePoint() {
        val distance = GeoUtils.calculateDistance(40.7128, -74.0060, 40.7128, -74.0060)
        assertEquals(0.0, distance, EPSILON)
    }

    @Test
    fun testCalculateDistance_newYorkToLondon() {
        // New York to London is approximately 5,570 km
        val distance = GeoUtils.calculateDistance(
            40.7128, -74.0060,  // New York
            51.5074, -0.1278     // London
        )

        assertEquals(5570.0, distance, 50.0) // Allow 50km tolerance
    }

    @Test
    fun testCalculateDistance_antipodal() {
        // Distance between antipodal points should be half Earth's circumference
        // Half of 40,075 km = approximately 20,037 km
        val distance = GeoUtils.calculateDistance(0.0, 0.0, 0.0, 180.0)

        assertEquals(20015.0, distance, 50.0)
    }

    @Test
    fun testCalculateDistance_equatorQuarter() {
        // Quarter of equator (90 degrees longitude at equator)
        // Should be approximately 10,000 km
        val distance = GeoUtils.calculateDistance(0.0, 0.0, 0.0, 90.0)

        assertEquals(10007.5, distance, 50.0)
    }

    // Bearing Tests
    @Test
    fun testCalculateBearing_north() {
        val bearing = GeoUtils.calculateBearing(0.0, 0.0, 1.0, 0.0)
        assertEquals(0.0, bearing, EPSILON) // Due north
    }

    @Test
    fun testCalculateBearing_east() {
        val bearing = GeoUtils.calculateBearing(0.0, 0.0, 0.0, 1.0)
        assertEquals(90.0, bearing, EPSILON) // Due east
    }

    @Test
    fun testCalculateBearing_south() {
        val bearing = GeoUtils.calculateBearing(1.0, 0.0, 0.0, 0.0)
        assertEquals(180.0, bearing, EPSILON) // Due south
    }

    @Test
    fun testCalculateBearing_west() {
        val bearing = GeoUtils.calculateBearing(0.0, 1.0, 0.0, 0.0)
        assertEquals(270.0, bearing, EPSILON) // Due west
    }

    // Ray Exit Point Tests
    @Test
    fun testCalculateRayExitPoint_straightDown() {
        // Pointing straight down from equator should give antipodal point
        val result = GeoUtils.calculateRayExitPoint(0.0, 0.0, 0.0, -90.0)

        assertNotNull(result)
        assertEquals(0.0, result!!.latitude, 1.0) // Allow some tolerance
        assertEquals(180.0, abs(result.longitude), 1.0)
    }

    @Test
    fun testCalculateRayExitPoint_shallowAngle() {
        // Pointing at -45 degrees (halfway down) should exit somewhere between
        val result = GeoUtils.calculateRayExitPoint(0.0, 0.0, 0.0, -45.0)

        assertNotNull(result)
        // Should be somewhere in southern hemisphere
        assertTrue(result!!.latitude < 0)
    }

    @Test
    fun testCalculateRayExitPoint_pointingUp() {
        // Pointing up should return null
        val result = GeoUtils.calculateRayExitPoint(0.0, 0.0, 0.0, 10.0)

        assertNull(result)
    }

    @Test
    fun testCalculateRayExitPoint_horizontally() {
        // Pointing near-horizontally should return null
        val result = GeoUtils.calculateRayExitPoint(0.0, 0.0, 0.0, -5.0)

        assertNull(result)
    }

    // Direction to Point Through Earth Tests
    @Test
    fun testCalculateDirectionToPointThroughEarth_sameLocation() {
        val result = GeoUtils.calculateDirectionToPointThroughEarth(
            40.7128, -74.0060,
            40.7128, -74.0060
        )

        // Should point straight down when targeting same location
        assertNotNull(result)
        assertTrue(result!!.pitch < -80) // Very steep pitch
    }

    @Test
    fun testCalculateDirectionToPointThroughEarth_antipodalPoint() {
        // Targeting antipodal point should result in pointing straight down
        val result = GeoUtils.calculateDirectionToPointThroughEarth(
            40.7128, -74.0060,
            -40.7128, 105.994
        )

        assertNotNull(result)
        // Pitch should be close to -90 (straight down)
        assertTrue(result!!.pitch < -85)
    }

    @Test
    fun testCalculateDirectionToPointThroughEarth_nearbyCity() {
        // New York to Philadelphia (nearby) - should have shallow angle
        val result = GeoUtils.calculateDirectionToPointThroughEarth(
            40.7128, -74.0060,  // New York
            39.9526, -75.1652   // Philadelphia
        )

        assertNotNull(result)
        // Nearby cities should require less steep pitch
        assertTrue(result!!.pitch > -45)
    }

    // findNearbyPlaces Tests
    @Test
    fun testFindNearbyPlaces() {
        val places = listOf(
            Place("Close City", 40.0, 0.0, "Country", PlaceType.CITY, 1000000),
            Place("Far City", 80.0, 0.0, "Country", PlaceType.CITY, 500000),
            Place("Very Close City", 40.1, 0.1, "Country", PlaceType.CITY, 750000)
        )

        val results = GeoUtils.findNearbyPlaces(40.0, 0.0, places, radiusKm = 500.0)

        // Should find Close City and Very Close City, but not Far City
        assertEquals(2, results.size)
        assertTrue(results.any { it.first.name == "Close City" })
        assertTrue(results.any { it.first.name == "Very Close City" })
        assertFalse(results.any { it.first.name == "Far City" })
    }

    @Test
    fun testFindNearbyPlaces_sorted() {
        val places = listOf(
            Place("Far", 41.0, 0.0, "Country", PlaceType.CITY),
            Place("Near", 40.1, 0.0, "Country", PlaceType.CITY),
            Place("Medium", 40.5, 0.0, "Country", PlaceType.CITY)
        )

        val results = GeoUtils.findNearbyPlaces(40.0, 0.0, places, radiusKm = 500.0)

        // Results should be sorted by distance (closest first)
        assertTrue(results.size >= 2)
        assertTrue(results[0].second < results[1].second)
    }

    // findPlacesInView Tests
    @Test
    fun testFindPlacesInView_populationFilter() {
        val places = listOf(
            Place("Big City", 40.0, 0.0, "Country", PlaceType.CITY, 5000000),
            Place("Small City", 40.1, 0.1, "Country", PlaceType.CITY, 10000),
            Place("Tiny City", 40.2, 0.2, "Country", PlaceType.CITY, 5000)
        )

        val results = GeoUtils.findPlacesInView(
            -40.0, -180.0, // Antipodal point
            0.0, -90.0,    // Pointing straight down
            places,
            fieldOfViewDegrees = 60.0,
            minPopulation = 100000
        )

        // Should only find Big City
        assertEquals(1, results.size)
        assertEquals("Big City", results[0].first.name)
    }

    @Test
    fun testFindPlacesInView_sortedByPopulation() {
        val places = listOf(
            Place("Medium City", 40.0, 0.0, "Country", PlaceType.CITY, 1000000),
            Place("Big City", 40.1, 0.1, "Country", PlaceType.CITY, 5000000),
            Place("Small City", 40.2, 0.2, "Country", PlaceType.CITY, 500000)
        )

        val results = GeoUtils.findPlacesInView(
            -40.0, -180.0,
            0.0, -90.0,
            places,
            fieldOfViewDegrees = 60.0,
            minPopulation = 0
        )

        // Should be sorted by population (highest first)
        assertTrue(results.size >= 2)
        assertTrue(results[0].first.population >= results[1].first.population)
    }

    // Format Distance Tests
    @Test
    fun testFormatDistance_meters() {
        assertEquals("500 m", GeoUtils.formatDistance(0.5))
        assertEquals("999 m", GeoUtils.formatDistance(0.999))
    }

    @Test
    fun testFormatDistance_kilometersDecimal() {
        assertEquals("5.5 km", GeoUtils.formatDistance(5.5))
        assertEquals("9.9 km", GeoUtils.formatDistance(9.9))
    }

    @Test
    fun testFormatDistance_kilometersWhole() {
        assertEquals("100 km", GeoUtils.formatDistance(100.0))
        assertEquals("5570 km", GeoUtils.formatDistance(5570.0))
    }

    // Edge Cases
    @Test
    fun testVector3D_normalizeZeroVector() {
        val v = GeoUtils.Vector3D(0.0, 0.0, 0.0)
        val normalized = v.normalize()

        // Should return itself (zero vector)
        assertEquals(0.0, normalized.x, EPSILON)
        assertEquals(0.0, normalized.y, EPSILON)
        assertEquals(0.0, normalized.z, EPSILON)
    }

    @Test
    fun testCalculateDistance_poleToPole() {
        // North pole to south pole should be half Earth's circumference
        val distance = GeoUtils.calculateDistance(90.0, 0.0, -90.0, 0.0)

        assertEquals(20015.0, distance, 50.0)
    }

    @Test
    fun testCalculateBearing_almostIdenticalPoints() {
        // Very close points should still give valid bearing
        val bearing = GeoUtils.calculateBearing(0.0, 0.0, 0.0001, 0.0)

        assertTrue(bearing >= 0.0 && bearing < 360.0)
    }
}
