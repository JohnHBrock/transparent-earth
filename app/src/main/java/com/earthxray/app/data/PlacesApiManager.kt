package com.earthxray.app.data

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.libraries.places.api.model.Place as GooglePlace
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.tasks.await

class PlacesApiManager(context: Context) {
    private val placesClient: PlacesClient

    init {
        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(context, context.packageName)
        }
        placesClient = Places.createClient(context)
    }

    /**
     * Fetch nearby places from Google Places API
     * @param latitude User's current latitude
     * @param longitude User's current longitude
     * @param radiusMeters Search radius in meters (max 50000)
     * @return List of Place objects from nearby locations
     */
    suspend fun fetchNearbyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Double = 50000.0
    ): List<Place> {
        try {
            val center = LatLng(latitude, longitude)
            val circle = CircularBounds.newInstance(center, radiusMeters)

            // Specify the place fields to return
            val placeFields = listOf(
                GooglePlace.Field.NAME,
                GooglePlace.Field.LAT_LNG,
                GooglePlace.Field.TYPES,
                GooglePlace.Field.USER_RATINGS_TOTAL
            )

            // Build the search request for cities and notable places
            val searchNearbyRequest = SearchNearbyRequest.builder(circle, placeFields)
                .setIncludedTypes(listOf(
                    "locality",           // Cities
                    "administrative_area_level_3", // Towns
                    "tourist_attraction", // Tourist attractions
                    "landmark",          // Landmarks
                    "point_of_interest"  // Points of interest
                ))
                .setMaxResultCount(20)
                .build()

            val response = placesClient.searchNearby(searchNearbyRequest).await()

            // Convert Google Places to our Place objects
            return response.places.mapNotNull { googlePlace ->
                val latLng = googlePlace.latLng ?: return@mapNotNull null
                val name = googlePlace.name ?: return@mapNotNull null

                // Determine place type based on Google Place types
                val placeType = when {
                    googlePlace.placeTypes?.contains("locality") == true -> PlaceType.CITY
                    googlePlace.placeTypes?.contains("tourist_attraction") == true -> PlaceType.LANDMARK
                    googlePlace.placeTypes?.contains("landmark") == true -> PlaceType.LANDMARK
                    else -> PlaceType.CITY
                }

                // Use ratings count as a proxy for population/importance
                val population = googlePlace.userRatingsTotal ?: 0

                Place(
                    name = name,
                    latitude = latLng.latitude,
                    longitude = latLng.longitude,
                    type = placeType,
                    population = population
                )
            }
        } catch (e: Exception) {
            // Return empty list on error (e.g., no API key, network failure)
            return emptyList()
        }
    }

    /**
     * Merge API places with our static database, removing duplicates
     */
    fun mergePlaces(apiPlaces: List<Place>, staticPlaces: List<Place>): List<Place> {
        val merged = mutableListOf<Place>()
        merged.addAll(staticPlaces)

        // Add API places that aren't too close to existing places
        for (apiPlace in apiPlaces) {
            val isDuplicate = staticPlaces.any { staticPlace ->
                // Consider it a duplicate if within 10km and similar name
                val distance = com.earthxray.app.utils.GeoUtils.calculateDistance(
                    apiPlace.latitude, apiPlace.longitude,
                    staticPlace.latitude, staticPlace.longitude
                )
                distance < 10.0 && staticPlace.name.contains(apiPlace.name, ignoreCase = true)
            }

            if (!isDuplicate) {
                merged.add(apiPlace)
            }
        }

        return merged
    }
}
