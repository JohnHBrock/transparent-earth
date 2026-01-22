package com.earthxray.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface
import android.view.WindowManager

class OrientationManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    var azimuth: Float = 0f
        private set
    var pitch: Float = 0f
        private set
    var roll: Float = 0f
        private set

    private var listener: OrientationListener? = null

    interface OrientationListener {
        fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float)
    }

    fun setOrientationListener(listener: OrientationListener) {
        this.listener = listener
    }

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, gravity, 0, event.values.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, geomagnetic, 0, event.values.size)
            }
        }

        val success = SensorManager.getRotationMatrix(
            rotationMatrix, null, gravity, geomagnetic
        )

        if (success) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val rotation = display.rotation

            val remappedRotation = FloatArray(9)
            when (rotation) {
                Surface.ROTATION_0 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        remappedRotation
                    )
                }
                Surface.ROTATION_90 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_Z, SensorManager.AXIS_MINUS_X,
                        remappedRotation
                    )
                }
                Surface.ROTATION_180 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_X, SensorManager.AXIS_MINUS_Z,
                        remappedRotation
                    )
                }
                Surface.ROTATION_270 -> {
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_MINUS_Z, SensorManager.AXIS_X,
                        remappedRotation
                    )
                }
                else -> {
                    System.arraycopy(rotationMatrix, 0, remappedRotation, 0, 9)
                }
            }

            SensorManager.getOrientation(remappedRotation, orientationAngles)

            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            pitch = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            roll = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

            if (azimuth < 0) azimuth += 360f

            listener?.onOrientationChanged(azimuth, pitch, roll)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    fun isPointingDown(): Boolean {
        return pitch < -45
    }

    fun getDeviceDirection(): Float {
        return azimuth
    }
}
