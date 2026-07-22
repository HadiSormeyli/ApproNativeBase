package com.approagency.base.model.ui.deepLink

data class DeepLinkEvent(
    val id: Long,
    val input: DeepLinkInput,
    val target: DeepLinkTarget? = null
)