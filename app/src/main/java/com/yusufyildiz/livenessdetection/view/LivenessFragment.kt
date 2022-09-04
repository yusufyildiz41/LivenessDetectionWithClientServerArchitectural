package com.yusufyildiz.livenessdetection.view

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.gson.GsonBuilder
import com.yusufyildiz.livenessdetection.R
import com.yusufyildiz.livenessdetection.databinding.FragmentLivenessBinding
import com.yusufyildiz.livenessdetection.facedetector.CameraManager
import com.yusufyildiz.livenessdetection.model.Image
import com.yusufyildiz.livenessdetection.service.ImageAPI
import com.yusufyildiz.livenessdetection.util.Constants
import io.ktor.client.request.*
import kotlinx.android.synthetic.main.fail_dialog.*
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.ByteArrayOutputStream


class LivenessFragment : Fragment() {

    private lateinit var binding: FragmentLivenessBinding

    private lateinit var cameraManager: CameraManager
    lateinit var dialog: Dialog
    lateinit var imageModel: Image
    lateinit var retrofit: Retrofit
    lateinit var retrofitInterface: ImageAPI


    val BASE_URL = "http://192.168.1.232:5000" // 192.168.1.45

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLivenessBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog = Dialog(requireContext())

        if (allPermissionGranted()) {
                createCameraManager().apply {
                    cameraManager.startCamera()
                }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                Constants.REQUIRED_PERMISSIONS,
                Constants.REQUEST_CODE_PERMISSIONS
            )
        }

        val client = OkHttpClient.Builder().build()
        val gson = GsonBuilder()
            .setLenient()
            .create()
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
        retrofitInterface = retrofit.create(ImageAPI::class.java)
        binding.imageCapture.setOnClickListener {
            if (binding.imageCapture.isVisible) {
                val bitmap2 = binding.previewView.bitmap
                binding.imageView.setImageBitmap(bitmap2)
                imageToBase64String(bitmap2!!).also {
                    cameraManager.cameraProvider!!.unbindAll()
                }
            }
        }
    }
    fun postBase64String(imageString: String) {
        try {
            Log.e(Constants.TAG, "my new image stringg : ${imageString}")
            //Log.e(Constants.TAG,"imageString : : ${imageString}")
            imageModel = Image(imageString)
            val call = retrofitInterface.postBase64String(imageModel)
            call.enqueue(object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    if (response.body() == "OK") {
                        showSuccessDialog()
                        //Toast.makeText(applicationContext,"Canlılık Algılandı",Toast.LENGTH_SHORT).show()
                        var result = response.body()
                        if (result != null) {

                            Log.e(Constants.TAG2, "my result is : ${result}")

                        } else{
                            Log.e(Constants.TAG, "result is null")
                        }
                    }
                    else if(response.body() == "NOTOK")
                    {
                        showFailDialog()
                    }
                    else
                    {
                        showImageIsNotReceivedDialog()
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    showImageIsNotReceivedDialog()
                    Log.e(Constants.TAG, "my errorr message : ${t.message}")
                }

            })

        } catch (e: Exception) {
            e.localizedMessage?.let { Log.e(Constants.TAG, it) }
        }
    }

    private fun createCameraManager() {

        cameraManager = CameraManager(
            requireContext(),
            binding.previewView,
            this,
            binding.graphicOverlay,
            binding.imageCapture
        )
    }


    fun imageToBase64String(bitmap: Bitmap) {

        // Bitmap Convert to Base64 String
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val imageString = Base64.encodeToString(byteArray, Base64.DEFAULT)
        Log.e(Constants.TAG, "image string : $imageString")

        postBase64String(imageString)


        // Base64 String Convert to Bitmap
        //val imageBytes = Base64.decode(imageString, Base64.DEFAULT)
        //val decodeImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        //binding.imageCapture.setImageBitmap(decodeImage)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

//        if(requestCode == Constants.REQUEST_CODE_PERMISSIONS)
//        {
//            if(allPermissionGranted())
//            {
//                //startCamera()
//            }
//            else
//            {
//                Toast.makeText(this,"Izin Verilmedi",Toast.LENGTH_LONG).show()
//                finish()
//            }
//        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }
    private fun allPermissionGranted() =
        Constants.REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                this.requireActivity().baseContext, it
            ) == PackageManager.PERMISSION_GRANTED
        }
    fun showSuccessDialog() {
        dialog.setContentView(R.layout.success_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.show()
    }
    fun showFailDialog() {
        dialog.setContentView(R.layout.fail_dialog)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.show()
    }

    fun showImageIsNotReceivedDialog()
    {
        dialog.setContentView(R.layout.image_not_found)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        dialog.show()
    }
}