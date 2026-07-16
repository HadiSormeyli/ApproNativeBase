package com.approagency.base.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateSizeAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.datastore.preferences.core.Preferences
import com.approagency.base.R
import com.approagency.base.model.showcase.Arrow
import com.approagency.base.model.showcase.Gravity
import com.approagency.base.model.showcase.Head
import com.approagency.base.model.showcase.MsgAnimation
import com.approagency.base.model.showcase.ShowcaseMsg
import com.approagency.base.model.showcase.ShowcaseScope
import com.approagency.base.model.showcase.ShowcaseScopeImpl
import com.approagency.base.model.showcase.ShowcaseState
import com.approagency.base.model.showcase.Side
import com.approagency.base.utils.calculateStatusBarPadding
import com.approagency.base.utils.heightDp
import com.approagency.base.utils.toPx
import com.approagency.base.utils.widthDp
import kotlin.math.PI
import kotlin.math.atan2

@Composable
fun ShowcaseLayout(
    key: Preferences.Key<Int>,
    lastIndex: Int,
    lineThickness: Dp = 2.dp,
    shouldShowcasing: Boolean = true,
    animationDuration: Int = MsgAnimation.DEFAULT_DURATION,
    variantColor: Color = MaterialTheme.colorScheme.outlineVariant,
    topPadding: Float = calculateStatusBarPadding(),
    content: @Composable ShowcaseScope.(Boolean, Int) -> Unit
) {
    val scope = remember(topPadding) {
        ShowcaseScopeImpl(
            topPadding
        )
    }

    val state = rememberShowcaseState(
        shouldShowcasing = shouldShowcasing,
        lastIndex = lastIndex,
        key = key,
        animationDuration = animationDuration,
        showcaseScope = scope
    )

    Box {
        scope.content(state.isShowcasing, state.currentIndex)

        AnimatedVisibility(
            visible = state.isShowcasing,
            enter = fadeIn(animationSpec = tween(animationDuration)),
            exit = fadeOut(animationSpec = tween(animationDuration))
        ) {
            ShowcaseOverlay(
                state = state,
                scope = scope,
                lastIndex = lastIndex,
                lineThickness = lineThickness,
                variantColor = variantColor
            )
        }
    }
}

@Composable
private fun ShowcaseOverlay(
    state: ShowcaseState,
    scope: ShowcaseScopeImpl,
    lastIndex: Int,
    lineThickness: Dp,
    variantColor: Color
) {
    val offset by animateOffsetAsState(
        scope.getPositionFor(state.currentIndex),
        animationSpec = tween(state.animationDuration),
        label = ""
    )
    val itemSize by animateSizeAsState(
        scope.getSizeFor(state.currentIndex),
        animationSpec = tween(state.animationDuration),
        label = ""
    )
    val message by remember { derivedStateOf { scope.getMessageFor(state.currentIndex) } }

    val properties = remember {
        PopupProperties(
            focusable = true,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            excludeFromSystemGesture = true
        )
    }

    Popup(
        properties = properties
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val screenWidth = context.widthDp.toPx()
            val screenHeight = context.heightDp.toPx()

            ShowcaseCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { testTag = "canvas" }
                    .pointerInput(state.isShowcasing) {
                        detectTapGestures { state.handleTap(message) }
                    },
                state = state,
                offset = offset,
                itemSize = itemSize,
                message = message,
                lineThickness = lineThickness,
                variantColor = variantColor,
                screenWidth = screenWidth,
                screenHeight = screenHeight
            )

            if (lastIndex > 1) {
                ShowcaseNavigationControls(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    state = state,
                    lastIndex = lastIndex
                )
            }
        }
    }
}

@Composable
private fun ShowcaseCanvas(
    modifier: Modifier,
    state: ShowcaseState,
    offset: Offset,
    itemSize: Size,
    message: ShowcaseMsg?,
    lineThickness: Dp,
    variantColor: Color,
    screenWidth: Float,
    screenHeight: Float,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val lineSize = remember { with(density) { 70.toDp().toPx() } }
    val textStyle = MaterialTheme.typography.labelMedium
    val textColor = MaterialTheme.colorScheme.onSurface
    val msgBackground = MaterialTheme.colorScheme.surface

    Canvas(modifier = modifier) {
        val showcasePath = Path().apply {
            addRect(Rect(0f, 0f, size.width, size.height))
            addRoundRect(
                RoundRect(
                    rect = Rect(
                        offset.x - 2,
                        offset.y,
                        offset.x + itemSize.width + 2,
                        offset.y + itemSize.height
                    ),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )
            )
            fillType = PathFillType.EvenOdd
        }
        drawPath(
            path = showcasePath,
            color = Color.Black,
            alpha = 0.4f
        )

        val arrowColor = message?.arrow?.color ?: Color.White
        if (state.currentIndex > 0 && message?.arrow != null && state.isArrowDelayOver) {
            drawAnimatedArrow(
                message.arrow,
                offset,
                itemSize,
                lineSize,
                state.pathPortion.value,
                state.animArrow.value,
                state.animArrowHead.value,
                arrowColor,
                variantColor,
                lineThickness
            )
        }

        message?.let { msg ->
            drawShowcaseMessage(
                msg = msg,
                textMeasurer = textMeasurer,
                textStyle = textStyle.copy(color = textColor),
                screenWidth = screenWidth.toInt(),
                screenHeight = screenHeight,
                size = size,
                currentIndex = state.currentIndex,
                offset = offset,
                itemSize = itemSize,
                lineSize = lineSize,
                msgBackground = msgBackground,
                animMsgAlpha = state.animMsgAlpha.value,
                animMsgTextAlpha = state.animMsgTextAlpha.value
            )
        }
    }
}

