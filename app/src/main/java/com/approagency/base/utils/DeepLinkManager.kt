package com.approagency.base.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.approagency.base.config.ApproConfig
import com.approagency.base.model.ui.deepLink.DeepLinkEvent
import com.approagency.base.model.ui.deepLink.DeepLinkInput
import com.approagency.base.model.ui.deepLink.DeepLinkParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.context.GlobalContext.get
import java.util.concurrent.atomic.AtomicLong

/**
 * Example parser:
 *
 * class AppDeepLinkParser : DeepLinkParser {
 *     override fun parse(input: DeepLinkInput): DeepLinkTarget? {
 *         return when (input.uri.host) {
 *             "home" -> DeepLinkTarget(
 *                 route = Route.Home,
 *                 navigationType = DeepLinkNavigationType.CLEAR_STACK
 *             )
 *
 *             "settings" -> DeepLinkTarget(
 *                 route = Route.Settings,
 *                 navigationType = DeepLinkNavigationType.SINGLE_TOP
 *             )
 *
 *             "product" -> {
 *                 val productId = input.uri.lastPathSegment
 *                     ?: input.data["product_id"]
 *                     ?: return null
 *
 *                 DeepLinkTarget(
 *                     route = Route.Product(productId),
 *                     navigationType = DeepLinkNavigationType.PUSH
 *                 )
 *             }
 *
 *             else -> null
 *         }
 *     }
 * }
 *
 * deepLinkManager.initialize(AppDeepLinkParser())
 */
class DeepLinkManager(
    private val config: ApproConfig
) {
    private val eventId = AtomicLong(0)

    private var parser: DeepLinkParser? = null

    private val _events = MutableSharedFlow<DeepLinkEvent>(
        replay = 1,
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val events: SharedFlow<DeepLinkEvent> =
        _events.asSharedFlow()

    val isInitialized: Boolean
        get() = parser != null

    fun initialize(parser: DeepLinkParser) {
        this.parser = parser
    }

    fun handle(
        link: String?,
        data: Map<String, String> = emptyMap()
    ): Boolean {
        if (link.isNullOrBlank()) {
            return false
        }

        return handle(
            DeepLinkInput(
                uri = link.toUri(),
                data = data
            )
        )
    }

    fun handle(input: DeepLinkInput): Boolean {
        val target = parser?.parse(input)
            ?: return false

        return _events.tryEmit(
            DeepLinkEvent(
                id = eventId.incrementAndGet(),
                input = input,
                target = target
            )
        )
    }

    fun handle(intent: Intent?): Boolean {
        val input = intent?.toDeepLinkInput()
            ?: return false

        return handle(input)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun consume(eventId: Long) {
        val event = _events.replayCache.lastOrNull()
            ?: return

        if (event.id == eventId) {
            _events.resetReplayCache()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clear() {
        _events.resetReplayCache()
    }

    companion object {
        fun createLink(
            route: String,
            pathParameters: List<Any?> = emptyList(),
            queryParameters: Map<String, Any?> = emptyMap(),
            config: ApproConfig = get().get<ApproConfig>()
        ): String {
            require(config.deepLink.isNotBlank()) {
                "Deep link must not be empty"
            }

            return config.deepLink.toUri()
                .buildUpon()
                .apply {
                    route
                        .trim('/')
                        .split('/')
                        .filter(String::isNotBlank)
                        .forEach(::appendPath)

                    pathParameters.forEach { value ->
                        value?.let {
                            appendPath(it.toString())
                        }
                    }

                    queryParameters.forEach { (key, value) ->
                        value?.let {
                            appendQueryParameter(key, it.toString())
                        }
                    }
                }
                .build()
                .toString()
        }

        fun createDeepLinkIntent(
            context: Context,
            route: String,
            pathParameters: List<Any?> = emptyList(),
            queryParameters: Map<String, Any?> = emptyMap(),
            config: ApproConfig = get().get<ApproConfig>()
        ): PendingIntent {
            val deepLinkIntent =
                Intent(
                    Intent.ACTION_VIEW,
                    createLink(route, pathParameters, queryParameters, config).toUri()
                ).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }

            return PendingIntent.getActivity(
                context,
                0,
                deepLinkIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}