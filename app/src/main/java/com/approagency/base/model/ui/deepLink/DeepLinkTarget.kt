package com.approagency.base.model.ui.deepLink

data class DeepLinkTarget(
    val route: Any,
    val navigationType: DeepLinkNavigationType = DeepLinkNavigationType.PUSH
)