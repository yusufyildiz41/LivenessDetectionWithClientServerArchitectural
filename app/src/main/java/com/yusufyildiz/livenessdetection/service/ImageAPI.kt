package com.yusufyildiz.livenessdetection.service

import com.yusufyildiz.livenessdetection.model.Image
import retrofit2.http.*

interface ImageAPI {

    //GET, POST
    // base url -> http://192.168.1.232:5000

    @POST("/image")
    @Headers("Content-Type: application/json")
    fun postBase64String(@Body image: Image): retrofit2.Call<String>


}