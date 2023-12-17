package com.bangkit.huggingpet.api

import com.bangkit.huggingpet.*
import com.bangkit.huggingpet.dataclass.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {
    @POST("register")
    fun registUser(@Body requestRegister: RegisterDataAccount): Call<ResponseDetail>

    @POST("login")
    fun loginUser(@Body requestLogin: LoginDataAccount): Call<ResponseLogin>

//    @GET("stories")
//    fun getLocationStory(
//        @Query("size") size: Int? = null,
//        @Query("location") location: Int? = 0,
//        @Header("Authorization") token: String,
//    ): Call<ResponseLocationStory>

    @GET("pets")
    suspend fun getPagingPet(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Header("Authorization") token: String,
    ): ResponsePagingPet

    @Multipart
    @POST("pets")
    fun addPet(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Header("Authorization") token: String
    ): Call<ResponseDetail>
}