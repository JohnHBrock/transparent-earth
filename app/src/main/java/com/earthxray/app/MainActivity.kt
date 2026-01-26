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
import com.earthxray.app.data.PlacesApiManager
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
    private lateinit var placesApiManager: PlacesApiManager

    private var camera: Camera? = null
    private var currentLocation: Location? = null
    private var targetPlace: Place? = null
    private var minPopulation: Int = 0

    private val CAMERA_PERMISSION_REQUEST = 100
    private val LOCATION_PERMISSION_REQUEST = 101

    private val populationThresholds = listOf(
        0,          // All cities
        10_000,     // 10k
        50_000,     // 50k
        100_000,    // 100k
        250_000,    // 250k
        500_000,    // 500k
        1_000_000,  // 1M
        2_500_000,  // 2.5M
        5_000_000   // 5M+
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        placesApiManager = PlacesApiManager(this)
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

        binding.populationSlider.setOnSeekBarChangeListener(object : android.widget.SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: android.widget.SeekBar?, progress: Int, fromUser: Boolean) {
                minPopulation = populationThresholds[progress]
                updatePopulationLabel()
                updateVisiblePlaces()
            }

            override fun onStartTrackingTouch(seekBar: android.widget.SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: android.widget.SeekBar?) {}
        })

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

        // Search asynchronously in both static DB and Google Places API
        lifecycleScope.launch {
            try {
                val results = placesApiManager.searchAllSources(query)

                if (results.isEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        "No results found for '$query'",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                // Handle the first result
                val firstResult = results.first()
                val place = when (firstResult) {
                    is PlacesApiManager.SearchResult.StaticPlace -> {
                        // Result from static database - already have coordinates
                        firstResult.place
                    }
                    is PlacesApiManager.SearchResult.ApiPrediction -> {
                        // Result from Google Places API - need to fetch details
                        placesApiManager.getPlaceDetails(firstResult.getPlaceId())
                            ?: run {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Could not load place details",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@launch
                            }
                    }
                }

                targetPlace = place

                currentLocation?.let { loc ->
                    // Calculate direction to point through Earth to see this place
                    val direction = GeoUtils.calculateDirectionToPointThroughEarth(
                        loc.latitude, loc.longitude,
                        place.latitude, place.longitude
                    )

                    val surfaceDistance = GeoUtils.calculateDistance(
                        loc.latitude, loc.longitude,
                        place.latitude, place.longitude
                    )

                    val straightLineDistance = GeoUtils.calculateStraightLineDistance(
                        loc.latitude, loc.longitude,
                        place.latitude, place.longitude
                    )

                    direction?.let { dir ->
                        binding.arOverlayView.setTargetPlace(place, dir.azimuth, dir.pitch)

                        binding.locationNameText.text = place.name
                        binding.locationDetailsText.text = "${place.country} • ${place.type.name.lowercase()}"
                        binding.distanceText.text = GeoUtils.formatBothDistances(surfaceDistance, straightLineDistance)
                        binding.bottomPanel.visibility = android.view.View.VISIBLE

                        binding.infoText.text = "Point your phone to see ${place.name} through Earth"
                        binding.clearButton.visibility = android.view.View.VISIBLE

                        hideKeyboard()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Search error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun clearSearch() {
        targetPlace = null
        binding.searchEditText.text.clear()
        binding.arOverlayView.setTargetPlace(null, 0.0, 0.0)
        binding.bottomPanel.visibility = android.view.View.GONE
        binding.clearButton.visibility = android.view.View.GONE
        binding.infoText.text = getString(R.string.point_at_ground)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun updatePopulationLabel() {
        val text = when (minPopulation) {
            0 -> "Min Population: All Cities"
            in 1..999 -> "Min Population: $minPopulation"
            in 1_000..999_999 -> "Min Population: ${minPopulation / 1_000}k"
            else -> "Min Population: ${minPopulation / 1_000_000}M"
        }
        binding.populationLabel.text = text
    }

    private fun updateVisiblePlaces() {
        val location = currentLocation ?: return

        // Get current orientation
        val azimuth = orientationManager.azimuth.toDouble()
        val pitch = orientationManager.pitch.toDouble()

        // Find all places visible in current field of view
        val placesInView = GeoUtils.findPlacesInView(
            location.latitude,
            location.longitude,
            azimuth,
            pitch,
            PlacesDatabase.places,
            fieldOfViewDegrees = 60.0,
            minPopulation = minPopulation
        )

        // Limit to top 20 by population to avoid clutter
        binding.arOverlayView.updateVisiblePlaces(
            placesInView.take(20),
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
