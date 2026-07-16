package com.approagency.base.model.user

import com.google.gson.annotations.SerializedName

data class UserStatus(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("first_name") val firstName: String? = null,
    @SerializedName("last_name") val lastName: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("mobile") val mobile: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("wallet") val wallet: Long? = null,
    @SerializedName("products") val products: List<Product>? = null
) {
    val isSubscribed: Boolean get() = !products.isNullOrEmpty()

    val subscriptionTitle: String? get() = products?.firstOrNull()?.title

    val subscriptionExpireAt: String? get() = products?.firstOrNull()?.resolvedExpireAt

    val displayName: String?
        get() = fullName?.takeIf { it.isNotBlank() }
            ?: listOfNotNull(firstName, lastName).joinToString(" ").takeIf { it.isNotBlank() }
}
