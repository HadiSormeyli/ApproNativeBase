package com.approagency.base.utils

import android.util.Log
import com.approagency.base.config.ApproConfig

object Logger {

    @Volatile
    private var config: ApproConfig? = null

    val enabled: Boolean
        get() = config?.debug == true

    fun initialize(
        config: ApproConfig
    ) {
        this.config = config
    }

    fun verbose(
        tag: String,
        message: String
    ) {
        if (enabled) {
            Log.v(tag, message)
        }
    }

    fun debug(
        tag: String,
        message: String
    ) {
        if (enabled) {
            Log.d(tag, message)
        }
    }

    fun info(
        tag: String,
        message: String
    ) {
        if (enabled) {
            Log.i(tag, message)
        }
    }

    fun warning(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        if (!enabled) return

        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    fun error(
        tag: String,
        message: String,
        throwable: Throwable? = null
    ) {
        if (!enabled) return

        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }

    fun maskToken(
        token: String?
    ): String {
        if (token.isNullOrBlank()) {
            return "null"
        }

        if (token.length <= 16) {
            return "***"
        }

        return "${token.take(8)}...${token.takeLast(8)}"
    }
}