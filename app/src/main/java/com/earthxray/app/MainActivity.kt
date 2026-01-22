package com.earthxray.app

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Camera
import android.location.Location
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.earthxray.app.data.Place
import com.earthxray.app.data.PlacesDatabase
import com.earthxray.app.databinding.ActivityMainBinding
import com.earthxray.app.sensors.LocationTracker
import com.earthxray.app.sensors.OrientationManager
import com.earthxray.app.utils.GeoUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var orientationManager: OrientationManager
    private lateinit var locationTracker: LocationTracker

    private var camera: Camera? = null
    private var currentLocation: Location? = null
    private var targetPlace: Place? = null

    private val CAMERA_PERMISSION_REQUEST = 100
    private val LOCATION_PERMISSION_REQUEST = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSensors()
        setupUI()
        requestPermissions()
    }

    private fun setupSensors() {
        orientationManager = OrientationManager(this).apply {
            setOrientationListener(object : OrientationManager.OrientationListener {
                override fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float) {
                    binding.arOverlayView.updateOrientation(azimuth, pitch)
                    updateVisiblePlaces()
                }
            })
        }

        locationTracker = LocationTracker(this).apply {
            setLocationUpdateListener(object : LocationTracker.LocationUpdateListener {
                override fun onLocationUpdated(location: Location) {
                    currentLocation = location
                    updateVisiblePlaces()
                }
            })
        }
    }

    private fun setupUI() {
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        binding.searchButton.setOnClickListener {
            performSearch()
        }

        binding.clearButton.setOnClickListener {
            clearSearch()
        }

        binding.surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                startCamera(holder)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                stopCamera()
            }
        })
    }

    private fun performSearch() {
        val query = binding.searchEditText.text.toString().trim()
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show()
            return
        }

        val results = PlacesDatabase.searchPlaces(query)
        if (results.isEmpty()) {
            Toast.makeText(this, "No results found for '$query'", Toast.LENGTH_SHORT).show()
            return
        }

        val place = results.first()
        targetPlace = place

        currentLocation?.let { loc ->
            val bearing = GeoUtils.calculateBearing(
                loc.latitude, loc.longitude,
                place.latitude, place.longitude
            )
            val distance = GeoUtils.calculateDistance(
                loc.latitude, loc.longitude,
                place.latitude, place.longitude
            )

            binding.arOverlayView.setTargetPlace(place, bearing)

            binding.locationNameText.text = place.name
            binding.locationDetailsText.text = "${place.country} • ${place.type.name.lowercase()}"
            binding.distanceText.text = "Distance: ${GeoUtils.formatDistance(distance)}"
            binding.bottomPanel.visibility = android.view.View.VISIBLE

            binding.infoText.text = getString(R.string.searching)
            binding.clearButton.visibility = android.view.View.VISIBLE

            hideKeyboard()
        }
    }

    private fun clearSearch() {
        targetPlace = null
        binding.searchEditText.text.clear()
        binding.arOverlayView.setTargetPlace(null, 0.0)
        binding.bottomPanel.visibility = android.view.View.GONE
        binding.clearButton.visibility = android.view.View.GONE
        binding.infoText.text = getString(R.string.point_at_ground)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun updateVisiblePlaces() {
        val location = currentLocation ?: return

        if (!orientationManager.isPointingDown() && targetPlace == null) {
            return
        }

        val antipodal = GeoUtils.calculateAntipodal(location.latitude, location.longitude)

        val nearbyPlaces = GeoUtils.findNearbyPlaces(
            antipodal.latitude,
            antipodal.longitude,
            PlacesDatabase.places,
            radiusKm = 1000.0
        )

        binding.arOverlayView.updateVisiblePlaces(
            nearbyPlaces.take(10),
            location.latitude,
            location.longitude
        )
    }

    private fun startCamera(holder: SurfaceHolder) {
        try {
            camera = Camera.open()
            camera?.setDisplayOrientation(90)

            val parameters = camera?.parameters
            parameters?.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO

            val supportedSizes = parameters?.supportedPreviewSizes
            supportedSizes?.let { sizes ->
                val optimalSize = getOptimalPreviewSize(sizes, holder.surfaceFrame.width(), holder.surfaceFrame.height())
                parameters?.setPreviewSize(optimalSize.width, optimalSize.height)
            }

            camera?.parameters = parameters
            camera?.setPreviewDisplay(holder)
            camera?.startPreview()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getOptimalPreviewSize(sizes: List<Camera.Size>, w: Int, h: Int): Camera.Size {
        val targetRatio = h.toDouble() / w
        var optimalSize = sizes[0]
        var minDiff = Double.MAX_VALUE

        for (size in sizes) {
            val ratio = size.width.toDouble() / size.height
            if (Math.abs(ratio - targetRatio) > 0.2) continue

            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }

        return optimalSize
    }

    private fun stopCamera() {
        camera?.stopPreview()
        camera?.release()
        camera = null
    }

    private fun requestPermissions() {
        val permissions = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.CAMERA)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissions.toTypedArray(),
                CAMERA_PERMISSION_REQUEST
            )
        } else {
            startTracking()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startTracking()
        } else {
            Toast.makeText(
                this,
                "Camera and location permissions are required",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun startTracking() {
        orientationManager.start()
        locationTracker.startTracking()

        lifecycleScope.launch {
            delay(1000)
            currentLocation?.let {
                Toast.makeText(
                    this@MainActivity,
                    "Location acquired. Point at ground to see through Earth!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            orientationManager.start()
            locationTracker.startTracking()
        }
    }

    override fun onPause() {
        super.onPause()
        orientationManager.stop()
        locationTracker.stopTracking()
        stopCamera()
    }
}
