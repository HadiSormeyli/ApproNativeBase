package com.approagency.base.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.SecureFlagPolicy
import com.approagency.base.R
import com.approagency.base.theme.LocalBaseActivity
import com.approagency.base.utils.heightDp
import androidx.compose.material3.ModalBottomSheet as MaterialModalBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproAlertBottomSheet(
    title: String,
    description: String,
    confirmText: String = stringResource(R.string.confirm),
    cancelText: String = stringResource(R.string.cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    confirmColor: Color = MaterialTheme.colorScheme.error,
    confirmContentColor: Color = MaterialTheme.colorScheme.onError,
    cancelColor: Color = Color.Transparent,
    cancelContentColor: Color = confirmColor,
    descriptionColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    descriptionStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    buttonStyle: TextStyle = MaterialTheme.typography.labelLarge,
    buttonShape: Shape = MaterialTheme.shapes.medium,
    buttonHeight: Dp = 48.dp,
    buttonSpacing: Dp = 8.dp,
    contentSpacing: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(
        start = 16.dp,
        end = 16.dp,
        bottom = 16.dp
    ),
    confirmColors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = confirmColor,
        contentColor = confirmContentColor
    ),
    cancelColors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = cancelColor,
        contentColor = cancelContentColor
    )
) {
    ApproModalBottomSheet(
        title = title,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            Text(
                text = description,
                style = descriptionStyle,
                color = descriptionColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
            ) {
                FilledTextButton(
                    text = confirmText,
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight),
                    shape = buttonShape,
                    colors = confirmColors,
                    style = buttonStyle,
                    onClick = {
                        onConfirm()
                        onDismiss()
                    }
                )
                OutlinedTextButton(
                    text = cancelText,
                    modifier = Modifier
                        .weight(1f)
                        .height(buttonHeight),
                    shape = buttonShape,
                    colors = cancelColors,
                    border = BorderStroke(1.dp, confirmColor),
                    style = buttonStyle,
                    onClick = onDismiss
                )
            }
        }
    }
}

@Composable
fun ApproModalHeader(
    title: String,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    titleStyle: TextStyle = MaterialTheme.typography.titleMedium,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    closeTint: Color = MaterialTheme.colorScheme.onSurface,
    dividerColor: Color = MaterialTheme.colorScheme.outlineVariant,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    action: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            onDismiss?.let {
                SimpleIconButton(
                    imageVector = Icons.Default.Close,
                    tint = closeTint,
                    contentDescription = null,
                    onClick = it
                )
            }
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = titleStyle,
                color = titleColor
            )
            action?.invoke()
        }
        HorizontalDivider(color = dividerColor)
    }
}

@Composable
fun ApproModalDragHandle(
    modifier: Modifier = Modifier,
    width: Dp = 56.dp,
    height: Dp = 4.dp,
    containerHeight: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.background,
    shape: Shape = RoundedCornerShape(50)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(containerHeight)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(width)
                .height(height)
                .background(color, shape)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApproModalBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    skipPartiallyExpanded: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = skipPartiallyExpanded
    ),
    padding: Dp = 16.dp,
    expandedCornerRadius: Dp = 0.dp,
    defaultCornerRadius: Dp = 32.dp,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = MaterialTheme.colorScheme.onBackground,
    tonalElevation: Dp = 12.dp,
    sheetMaxWidth: Dp = 600.dp,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
    dragHandle: @Composable (() -> Unit)? = {
        ApproModalDragHandle()
    },
    headerAction: (@Composable () -> Unit)? = null,
    overlay: @Composable BoxScope.() -> Unit = {
        val activity = LocalBaseActivity.current

        ApproSnackBarHost(
            hostState = activity.snackBarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    },
    content: @Composable (SheetState) -> Unit
) {
    val activity = LocalBaseActivity.current
    val density = LocalDensity.current
    val height = remember { mutableStateOf(0.dp) }
    val statusBarSize = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val isDarkMode = !activity.themeManager.isDarkMode()

    val cornerRadius by animateDpAsState(
        targetValue = if (sheetState.targetValue == SheetValue.Expanded && height.value >= activity.heightDp) {
            expandedCornerRadius
        } else {
            defaultCornerRadius
        },
        label = "bottomSheetCornerRadius"
    )

    MaterialModalBottomSheet(
        modifier = modifier.onGloballyPositioned {
            height.value = with(density) {
                it.size.height.toDp() + statusBarSize
            }
        },
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = cornerRadius,
            topEnd = cornerRadius
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        sheetMaxWidth = sheetMaxWidth,
        dragHandle = dragHandle,
        properties = ModalBottomSheetProperties(
            isAppearanceLightStatusBars = isDarkMode,
            isAppearanceLightNavigationBars = isDarkMode,
            securePolicy = securePolicy,
            shouldDismissOnBackPress = dismissOnBackPress,
            shouldDismissOnClickOutside = dismissOnClickOutside
        )
    ) {
        Box(
            modifier = Modifier.wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = containerColor)
                    .padding(horizontal = padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ApproModalHeader(
                    title = title,
                    onDismiss = onDismiss,
                    action = headerAction
                )

                content(sheetState)
            }

            overlay()
        }
    }
}