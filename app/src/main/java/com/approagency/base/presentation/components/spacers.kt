package com.approagency.base.presentation.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun HorizontalSpacer(space: Dp = 8.dp) {
    Spacer(modifier = Modifier.width(space))
}

@Composable
fun VerticalSpacer(space: Dp = 8.dp) {
    Spacer(modifier = Modifier.height(space))
}

@Composable
fun ColumnScope.FillSpacer(weight: Float = 1f) {
    Spacer(modifier = Modifier.weight(weight))
}

@Composable
fun RowScope.FillSpacer(weight: Float = 1f) {
    Spacer(modifier = Modifier.weight(weight))
}