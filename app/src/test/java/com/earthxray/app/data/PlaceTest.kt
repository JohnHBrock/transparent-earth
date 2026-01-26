package com.earthxray.app.data

import org.junit.Assert.*
import org.junit.Test

class PlaceTest {

    @Test
    fun testPlace_creation() {
        val place = Place(
            name = "Test City",
            latitude = 40.7128,
            longitude = -74.0060,
            country = "USA",
            type = PlaceType.CITY,
            population = 8000000
        )

        assertEquals("Test City", place.name)
        assertEquals(40.7128, place.latitude, 0.0001)
        assertEquals(-74.0060, place.longitude, 0.0001)
        assertEquals("USA", place.country)
        assertEquals(PlaceType.CITY, place.type)
        assertEquals(8000000, place.population)
    }

    @Test
    fun testPlace_defaultValues() {
        val place = Place(
            name = "Landmark",
            latitude = 40.0,
            longitude = -74.0,
            country = "USA"
        )

        assertEquals(PlaceType.CITY, place.type) // Default type
        assertEquals(0, place.population) // Default population
    }

    @Test
    fun testPlace_landmark() {
        val landmark = Place(
            name = "Eiffel Tower",
            latitude = 48.8584,
            longitude = 2.2945,
            country = "France",
            type = PlaceType.LANDMARK
        )

        assertEquals(PlaceType.LANDMARK, landmark.type)
        assertEquals(0, landmark.population) // Landmarks typically have 0 population
    }

    @Test
    fun testPlace_equality() {
        val place1 = Place("City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)
        val place2 = Place("City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)

        assertEquals(place1, place2)
    }

    @Test
    fun testPlace_inequality_name() {
        val place1 = Place("City1", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)
        val place2 = Place("City2", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)

        assertNotEquals(place1, place2)
    }

    @Test
    fun testPlace_inequality_coordinates() {
        val place1 = Place("City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)
        val place2 = Place("City", 40.1, -74.0, "USA", PlaceType.CITY, 1000000)

        assertNotEquals(place1, place2)
    }

    @Test
    fun testPlace_copy() {
        val original = Place("City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)
        val copy = original.copy(population = 2000000)

        assertEquals(original.name, copy.name)
        assertEquals(original.latitude, copy.latitude, 0.0001)
        assertEquals(original.longitude, copy.longitude, 0.0001)
        assertEquals(original.country, copy.country)
        assertEquals(original.type, copy.type)
        assertNotEquals(original.population, copy.population)
        assertEquals(2000000, copy.population)
    }

    @Test
    fun testPlaceType_values() {
        // Ensure all PlaceType enum values are available
        assertNotNull(PlaceType.CITY)
        assertNotNull(PlaceType.LANDMARK)
        assertNotNull(PlaceType.CAPITAL)
        assertNotNull(PlaceType.OCEAN)
    }

    @Test
    fun testPlace_validCoordinates() {
        // Test with extreme valid coordinates
        val northPole = Place("North Pole", 90.0, 0.0, "Arctic")
        val southPole = Place("South Pole", -90.0, 0.0, "Antarctica")
        val dateLine = Place("Date Line", 0.0, 180.0, "Ocean", PlaceType.OCEAN)

        assertEquals(90.0, northPole.latitude, 0.0001)
        assertEquals(-90.0, southPole.latitude, 0.0001)
        assertEquals(180.0, dateLine.longitude, 0.0001)
    }

    @Test
    fun testPlace_toString() {
        val place = Place("Test City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)
        val stringRep = place.toString()

        // toString should contain key information
        assertTrue(stringRep.contains("Test City"))
        assertTrue(stringRep.contains("40.0"))
        assertTrue(stringRep.contains("-74.0"))
    }

    @Test
    fun testPlace_hashCode() {
        val place1 = Place("City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)
        val place2 = Place("City", 40.0, -74.0, "USA", PlaceType.CITY, 1000000)

        // Equal objects should have same hash code
        assertEquals(place1.hashCode(), place2.hashCode())
    }

    @Test
    fun testPlace_largePopulation() {
        val megacity = Place("Megacity", 35.0, 139.0, "Japan", PlaceType.CITY, 37400000)

        assertEquals(37400000, megacity.population)
    }

    @Test
    fun testPlace_negativeCoordinates() {
        val place = Place("Southern City", -33.8688, -151.2093, "Country", PlaceType.CITY)

        assertTrue(place.latitude < 0)
        assertTrue(place.longitude < 0)
    }
}
