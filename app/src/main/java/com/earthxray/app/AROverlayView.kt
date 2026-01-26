package com.earthxray.app

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.earthxray.app.data.Place
import com.earthxray.app.utils.GeoUtils
import kotlin.math.*

class AROverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }

    private val smallTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 28f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        setShadowLayer(4f, 0f, 0f, Color.BLACK)
    }

    private val pinPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val pinStrokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    private val arrowPaint = Paint().apply {
        color = Color.parseColor("#00FF00")
        style = Paint.Style.FILL
        isAntiAlias = true
        setShadowLayer(8f, 0f, 0f, Color.BLACK)
    }

    private val arrowStrokePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    data class VisiblePlace(
        val place: Place,
        val screenX: Float,
        val screenY: Float,
        val surfaceDistance: Double,
        val straightLineDistance: Double,
        val azimuth: Double,
        val pitch: Double
    )

    private var visiblePlaces = listOf<VisiblePlace>()
    private var userLatitude: Double = 0.0
    private var userLongitude: Double = 0.0
    private var targetPlace: Place? = null
    private var targetAzimuth: Double = 0.0
    private var targetPitch: Double = 0.0
    private var currentAzimuth: Float = 0f
    private var currentPitch: Float = 0f
    private var fieldOfView: Float = 60f

    fun updateOrientation(azimuth: Float, pitch: Float) {
        this.currentAzimuth = azimuth
        this.currentPitch = pitch
        invalidate()
    }

    fun updateVisiblePlaces(
        places: List<GeoUtils.PlaceWithDistances>,
        userLat: Double,
        userLon: Double
    ) {
        this.userLatitude = userLat
        this.userLongitude = userLon

        visiblePlaces = places.mapNotNull { placeWithDistances ->
            // Calculate direction to point to see this place through Earth
            val direction = GeoUtils.calculateDirectionToPointThroughEarth(
                userLat, userLon,
                placeWithDistances.place.latitude, placeWithDistances.place.longitude
            ) ?: return@mapNotNull null

            val screenPos = calculateScreenPosition(direction.azimuth, direction.pitch)

            screenPos?.let {
                VisiblePlace(
                    placeWithDistances.place,
                    it.first,
                    it.second,
                    placeWithDistances.surfaceDistance,
                    placeWithDistances.straightLineDistance,
                    direction.azimuth,
                    direction.pitch
                )
            }
        }
        invalidate()
    }

    fun setTargetPlace(place: Place?, azimuth: Double, pitch: Double) {
        this.targetPlace = place
        this.targetAzimuth = azimuth
        this.targetPitch = pitch
        invalidate()
    }

    private fun calculateScreenPosition(bearing: Double, elevation: Double): Pair<Float, Float>? {
        val deltaBearing = normalizeDegrees(bearing - currentAzimuth)

        if (abs(deltaBearing) > fieldOfView / 2) {
            return null
        }

        val horizontalFactor = deltaBearing / (fieldOfView / 2)
        val screenX = (width / 2) + (horizontalFactor * width / 2).toFloat()

        val deltaPitch = elevation - currentPitch
        val verticalFactor = deltaPitch / (fieldOfView / 2)
        val screenY = (height / 2) - (verticalFactor * height / 2).toFloat()

        return Pair(screenX, screenY)
    }

    private fun normalizeDegrees(angle: Double): Double {
        var normalized = angle
        while (normalized > 180) normalized -= 360
        while (normalized < -180) normalized += 360
        return normalized
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (visiblePlaces.isNotEmpty() && currentPitch < -45) {
            drawVisiblePlaces(canvas)
        }

        targetPlace?.let {
            drawTargetArrow(canvas)
        }

        drawCrosshair(canvas)
    }

    private fun drawVisiblePlaces(canvas: Canvas) {
        val sortedPlaces = visiblePlaces
            .filter { it.screenX in 0f..width.toFloat() }
            .sortedByDescending { it.surfaceDistance }

        for (vp in sortedPlaces) {
            drawPin(canvas, vp.screenX, vp.screenY)

            val distanceText = GeoUtils.formatBothDistances(vp.surfaceDistance, vp.straightLineDistance)
            canvas.drawText(vp.place.name, vp.screenX, vp.screenY - 40, textPaint)
            canvas.drawText(distanceText, vp.screenX, vp.screenY - 10, smallTextPaint)
        }
    }

    private fun drawPin(canvas: Canvas, x: Float, y: Float) {
        val pinPath = Path().apply {
            moveTo(x, y)
            lineTo(x - 15, y - 40)
            lineTo(x + 15, y - 40)
            close()
        }

        canvas.drawCircle(x, y - 50, 20f, pinPaint)
        canvas.drawCircle(x, y - 50, 20f, pinStrokePaint)
        canvas.drawPath(pinPath, pinPaint)
        canvas.drawPath(pinPath, pinStrokePaint)
    }

    private fun drawTargetArrow(canvas: Canvas) {
        val deltaAzimuth = normalizeDegrees(targetAzimuth - currentAzimuth)
        val deltaPitch = targetPitch - currentPitch

        val isOnScreen = abs(deltaAzimuth) <= fieldOfView / 2 && abs(deltaPitch) <= fieldOfView / 2

        if (!isOnScreen) {
            // Draw arrow at edge of screen pointing in the right direction
            val centerX = width / 2f
            val centerY = height / 2f

            // Calculate position based on both horizontal and vertical offset
            val horizontalFactor = deltaAzimuth.coerceIn(-fieldOfView / 2, fieldOfView / 2) / (fieldOfView / 2)
            val verticalFactor = deltaPitch.coerceIn(-fieldOfView / 2, fieldOfView / 2) / (fieldOfView / 2)

            val arrowX = centerX + (horizontalFactor * width / 2.5).toFloat()
            val arrowY = centerY - (verticalFactor * height / 2.5).toFloat()

            val arrowSize = 60f
            val arrowPath = Path()

            // Point arrow in the direction of the target
            if (abs(deltaAzimuth) > abs(deltaPitch)) {
                // Horizontal arrow (left or right)
                if (deltaAzimuth > 0) {
                    arrowPath.moveTo(arrowX, arrowY)
                    arrowPath.lineTo(arrowX - arrowSize, arrowY - arrowSize / 2)
                    arrowPath.lineTo(arrowX - arrowSize, arrowY + arrowSize / 2)
                } else {
                    arrowPath.moveTo(arrowX, arrowY)
                    arrowPath.lineTo(arrowX + arrowSize, arrowY - arrowSize / 2)
                    arrowPath.lineTo(arrowX + arrowSize, arrowY + arrowSize / 2)
                }
            } else {
                // Vertical arrow (up or down)
                if (deltaPitch > 0) {
                    arrowPath.moveTo(arrowX, arrowY)
                    arrowPath.lineTo(arrowX - arrowSize / 2, arrowY + arrowSize)
                    arrowPath.lineTo(arrowX + arrowSize / 2, arrowY + arrowSize)
                } else {
                    arrowPath.moveTo(arrowX, arrowY)
                    arrowPath.lineTo(arrowX - arrowSize / 2, arrowY - arrowSize)
                    arrowPath.lineTo(arrowX + arrowSize / 2, arrowY - arrowSize)
                }
            }
            arrowPath.close()

            canvas.drawPath(arrowPath, arrowPaint)
            canvas.drawPath(arrowPath, arrowStrokePaint)

            targetPlace?.let {
                val textX = arrowX
                val textY = arrowY + 100
                canvas.drawText(it.name, textX, textY, smallTextPaint)

                val angle = sqrt(deltaAzimuth * deltaAzimuth + deltaPitch * deltaPitch).toInt()
                canvas.drawText("${angle}° away", textX, textY + 35, smallTextPaint)
            }
        } else {
            val screenPos = calculateScreenPosition(targetAzimuth, targetPitch)
            screenPos?.let { (x, y) ->
                val arrowSize = 80f
                val arrowPath = Path().apply {
                    moveTo(x, y)
                    lineTo(x - arrowSize / 2, y + arrowSize)
                    lineTo(x + arrowSize / 2, y + arrowSize)
                    close()
                }

                canvas.drawPath(arrowPath, arrowPaint)
                canvas.drawPath(arrowPath, arrowStrokePaint)

                targetPlace?.let {
                    canvas.drawText(it.name, x, y - 20, textPaint)
                }
            }
        }
    }

    private fun drawCrosshair(canvas: Canvas) {
        val centerX = width / 2f
        val centerY = height / 2f
        val size = 30f

        val crosshairPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
            alpha = 150
        }

        canvas.drawCircle(centerX, centerY, size, crosshairPaint)
        canvas.drawLine(centerX - size - 10, centerY, centerX - size, centerY, crosshairPaint)
        canvas.drawLine(centerX + size, centerY, centerX + size + 10, centerY, crosshairPaint)
        canvas.drawLine(centerX, centerY - size - 10, centerX, centerY - size, crosshairPaint)
        canvas.drawLine(centerX, centerY + size, centerX, centerY + size + 10, crosshairPaint)
    }
}
