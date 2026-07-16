package com.approagency.base.model.user

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int,

    @SerializedName("first_name")
    val firstName: String?,

    @SerializedName("last_name")
    val lastName: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("mobile")
    val mobile: String?,

    @SerializedName("avatar")
    val avatar: String?,

    @SerializedName("uuid")
    val uuid: String,

    @SerializedName("is_admin")
    val isAdmin: Boolean,

    @SerializedName("wallet")
    val wallet: Int,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("birthday")
    val birthday: String?,

    @SerializedName("gender")
    val gender: Int?,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String?,

    @SerializedName("remember_token")
    val rememberToken: String?,

    @SerializedName("mobile_verified_at")
    val mobileVerifiedAt: String?,

    @SerializedName("referral_code")
    val referralCode: String?,

    @SerializedName("referred_by")
    val referredBy: String?,

    @SerializedName("referral_subscription_granted_at")
    val referralSubscriptionGrantedAt: String?,

    @SerializedName("age")
    val age: Int?,

    @SerializedName("full_name")
    val fullName: String?
)
