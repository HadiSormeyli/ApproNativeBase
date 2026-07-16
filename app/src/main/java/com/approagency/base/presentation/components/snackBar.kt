package com.approagency.base.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.approagency.base.R
import com.approagency.base.model.ui.ApproSnackBarVisuals
import com.approagency.base.model.ui.SnackBarStyle
import com.approagency.base.model.ui.SnackBarType
import kotlinx.coroutines.launch

@Composable
fun ApproSnackBarHost(
    modifier: Modifier = Modifier,
    hostState: SnackbarHostState,
) {
    SnackbarHost(
        modifier = modifier,
        hostState = hostState,
        snackbar = { snackBarData ->
            val visuals = snackBarData.visuals as ApproSnackBarVisuals

            ApproSnackBar(
                visuals = visuals,
                onDismiss = {
                    hostState.currentSnackbarData?.dismiss()
                }
            )
        })
}

@Composable
fun ApproSnackBar(
    visuals: ApproSnackBarVisuals,
    onDismiss: () -> Unit,
    getStyle: @Composable (SnackBarType) -> SnackBarStyle = {
        getSnackBarStyle(it)
    }
) {
    val style = getStyle(visuals.type)
    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        when (dismissState.currentValue) {
            SwipeToDismissBoxValue.StartToEnd,
            SwipeToDismissBoxValue.EndToStart -> {
                onDismiss()
            }

            else -> Unit
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.small,
            tonalElevation = 8.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .widthIn(max = 600.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(width = 1.dp, color = style.color, shape = MaterialTheme.shapes.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color.Transparent)
                    .padding(start = 8.dp)
            ) {
                style.iconRes?.let {
                    Icon(
                        painter = painterResource(it),
                        contentDescription = null,
                        tint = style.color,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = visuals.message,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.weight(1f)
                )

                visuals.actionLabel?.let { label ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.clickable {
                            visuals.onActionClick?.invoke()
                            onDismiss()
                        }
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    scope.launch {
                        dismissState.dismiss(SwipeToDismissBoxValue.StartToEnd)
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "Dismiss"
                    )
                }
            }
        }
    }
}

@Composable
private fun getSnackBarStyle(type: SnackBarType): SnackBarStyle {
    return when (type) {
        SnackBarType.SUCCESS -> SnackBarStyle(
            MaterialTheme.colorScheme.primary,
            R.drawable.check_fill
        )

        SnackBarType.ERROR -> SnackBarStyle(MaterialTheme.colorScheme.error, R.drawable.error)
        SnackBarType.WARNING -> SnackBarStyle(MaterialTheme.colorScheme.error, R.drawable.error)
        SnackBarType.SIMPLE -> SnackBarStyle(MaterialTheme.colorScheme.primary, null)
    }
}