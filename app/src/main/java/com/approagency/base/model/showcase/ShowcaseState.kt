package com.approagency.base.model.showcase

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import com.approagency.base.local.preference.PreferencesHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Stable
class ShowcaseState(
    shouldShowcasing: Boolean,
    val animationDuration: Int,
    private val lastIndex: Int,
    private val key: Preferences.Key<Int>,
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val showcaseScope: ShowcaseScope
) {
    var isShowcasing by mutableStateOf(false)
    var currentIndex by mutableIntStateOf(0)
    var isArrowDelayOver by mutableStateOf(false)

    val pathPortion = Animatable(0f)
    val animMsgTextAlpha = Animatable(0f)
    val animMsgAlpha = Animatable(0f)
    val animArrow = Animatable(0f)
    val animArrowHead = Animatable(0f)

    init {
        coroutineScope.launch {
            PreferencesHelper.readFlow(key, lastIndex + 1)
                .collectLatest { storedIndex ->
                    val initIndex = (storedIndex - 1).coerceAtLeast(0)
                    val canShowcase = shouldShowcasing && initIndex < lastIndex
                    isShowcasing = canShowcase
                    currentIndex = initIndex.coerceAtLeast(1)
                    if (canShowcase) {
                        triggerEnterAnimations(getMessageFor(currentIndex))
                    }
                }
        }
    }

    fun getMessageFor(index: Int): ShowcaseMsg? {
        return showcaseScope.getMessageFor(index)
    }

    fun handleTap(message: ShowcaseMsg?) {
        coroutineScope.launch {
            triggerExitAnimations(message)
            if (currentIndex < lastIndex) {
                currentIndex++
                triggerEnterAnimations(getMessageFor(currentIndex))
            } else {
                dismiss(resetIndex = true)
            }
        }
    }

    fun next() {
        if (currentIndex <= lastIndex) {
            currentIndex = (currentIndex + 1).coerceAtMost(lastIndex + 1)
            if (currentIndex == lastIndex + 1) {
                dismiss(resetIndex = false)
            }
        }
    }

    fun previous() {
        if (currentIndex > 1) {
            currentIndex = (currentIndex - 1).coerceAtLeast(1)
        }
    }

    fun dismiss(resetIndex: Boolean) {
        isShowcasing = false
        PreferencesHelper.write(key, lastIndex + 1)
        if (resetIndex) {
            coroutineScope.launch {
                val resetDelay = animationDuration.toLong() + MsgAnimation.RESET_DELAY
                delay(resetDelay)
                currentIndex = lastIndex + 1
            }
        } else {
            currentIndex = lastIndex + 1
        }
    }

    private suspend fun triggerEnterAnimations(message: ShowcaseMsg?) {
        isArrowDelayOver = false

        message?.let { msg ->
            when (msg.enterAnim) {
                is MsgAnimation.FadeInOut -> {
                    val duration = msg.enterAnim.duration
                    animMsgAlpha.animateTo(1f, tween(duration))
                    animMsgTextAlpha.animateTo(1f, tween(duration))
                }

                is MsgAnimation.None -> {
                    animMsgAlpha.snapTo(1f)
                    animMsgTextAlpha.snapTo(1f)
                }
            }

            if (msg.arrow != null) {
                delay(animationDuration.toLong())
                isArrowDelayOver = true
                val arrowAnimDuration = msg.arrow.animationDuration
                coroutineScope.launch { pathPortion.animateTo(1f, tween(arrowAnimDuration)) }
                coroutineScope.launch {
                    if (!msg.arrow.animSize) animArrowHead.snapTo(msg.arrow.headSize)
                    animArrow.animateTo(1f, tween(arrowAnimDuration))
                    if (msg.arrow.animSize) animArrowHead.animateTo(
                        msg.arrow.headSize,
                        tween(arrowAnimDuration)
                    )
                }
            }
        }
    }

    private suspend fun triggerExitAnimations(message: ShowcaseMsg?) {
        message?.arrow?.let { arrow ->
            val duration = arrow.animationDuration / 2
            coroutineScope.launch {
                if (arrow.animSize) animArrowHead.animateTo(0f, tween(duration))
            }
            pathPortion.animateTo(0f - Float.MIN_VALUE, tween(duration))
        }

        message?.let { msg ->
            when (msg.exitAnim) {
                is MsgAnimation.FadeInOut -> {
                    val duration = msg.exitAnim.duration
                    animMsgTextAlpha.animateTo(0f, tween(duration))
                    animMsgAlpha.animateTo(0f, tween(duration))
                }

                is MsgAnimation.None -> {
                    animMsgTextAlpha.snapTo(0f)
                    animMsgAlpha.snapTo(0f)
                }
            }
        }
    }
}
