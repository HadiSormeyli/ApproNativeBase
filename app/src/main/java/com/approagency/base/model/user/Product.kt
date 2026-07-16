package com.approagency.base.model.user

import com.approagency.base.model.session.Session
import com.approagency.base.paymnet.PaymentProductType
import com.approagency.base.paymnet.PaymentRequest
import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("id") val id: Int,
    @SerializedName("package_name_id") val packageNameId: Int? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("price") val price: Long? = null,
    @SerializedName("discounted_price") val discountedPrice: String? = null,
    @SerializedName("daily_price") val dailyPrice: String? = null,
    @SerializedName("discount") val discount: String? = null,
    @SerializedName("is_best_seller") val isBestSelling: Boolean? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("descriptions") val descriptions: String? = null,
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("expire_at") val expireAt: String? = null,
    @SerializedName("pivot") val pivot: ProductPivot? = null
) {
    val resolvedExpireAt: String?
        get() = pivot?.expiresAt ?: pivot?.expireAt ?: expiresAt ?: expireAt
}

data class ProductPivot(
    @SerializedName("expires_at") val expiresAt: String? = null,
    @SerializedName("expire_at") val expireAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)


fun Product.toPaymentRequest(
    sessionId: String = Session.ID,
    payload: String? = null
) = PaymentRequest(
    productId = id,
    productUuid = uuid,
    type = PaymentProductType.SUBSCRIPTION,
    sessionId = sessionId,
    payload = payload
)