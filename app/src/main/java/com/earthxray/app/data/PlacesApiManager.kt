package com.earthxray.app.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place as GooglePlace
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await

class PlacesApiManager(context: Context) {
    private val placesClient: PlacesClient
    private var sessionToken: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()

    init {
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(context, context.packageName)
        }
        placesClient = Places.createClient(context)
    }

    /**
     * Search for places by name using Google Places Autocomplete
     * @param query User's search query
     * @return List of autocomplete predictions with place IDs
     */
    suspend fun searchPlaces(query: String): List<AutocompletePrediction> {
        if (query.isBlank()) return emptyList()

        try {
            // Build autocomplete request - searches entire world
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(sessionToken)
                .setQuery(query)
                .build()

            val response = placesClient.findAutocompletePredictions(request).await()
            return response.autocompletePredictions
        } catch (e: Exception) {
            // Return empty list on error (e.g., no API key, network failure)
            return emptyList()
        }
    }

    /**
     * Fetch detailed place information by place ID
     * @param placeId Google Place ID from autocomplete
     * @return Place object with coordinates, or null if not found
     */
    suspend fun getPlaceDetails(placeId: String): Place? {
        try {
            // Specify the fields we need
            val placeFields = listOf(
                GooglePlace.Field.NAME,
                GooglePlace.Field.LAT_LNG,
                GooglePlace.Field.TYPES
            )

            // Build fetch place request
            val request = FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(sessionToken)
                .build()

            val response = placesClient.fetchPlace(request).await()
            val googlePlace = response.place

            val latLng = googlePlace.latLng ?: return null
            val name = googlePlace.name ?: return null

            // Generate new session token after fetching place details
            sessionToken = AutocompleteSessionToken.newInstance()

            // Determine place type based on Google Place types
            val placeType = when {
                googlePlace.placeTypes?.any {
                    it.contains("locality") ||
                    it.contains("administrative_area") ||
                    it.contains("political")
                } == true -> PlaceType.CITY
                else -> PlaceType.LANDMARK
            }

            return Place(
                name = name,
                latitude = latLng.latitude,
                longitude = latLng.longitude,
                type = placeType,
                population = 0 // Unknown population for API results
            )
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Search in both static database and Google Places API
     * @param query User's search query
     * @return Combined list of places from static DB and API
     */
    suspend fun searchAllSources(query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        // Search static database first (instant results)
        val staticResults = PlacesDatabase.searchPlaces(query)
        results.addAll(staticResults.map { SearchResult.StaticPlace(it) })

        // Search Google Places API (requires network)
        try {
            val apiPredictions = searchPlaces(query)
            results.addAll(apiPredictions.map { SearchResult.ApiPrediction(it) })
        } catch (e: Exception) {
            // Silently fail - static results are still available
        }

        return results
    }

    /**
     * Sealed class representing a search result that could come from either source
     */
    sealed class SearchResult {
        data class StaticPlace(val place: Place) : SearchResult() {
            fun getDisplayName(): String = place.name
        }

        data class ApiPrediction(val prediction: AutocompletePrediction) : SearchResult() {
            fun getDisplayName(): String = prediction.getFullText(null).toString()
            fun getPlaceId(): String = prediction.placeId
        }
    }
}
