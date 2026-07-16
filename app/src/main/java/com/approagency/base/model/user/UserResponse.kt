package com.approagency.base.model.user

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("user")
    val user: User,

    @SerializedName("token")
    val token: String
)
