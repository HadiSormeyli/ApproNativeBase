package com.approagency.base.model.ui.deepLink

fun interface DeepLinkParser {
    fun parse(input: DeepLinkInput): DeepLinkTarget?
}