package com.approagency.base.model.ui

import com.google.gson.annotations.SerializedName

data class Promotion(
    @SerializedName("id") val id: Int,
    @SerializedName("type") val type: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("image") val image: String,
    @SerializedName("action_text") val actionText: String,
    @SerializedName("url_myket") val urlMyket: String?,
    @SerializedName("url_bazzar") val urlBazzar: String?,
    @SerializedName("url_google_play") val urlGooglePlay: String?,
    @SerializedName("url_site") val urlSite: String?,
    @SerializedName("is_active") val isActive: Boolean,
    @SerializedName("priority") val priority: Int,
    @SerializedName("image_url") val imageUrl: String
)