private fun DrawScope.drawAnimatedArrow(
    arrow: Arrow,
    offset: Offset,
    itemSize: Size,
    lineSize: Float,
    pathPortionValue: Float,
    animArrowValue: Float,
    animArrowHeadValue: Float,
    arrowColor: Color,
    variantColor: Color,
    lineThickness: Dp
) {
    val hasArrowHead = arrow.head != null
    val arrowHeadMargin = arrow.headSize

    val arrowPath = Path().apply {
        when (arrow.targetFrom) {
            Side.Top -> {
                moveTo(offset.x + (itemSize.width / 2), offset.y - lineSize)
                lineTo(
                    offset.x + (itemSize.width / 2),
                    if (hasArrowHead) offset.y - arrowHeadMargin else offset.y
                )
            }

            Side.Bottom -> {
                moveTo(offset.x + (itemSize.width / 2), offset.y + (itemSize.height + lineSize))
                lineTo(
                    offset.x + (itemSize.width / 2),
                    if (hasArrowHead) offset.y + itemSize.height + arrowHeadMargin else offset.y + itemSize.height
                )
            }

            Side.Left -> {
                moveTo(offset.x - lineSize, offset.y + (itemSize.height / 2))
                lineTo(
                    if (hasArrowHead) offset.x - arrowHeadMargin else offset.x,
                    offset.y + (itemSize.height / 2)
                )
            }

            Side.Right -> {
                moveTo(offset.x + (itemSize.width + lineSize), offset.y + (itemSize.height / 2))
                lineTo(
                    if (hasArrowHead) offset.x + itemSize.width + arrowHeadMargin else offset.x + itemSize.width,
                    offset.y + (itemSize.height / 2)
                )
            }
        }
    }

    val outPath = Path()
    val pos = FloatArray(2)
    val tan = FloatArray(2)
    PathMeasure().apply {
        setPath(arrowPath, false)
        getSegment(0f, pathPortionValue * length, outPath, true)
        getPosition(pathPortionValue * length).apply {
            pos[0] = x; pos[1] = y
        }
        getTangent(pathPortionValue * length).apply {
            tan[0] = x; tan[1] = y
        }
    }

    drawPath(
        path = outPath,
        color = arrowColor,
        style = Stroke(width = lineThickness.toPx(), cap = StrokeCap.Round)
    )

    val x = pos[0]
    val y = pos[1]

    when (arrow.head) {
        Head.CIRCLE -> {
            drawCircle(
                center = Offset(x, y),
                color = arrowColor,
                alpha = animArrowValue,
                radius = animArrowHeadValue
            )
            drawCircle(
                center = Offset(x, y),
                color = variantColor,
                alpha = animArrowValue,
                radius = animArrowHeadValue * 0.6f
            )
        }

        Head.TRIANGLE -> {
            val degrees = -atan2(tan[0], tan[1]) * (180f / PI.toFloat()) - 180f
            rotate(degrees = degrees, pivot = Offset(x, y)) {
                drawPath(
                    path = Path().apply {
                        moveTo(x, y - animArrowHeadValue)
                        lineTo(x - animArrowHeadValue, y + animArrowHeadValue)
                        lineTo(x + animArrowHeadValue, y + animArrowHeadValue)
                        close()
                    },
                    color = arrowColor,
                    alpha = animArrowValue
                )
            }
        }

        Head.SQUARE -> {
            drawRect(
                topLeft = Offset(x - animArrowHeadValue / 2, y - animArrowHeadValue / 2),
                color = arrowColor,
                alpha = animArrowValue,
                size = Size(animArrowHeadValue, animArrowHeadValue)
            )
        }

        Head.ROUND_SQUARE -> {
            drawRoundRect(
                topLeft = Offset(x - animArrowHeadValue / 2, y - animArrowHeadValue / 2),
                color = arrowColor,
                alpha = animArrowValue,
                size = Size(animArrowHeadValue, animArrowHeadValue),
                cornerRadius = CornerRadius(animArrowHeadValue / 4)
            )
        }

        null -> Unit
    }
}

