package com.yusufyildiz.livenessdetection.facedetector


import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.yusufyildiz.livenessdetection.facedetector.GraphicOverlay
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    val context: Context,
    val finderView: PreviewView,
    val lifecycleOwner: LifecycleOwner,
    val graphicOverlay: GraphicOverlay,
    val imageView: ImageView
) {

    var preview: Preview? = null

    var camera: Camera? = null
    lateinit var cameraExecutor: ExecutorService
    var cameraSelectorOption = CameraSelector.LENS_FACING_FRONT
    var cameraProvider: ProcessCameraProvider? = null

    lateinit var cameraManager: CameraManager

    var imageAnalyzer: ImageAnalysis? = null

    init {
        createNewExecutor()
    }

    fun createNewExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
    }


    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            Runnable {
                cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder()
                    .build()

                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, selectAnalyzer())
                    }


                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(cameraSelectorOption)
                    .build()

                setCameraConfig(cameraProvider, cameraSelector)


            }, ContextCompat.getMainExecutor(context)
        )
    }


    fun selectAnalyzer(): ImageAnalysis.Analyzer {
        return FaceContourDetectionProcessor(context, graphicOverlay, imageView)
    }


    fun setCameraConfig(
        cameraProvider: ProcessCameraProvider?,
        cameraSelector: CameraSelector
    ) {

        try {
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                //imageCapture,
                imageAnalyzer
            )
            preview?.setSurfaceProvider( // The Preview use case needs a Surface to display the incoming preview frames it receives from the camera.
                finderView.surfaceProvider
            )


        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    companion object {
        const val TAG = "CameraXBasic"
    }


}