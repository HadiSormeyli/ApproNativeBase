package com.approagency.base.model.showcase

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ShowcaseMsg(
    val text: String,
    val textStyle: TextStyle = TextStyle(color = Color.Black),
    val msgBackground: Color? = null,
    val roundedCorner: Dp = 0.dp,
    val gravity: Gravity = Gravity.Auto,
    val arrow: Arrow? = null,
    val enterAnim: MsgAnimation = MsgAnimation.FadeInOut(),
    val exitAnim: MsgAnimation = MsgAnimation.FadeInOut(),
)