private fun DrawScope.drawShowcaseMessage(
    msg: ShowcaseMsg,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    screenWidth: Int,
    screenHeight: Float,
    size: Size,
    currentIndex: Int,
    offset: Offset,
    itemSize: Size,
    lineSize: Float,
    msgBackground: Color,
    animMsgAlpha: Float,
    animMsgTextAlpha: Float
) {
    val textResult = textMeasurer.measure(
        msg.text,
        style = textStyle,
        overflow = TextOverflow.Visible,
        constraints = Constraints(0, screenWidth - 90)
    )

    val halfWidth = size.width / 2
    val messageWidthHalf = textResult.size.width / 2

    val xOffset = if (currentIndex == 0 || msg.arrow?.curved == true) {
        halfWidth - messageWidthHalf
    } else {
        val currentItemXMiddlePoint = offset.x + (itemSize.width / 2)
        when {
            (currentItemXMiddlePoint < halfWidth) -> if ((currentItemXMiddlePoint - messageWidthHalf) < 0) offset.x else currentItemXMiddlePoint - messageWidthHalf
            (currentItemXMiddlePoint == halfWidth) -> currentItemXMiddlePoint - messageWidthHalf
            else -> if (currentItemXMiddlePoint + messageWidthHalf > size.width) offset.x + itemSize.width - textResult.size.width else currentItemXMiddlePoint - messageWidthHalf
        }
    }

    val cardSize = IntSize(textResult.size.width + 36, textResult.size.height + 36).toSize()

    val yOffset = if (currentIndex == 0) (size.height / 2) else {
        when (msg.gravity) {
            Gravity.Top -> offset.y - lineSize - cardSize.height + 36
            Gravity.Bottom -> offset.y + itemSize.height + lineSize
            Gravity.Auto -> offset.y + itemSize.height + lineSize
        }
    }

    val textOffset = Offset(
        (xOffset).coerceIn(0f, screenWidth.toFloat() - textResult.size.width - 36),
        (yOffset).coerceIn(0f, screenHeight - textResult.size.height - 36)
    )
    val cardOffset = Offset(textOffset.x - 18, textOffset.y - 18)

    drawRoundRect(
        color = msgBackground,
        topLeft = cardOffset,
        size = cardSize,
        cornerRadius = CornerRadius(msg.roundedCorner.toPx()),
        alpha = animMsgAlpha
    )

    drawText(
        textResult,
        topLeft = textOffset,
        alpha = animMsgTextAlpha
    )
}

@Composable
private fun ShowcaseNavigationControls(
    modifier: Modifier = Modifier,
    state: ShowcaseState,
    lastIndex: Int
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(Unit) {}
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        SimpleIconButton(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            enabled = state.currentIndex <= lastIndex,
            onClick = { state.next() }
        )

        Text(
            buildString { append("($lastIndex / ${state.currentIndex.coerceAtMost(lastIndex)})") },
            style = MaterialTheme.typography.labelMedium
        )

        SimpleIconButton(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            enabled = state.currentIndex > 1,
            onClick = { state.previous() }
        )

        FilledTextButton(
            stringResource(R.string.pass_guide),
            style = MaterialTheme.typography.labelSmall,
            onClick = { state.dismiss(resetIndex = false) },
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun rememberShowcaseState(
    shouldShowcasing: Boolean,
    lastIndex: Int,
    key: Preferences.Key<Int>,
    animationDuration: Int,
    showcaseScope: ShowcaseScope
): ShowcaseState {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    return remember(shouldShowcasing, lastIndex) {
        ShowcaseState(
            shouldShowcasing = shouldShowcasing,
            animationDuration = animationDuration,
            lastIndex = lastIndex,
            key = key,
            context = context,
            coroutineScope = coroutineScope,
            showcaseScope = showcaseScope
        )
    }
}


@Stable
@Composable
@NonRestartableComposable
fun defaultShowCaseMessage(
    text: String,
    gravity: Gravity = Gravity.Bottom,
    targetFrom: Side = Side.Bottom,
): ShowcaseMsg {
    return ShowcaseMsg(
        text, gravity = gravity, roundedCorner = 8.dp, arrow = Arrow(
            targetFrom = targetFrom,
            curved = false,
            animationDuration = MsgAnimation.DEFAULT_DURATION,
            animSize = true,
            head = Head.CIRCLE,
        )
    )
}