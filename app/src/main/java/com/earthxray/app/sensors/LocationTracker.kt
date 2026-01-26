package com.earthxray.app.sensors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationTracker(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var currentLocation: Location? = null
    private var locationCallback: LocationCallback? = null
    private var listener: LocationUpdateListener? = null

    interface LocationUpdateListener {
        fun onLocationUpdated(location: Location)
    }

    fun setLocationUpdateListener(listener: LocationUpdateListener) {
        this.listener = listener
    }

    fun getCurrentLocation(): Location? = currentLocation

    fun startTracking() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000
        ).apply {
            setMinUpdateIntervalMillis(2000)
            setMaxUpdateDelayMillis(10000)
        }.build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    currentLocation = location
                    listener?.onLocationUpdated(location)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                currentLocation = it
                listener?.onLocationUpdated(it)
            }
        }
    }

    fun stopTracking() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }
}
