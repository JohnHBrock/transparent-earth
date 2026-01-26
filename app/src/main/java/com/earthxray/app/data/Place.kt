package com.earthxray.app.data

data class Place(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val type: PlaceType = PlaceType.CITY,
    val population: Int = 0
)

enum class PlaceType {
    CITY,
    LANDMARK,
    CAPITAL,
    OCEAN
}
