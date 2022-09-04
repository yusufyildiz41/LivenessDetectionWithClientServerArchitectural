package com.yusufyildiz.livenessdetection.facedetector

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.camera.core.CameraSelector
import kotlin.math.ceil

class GraphicOverlay(context: Context?,attrs:AttributeSet?) : View(context,attrs) {

    private val lock = Any()
    private val graphics : MutableList<Graphic> = ArrayList()
    var mScale: Float?=null
    var mOffsetX: Float?=null
    var mOffsetY: Float?=null
    var cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA


    abstract class Graphic(private val overlay: GraphicOverlay)
    {
        abstract fun draw(canvas: Canvas?)

        fun calculateRect(height : Float,width : Float, boundingBoxT : Rect): RectF{

            fun isLandScapeMode() : Boolean{
                return overlay.context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            }
            fun whenLandScapeModeWith():Float{
                return when(isLandScapeMode()){
                    true -> width
                    false -> height
                }
            }

            fun whenLandScapeModeHeight():Float{
                return when(isLandScapeMode()){
                    true -> height
                    false -> width
                }
            }

            val scaleX = overlay.width.toFloat() / whenLandScapeModeWith()
            val scaleY = overlay.height.toFloat() / whenLandScapeModeHeight()
            val scale = scaleX.coerceAtLeast(scaleY)
            overlay.mScale = scale

            val offsetX = (overlay.width.toFloat() - ceil(whenLandScapeModeWith()*scale)) / 2.0f
            val offsetY = (overlay.height.toFloat() - ceil(whenLandScapeModeHeight() * scale)) / 2.0f
            overlay.mOffsetX = offsetX
            overlay.mOffsetY = offsetY

            val mappedBox = RectF().apply {
                left = boundingBoxT.right * scale + offsetX
                top = boundingBoxT.top * scale + offsetY
                right = boundingBoxT.left * scale + offsetX
                bottom = boundingBoxT.bottom * scale + offsetY
            }

            // for front mode
            if (overlay.isFrontMode()) {
                val centerX = overlay.width.toFloat() / 2
                mappedBox.apply {
                    left = centerX + (centerX - left)
                    right = centerX - (right - centerX)
                }
            }
            return mappedBox

        }
    }

    fun isFrontMode() = cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA

    fun toggleSelector() {
        cameraSelector =
            if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
    }
    fun clear() {
        synchronized(lock) { graphics.clear() }
        postInvalidate()
    }
    fun add(graphic: Graphic) {
        synchronized(lock) { graphics.add(graphic) }
    }
    fun remove(graphic: Graphic) {
        synchronized(lock) { graphics.remove(graphic) }
        postInvalidate()
    }
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        synchronized(lock) {
            for (graphic in graphics) {
                graphic.draw(canvas)
            }
        }
    }




}