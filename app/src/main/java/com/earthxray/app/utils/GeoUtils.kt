package com.earthxray.app.utils

import android.location.Location
import com.earthxray.app.data.Place
import kotlin.math.*

object GeoUtils {
    const val EARTH_RADIUS_KM = 6371.0

    data class Antipodal(
        val latitude: Double,
        val longitude: Double
    )

    fun calculateAntipodal(latitude: Double, longitude: Double): Antipodal {
        val antipodalLat = -latitude
        val antipodalLon = if (longitude >= 0) {
            longitude - 180
        } else {
            longitude + 180
        }
        return Antipodal(antipodalLat, antipodalLon)
    }

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLat = Math.toRadians(lat2 - lat1)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    fun calculateBearing(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val lat1Rad = Math.toRadians(lat1)
        val lat2Rad = Math.toRadians(lat2)
        val deltaLon = Math.toRadians(lon2 - lon1)

        val y = sin(deltaLon) * cos(lat2Rad)
        val x = cos(lat1Rad) * sin(lat2Rad) -
                sin(lat1Rad) * cos(lat2Rad) * cos(deltaLon)

        val bearing = Math.toDegrees(atan2(y, x))
        return (bearing + 360) % 360
    }

    fun findNearbyPlaces(
        centerLat: Double,
        centerLon: Double,
        places: List<Place>,
        radiusKm: Double = 500.0
    ): List<Pair<Place, Double>> {
        return places.mapNotNull { place ->
            val distance = calculateDistance(
                centerLat, centerLon,
                place.latitude, place.longitude
            )
            if (distance <= radiusKm) {
                Pair(place, distance)
            } else {
                null
            }
        }.sortedBy { it.second }
    }

    data class Vector3D(val x: Double, val y: Double, val z: Double) {
        fun normalize(): Vector3D {
            val mag = sqrt(x * x + y * y + z * z)
            return if (mag > 0) Vector3D(x / mag, y / mag, z / mag) else this
        }

        operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)

        fun dot(other: Vector3D) = x * other.x + y * other.y + z * other.z
    }

    fun latLonToVector(lat: Double, lon: Double): Vector3D {
        val latRad = Math.toRadians(lat)
        val lonRad = Math.toRadians(lon)

        val x = cos(latRad) * cos(lonRad)
        val y = cos(latRad) * sin(lonRad)
        val z = sin(latRad)

        return Vector3D(x, y, z)
    }

    fun formatDistance(distanceKm: Double): String {
        return when {
            distanceKm < 1 -> "${(distanceKm * 1000).toInt()} m"
            distanceKm < 10 -> "%.1f km".format(distanceKm)
            else -> "${distanceKm.toInt()} km"
        }
    }
}
