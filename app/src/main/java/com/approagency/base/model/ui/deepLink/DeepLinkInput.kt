package com.approagency.base.model.ui.deepLink

import android.net.Uri

data class DeepLinkInput(
    val uri: Uri,
    val data: Map<String, String> = emptyMap()
)