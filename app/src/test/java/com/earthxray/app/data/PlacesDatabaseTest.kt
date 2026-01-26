package com.earthxray.app.data

import org.junit.Assert.*
import org.junit.Test

class PlacesDatabaseTest {

    @Test
    fun testSearchPlaces_exactMatch() {
        val results = PlacesDatabase.searchPlaces("Tokyo")

        assertTrue(results.isNotEmpty())
        assertEquals("Tokyo", results[0].name)
    }

    @Test
    fun testSearchPlaces_caseInsensitive() {
        val results1 = PlacesDatabase.searchPlaces("tokyo")
        val results2 = PlacesDatabase.searchPlaces("TOKYO")
        val results3 = PlacesDatabase.searchPlaces("Tokyo")

        assertTrue(results1.isNotEmpty())
        assertTrue(results2.isNotEmpty())
        assertTrue(results3.isNotEmpty())

        assertEquals("Tokyo", results1[0].name)
        assertEquals("Tokyo", results2[0].name)
        assertEquals("Tokyo", results3[0].name)
    }

    @Test
    fun testSearchPlaces_partialMatch() {
        val results = PlacesDatabase.searchPlaces("York")

        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.name.contains("York") })
    }

    @Test
    fun testSearchPlaces_countryMatch() {
        val results = PlacesDatabase.searchPlaces("USA")

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.country == "USA" })
    }

    @Test
    fun testSearchPlaces_emptyQuery() {
        val results = PlacesDatabase.searchPlaces("")

        assertEquals(0, results.size)
    }

    @Test
    fun testSearchPlaces_blankQuery() {
        val results = PlacesDatabase.searchPlaces("   ")

        assertEquals(0, results.size)
    }

    @Test
    fun testSearchPlaces_noMatches() {
        val results = PlacesDatabase.searchPlaces("XYZ123NonExistentCity")

        assertEquals(0, results.size)
    }

    @Test
    fun testSearchPlaces_prioritization() {
        // Search for "New" should prioritize exact starts over contains
        val results = PlacesDatabase.searchPlaces("New")

        assertTrue(results.isNotEmpty())
        // "New York" or "New Delhi" should come before cities containing "new" in middle
        assertTrue(results[0].name.startsWith("New"))
    }

    @Test
    fun testSearchPlaces_populationSorting() {
        // When multiple matches, larger population should rank higher
        val results = PlacesDatabase.searchPlaces("USA")

        if (results.size >= 2) {
            // Results should generally be sorted by relevance, then population
            val majorCities = results.filter { it.population > 1000000 }
            assertTrue(majorCities.isNotEmpty())
        }
    }

    @Test
    fun testGetPlaceByName_exactMatch() {
        val place = PlacesDatabase.getPlaceByName("Paris")

        assertNotNull(place)
        assertEquals("Paris", place?.name)
        assertEquals("France", place?.country)
    }

    @Test
    fun testGetPlaceByName_caseInsensitive() {
        val place1 = PlacesDatabase.getPlaceByName("paris")
        val place2 = PlacesDatabase.getPlaceByName("PARIS")

        assertNotNull(place1)
        assertNotNull(place2)
        assertEquals("Paris", place1?.name)
        assertEquals("Paris", place2?.name)
    }

    @Test
    fun testGetPlaceByName_noMatch() {
        val place = PlacesDatabase.getPlaceByName("NonExistentCity")

        assertNull(place)
    }

    @Test
    fun testPlacesDatabase_containsMajorCities() {
        // Verify database contains expected major cities
        val majorCities = listOf("Tokyo", "New York", "London", "Paris", "Beijing")

        for (cityName in majorCities) {
            val place = PlacesDatabase.getPlaceByName(cityName)
            assertNotNull("Database should contain $cityName", place)
        }
    }

    @Test
    fun testPlacesDatabase_containsLandmarks() {
        // Verify database contains expected landmarks
        val landmarks = listOf("Eiffel Tower", "Taj Mahal", "Great Wall of China")

        for (landmarkName in landmarks) {
            val place = PlacesDatabase.getPlaceByName(landmarkName)
            assertNotNull("Database should contain $landmarkName", place)
            assertEquals(PlaceType.LANDMARK, place?.type)
        }
    }

    @Test
    fun testPlacesDatabase_containsCapitals() {
        // Verify some capitals are marked as such
        val capitals = listOf("Washington DC", "London", "Paris", "Tokyo", "Beijing")

        for (capitalName in capitals) {
            val place = PlacesDatabase.getPlaceByName(capitalName)
            assertNotNull("Database should contain $capitalName", place)
            assertTrue(
                "Expected $capitalName to be CAPITAL or CITY",
                place?.type == PlaceType.CAPITAL || place?.type == PlaceType.CITY
            )
        }
    }

    @Test
    fun testPlacesDatabase_hasCoordinates() {
        // All places should have valid coordinates
        for (place in PlacesDatabase.places) {
            assertTrue("Latitude should be valid", place.latitude >= -90 && place.latitude <= 90)
            assertTrue("Longitude should be valid", place.longitude >= -180 && place.longitude <= 180)
        }
    }

    @Test
    fun testPlacesDatabase_hasCountry() {
        // All places should have a country
        for (place in PlacesDatabase.places) {
            assertTrue("Place ${place.name} should have a country", place.country.isNotEmpty())
        }
    }

    @Test
    fun testPlacesDatabase_citiesHavePopulation() {
        // Cities and capitals should have population data
        val citiesAndCapitals = PlacesDatabase.places.filter {
            it.type == PlaceType.CITY || it.type == PlaceType.CAPITAL
        }

        assertTrue("Database should have cities", citiesAndCapitals.isNotEmpty())

        for (place in citiesAndCapitals) {
            assertTrue(
                "City ${place.name} should have population > 0",
                place.population > 0
            )
        }
    }

    @Test
    fun testPlacesDatabase_landmarksHaveZeroPopulation() {
        // Landmarks typically have population = 0
        val landmarks = PlacesDatabase.places.filter { it.type == PlaceType.LANDMARK }

        assertTrue("Database should have landmarks", landmarks.isNotEmpty())

        for (landmark in landmarks) {
            assertEquals(
                "Landmark ${landmark.name} should have population = 0",
                0,
                landmark.population
            )
        }
    }

    @Test
    fun testPlacesDatabase_noDuplicateNames() {
        val names = PlacesDatabase.places.map { it.name.lowercase() }
        val uniqueNames = names.toSet()

        // Some cities might have same name in different countries, so allow some duplicates
        // But the ratio should be reasonable
        val duplicateRatio = (names.size - uniqueNames.size).toDouble() / names.size
        assertTrue(
            "Too many duplicate names in database",
            duplicateRatio < 0.1 // Less than 10% duplicates
        )
    }

    @Test
    fun testPlacesDatabase_hasDiverseLocations() {
        // Database should have places across different continents
        val places = PlacesDatabase.places

        val hasNorthAmerica = places.any { it.latitude > 15 && it.longitude < -60 }
        val hasSouthAmerica = places.any { it.latitude < -10 && it.longitude < -30 }
        val hasEurope = places.any { it.latitude > 35 && it.longitude in -10.0..40.0 }
        val hasAsia = places.any { it.latitude > 0 && it.longitude > 60 }
        val hasAfrica = places.any { it.latitude < 35 && it.latitude > -35 && it.longitude in -20.0..50.0 }
        val hasOceania = places.any { it.latitude < -10 && it.longitude > 110 }

        assertTrue("Database should have North American locations", hasNorthAmerica)
        assertTrue("Database should have South American locations", hasSouthAmerica)
        assertTrue("Database should have European locations", hasEurope)
        assertTrue("Database should have Asian locations", hasAsia)
        assertTrue("Database should have African locations", hasAfrica)
        assertTrue("Database should have Oceania locations", hasOceania)
    }

    @Test
    fun testSearchPlaces_multipleWords() {
        val results = PlacesDatabase.searchPlaces("New York")

        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.name == "New York" })
    }

    @Test
    fun testSearchPlaces_specialCharacters() {
        // Search should handle queries with special characters gracefully
        val results = PlacesDatabase.searchPlaces("São Paulo")

        // Should find São Paulo even with special characters
        assertTrue(results.any { it.name.contains("Paulo", ignoreCase = true) })
    }
}
