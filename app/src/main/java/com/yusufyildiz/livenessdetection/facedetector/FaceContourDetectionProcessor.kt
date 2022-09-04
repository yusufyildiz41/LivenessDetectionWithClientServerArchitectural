package com.yusufyildiz.livenessdetection.facedetector

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.IOException

class FaceContourDetectionProcessor(
    val context: Context,
    val view: GraphicOverlay,
    val imageView: ImageView,

    ) : BaseImageAnalyzer<List<Face>>() {

    var messageSuccess = Toast.makeText(context, "Yüz Tespit Edildi", Toast.LENGTH_LONG)
    var warningMessage =
        Toast.makeText(context, "Lütfen Yüzünüzü Ortalayın !!!", Toast.LENGTH_SHORT)

    var detectionCounter = 0
    var clickCounter = 0
    val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .build()

    val detector = FaceDetection.getClient(realTimeOpts)

    override val graphicOverlay: GraphicOverlay
        get() = view

    override fun detectInImage(image: InputImage): Task<List<Face>> {
        return detector.process(image)
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            println("errror")
        }
    }

    override fun onSuccess(
        results: List<Face>, graphicOverlay: GraphicOverlay, rect: Rect
    ) {
        graphicOverlay.clear()
        results.forEach { face ->
            detectionCounter++
            if (detectionCounter >= 5) {

                imageView.isGone = false
                imageView.isVisible = true

                graphicOverlay.clear()
                val faceGraphic = FaceContourGraphic(graphicOverlay, face, rect)
                graphicOverlay.add(faceGraphic)
                if (face.boundingBox.height() > (graphicOverlay.height * 30) / 100) {
                    warningMessage.show()
                    messageSuccess.cancel()
                    imageView.isGone = true
                    imageView.isVisible = false
                } else {
                    messageSuccess.show()
                    warningMessage.cancel()
                    imageView.isVisible = true
                    imageView.isGone = false
                }
            }
        }
        if (results.isEmpty()) {
            messageSuccess.cancel()
            warningMessage.cancel()
            detectionCounter = 0
            imageView.isGone = true
            imageView.isVisible = false
        }
        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.w(TAG, "Face Detector failed.$e")
        graphicOverlay.clear()
    }

    companion object {
        private const val TAG = "FaceDetectorProcessor"
    }

}