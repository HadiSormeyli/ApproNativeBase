package com.approagency.base.model.showcase

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.toSize

class ShowcaseScopeImpl(
    private val topPadding: Float
) :
    ShowcaseScope {
    val showcaseItems = mutableStateMapOf<Int, ShowcaseData>()

    @Composable
    override fun Showcase(
        index: Int,
        message: ShowcaseMsg?,
        itemContent: @Composable () -> Unit
    ) {
        Box(
            modifier = Modifier.showcase(
                index = index,
                message = message
            )
        ) {
            itemContent()
        }
    }

    @Composable
    override fun Modifier.showcase(index: Int, message: ShowcaseMsg?): Modifier {
        val statusBarPadding = topPadding

        return this.onGloballyPositioned {
            if (it.isAttached) {
                val adjustedPosition =
                    it.localToScreen(Offset(0f, -statusBarPadding))
                showcaseItems[index] = ShowcaseData(it.size, adjustedPosition, message)
            }
        }
    }

    fun getSizeFor(index: Int): Size {
        return showcaseItems[index]?.size?.toSize() ?: Size(0f, 0f)
    }

    fun getPositionFor(index: Int): Offset {
        return showcaseItems[index]?.position ?: Offset(0f, 0f)
    }

    override fun getMessageFor(index: Int): ShowcaseMsg? {
        return showcaseItems[index]?.message
    }
}


interface ShowcaseScope {
    @Composable
    fun Showcase(index: Int, message: ShowcaseMsg?, itemContent: @Composable () -> Unit)


    @Composable
    fun Modifier.showcase(index: Int, message: ShowcaseMsg?): Modifier

    fun getMessageFor(index: Int): ShowcaseMsg?
}