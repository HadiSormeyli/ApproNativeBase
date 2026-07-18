package com.approagency.base.network.service

import com.approagency.base.model.Promotion
import com.approagency.base.model.user.Product
import com.approagency.base.model.user.UserResponse
import com.approagency.base.model.user.UserStatus
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApproService {
    @GET("promotions/")
    suspend fun getPromotions(): List<Promotion>

    @FormUrlEncoded
    @POST("auth/login-otp/")
    suspend fun login(
        @Field("mobile") mobile: String,
        @Field("package_name") packageName: String,
    ): ResponseBody

    @FormUrlEncoded
    @POST("auth/check-otp/")
    suspend fun checkOtp(
        @Field("mobile") mobile: String,
        @Field("token") token: String
    ): UserResponse

    @GET("status/")
    suspend fun getStatus(
        @Query("package_name") packageName: String
    ): UserStatus

    @GET("package-names/{name}/products/")
    suspend fun getProducts(
        @Path("name") packageName: String
    ): List<Product>

    @PUT("package-names/{name}/products/{productId}/subscribe")
    suspend fun subscribeProduct(
        @Path("name") packageName: String,
        @Path("productId") productId: Int,
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @POST("profile/")
    suspend fun sendFCMToken(
        @Query("fcm_token") fcmToken: String,
        @Query("package_name") packageName: String,
    ): ResponseBody
}