package com.approagency.base.firebase

data class FirebaseMessage(
    val title: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val data: Map<String, String> = emptyMap()
)