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

        operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)

        operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)

        fun dot(other: Vector3D) = x * other.x + y * other.y + z * other.z

        fun cross(other: Vector3D) = Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )

        fun magnitude() = sqrt(x * x + y * y + z * z)
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

    /**
     * Calculate where a ray through the Earth exits on the other side
     * based on user's location and phone orientation
     *
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param azimuth Compass heading in degrees (0 = North, 90 = East)
     * @param pitch Tilt angle in degrees (negative = pointing down)
     * @return The lat/lon where the ray exits Earth, or null if pointing up/horizontally
     */
    fun calculateRayExitPoint(
        userLat: Double,
        userLon: Double,
        azimuth: Double,
        pitch: Double
    ): Antipodal? {
        // Only process if pointing downward
        if (pitch > -10) return null

        // Convert user position to 3D point on unit sphere
        val userPos = latLonToVector(userLat, userLon)

        // Create local coordinate system at user's position
        // North vector: tangent to sphere pointing north
        val north = Vector3D(
            -sin(Math.toRadians(userLat)) * cos(Math.toRadians(userLon)),
            -sin(Math.toRadians(userLat)) * sin(Math.toRadians(userLon)),
            cos(Math.toRadians(userLat))
        ).normalize()

        // East vector: tangent to sphere pointing east
        val east = Vector3D(
            -sin(Math.toRadians(userLon)),
            cos(Math.toRadians(userLon)),
            0.0
        ).normalize()

        // Down vector: toward center of Earth (negative of position vector)
        val down = userPos.times(-1.0).normalize()

        // Calculate direction vector based on azimuth and pitch
        // Azimuth rotates in the horizontal plane (north-east)
        // Pitch rotates from horizontal down toward center
        val azimuthRad = Math.toRadians(azimuth)
        val pitchRad = Math.toRadians(pitch)

        // Horizontal component (in north-east plane)
        val horizontalMag = cos(pitchRad)
        val northComponent = horizontalMag * cos(azimuthRad)
        val eastComponent = horizontalMag * sin(azimuthRad)

        // Vertical component (down)
        val downComponent = -sin(pitchRad)

        // Combine to get direction vector in local coordinates
        val direction = (north.times(northComponent))
            .plus(east.times(eastComponent))
            .plus(down.times(downComponent))
            .normalize()

        // The ray goes through Earth's center, so the exit point is just
        // the opposite direction from the entry direction
        // If we're at position P and pointing in direction D (into Earth),
        // the exit point is in direction -D from origin
        val exitVector = direction.times(-1.0)

        // Convert exit vector back to lat/lon
        return vectorToLatLon(exitVector)
    }

    /**
     * Convert a 3D unit vector to latitude and longitude
     */
    fun vectorToLatLon(v: Vector3D): Antipodal {
        val normalized = v.normalize()

        val lat = Math.toDegrees(asin(normalized.z))
        val lon = Math.toDegrees(atan2(normalized.y, normalized.x))

        return Antipodal(lat, lon)
    }

    /**
     * Calculate the angle between where the user is currently pointing
     * and where they need to point to see a specific place through Earth
     *
     * @return Angular distance in degrees
     */
    fun calculateAngularDistance(
        userLat: Double,
        userLon: Double,
        currentAzimuth: Double,
        currentPitch: Double,
        targetLat: Double,
        targetLon: Double
    ): Double {
        // Calculate where user is currently pointing
        val currentExit = calculateRayExitPoint(userLat, userLon, currentAzimuth, currentPitch)
            ?: return Double.MAX_VALUE

        // Calculate angular distance between current exit point and target
        return calculateDistance(currentExit.latitude, currentExit.longitude, targetLat, targetLon) / EARTH_RADIUS_KM
    }

    /**
     * Find all places that are visible when looking through Earth from a specific position and orientation
     */
    fun findPlacesAlongRay(
        userLat: Double,
        userLon: Double,
        azimuth: Double,
        pitch: Double,
        places: List<Place>,
        toleranceDegrees: Double = 5.0
    ): List<Pair<Place, Double>> {
        val exitPoint = calculateRayExitPoint(userLat, userLon, azimuth, pitch)
            ?: return emptyList()

        // Find places near the exit point
        return findNearbyPlaces(
            exitPoint.latitude,
            exitPoint.longitude,
            places,
            radiusKm = toleranceDegrees * EARTH_RADIUS_KM * PI / 180.0
        )
    }

    data class PointingDirection(
        val azimuth: Double,
        val pitch: Double
    )

    /**
     * Calculate what direction (azimuth and pitch) the user needs to point
     * to see a specific location through the Earth
     *
     * @param userLat User's latitude
     * @param userLon User's longitude
     * @param targetLat Target location's latitude
     * @param targetLon Target location's longitude
     * @return The azimuth and pitch to point at, or null if same location
     */
    fun calculateDirectionToPointThroughEarth(
        userLat: Double,
        userLon: Double,
        targetLat: Double,
        targetLon: Double
    ): PointingDirection? {
        // Convert positions to 3D vectors
        val userPos = latLonToVector(userLat, userLon)
        val targetPos = latLonToVector(targetLat, targetLon)

        // The direction to point is toward the target position
        // (which will exit at the target on the other side)
        val direction = (targetPos.times(-1.0)).normalize()

        // Create local coordinate system at user's position
        val north = Vector3D(
            -sin(Math.toRadians(userLat)) * cos(Math.toRadians(userLon)),
            -sin(Math.toRadians(userLat)) * sin(Math.toRadians(userLon)),
            cos(Math.toRadians(userLat))
        ).normalize()

        val east = Vector3D(
            -sin(Math.toRadians(userLon)),
            cos(Math.toRadians(userLon)),
            0.0
        ).normalize()

        val down = userPos.times(-1.0).normalize()

        // Project direction onto local coordinate system
        val northComp = direction.dot(north)
        val eastComp = direction.dot(east)
        val downComp = direction.dot(down)

        // Calculate azimuth (compass direction)
        val azimuth = Math.toDegrees(atan2(eastComp, northComp))
        val normalizedAzimuth = (azimuth + 360) % 360

        // Calculate pitch (negative = down)
        val horizontalMag = sqrt(northComp * northComp + eastComp * eastComp)
        val pitch = -Math.toDegrees(atan2(downComp, horizontalMag))

        return PointingDirection(normalizedAzimuth, pitch)
    }
}
