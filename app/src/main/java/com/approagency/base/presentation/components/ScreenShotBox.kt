package com.approagency.base.presentation.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.core.content.ContextCompat
import com.approagency.base.R
import com.approagency.base.model.ui.UiText
import com.approagency.base.presentation.BaseActivity
import com.approagency.base.theme.LocalBaseActivity
import com.approagency.base.utils.Utils
import kotlinx.coroutines.launch

@Composable
fun ScreenShotBox(
    name: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
    permissionError: UiText = UiText.StringResource(R.string.error_permission),
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    onCaptured: (Bitmap) -> Unit = {},
    onSaved: (Bitmap) -> Unit = {},
    onError: (Throwable) -> Unit = {},
    saveBitmap: suspend (BaseActivity, Bitmap, String) -> Unit = { activity, bitmap, fileName ->
        Utils.saveBitmapToGallery(activity, bitmap, fileName)
    },
    content: @Composable BoxScope.(
        onScreenShotClicked: () -> Unit
    ) -> Unit
) {
    val activity = LocalBaseActivity.current
    val coroutineScope = rememberCoroutineScope()

    val currentName by rememberUpdatedState(name)
    val currentOnPermissionGranted by rememberUpdatedState(onPermissionGranted)
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    val currentOnSaved by rememberUpdatedState(onSaved)
    val currentOnError by rememberUpdatedState(onError)
    val currentSaveBitmap by rememberUpdatedState(saveBitmap)

    val captureScreen: () -> Unit = {
        if (enabled) {
            coroutineScope.launch {
                runCatching {
                    graphicsLayer
                        .toImageBitmap()
                        .asAndroidBitmap()
                }.onSuccess { bitmap ->
                    currentOnCaptured(bitmap)

                    runCatching {
                        currentSaveBitmap(
                            activity,
                            bitmap,
                            currentName
                        )
                    }.onSuccess {
                        currentOnSaved(bitmap)
                    }.onFailure(currentOnError)
                }.onFailure(currentOnError)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            currentOnPermissionGranted()
            captureScreen()
        } else {
            currentOnPermissionDenied()
            activity.showSnackBar(permissionError)
        }
    }

    val requestScreenShot: () -> Unit = {
        when {
            !enabled -> Unit

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                captureScreen()
            }

            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                captureScreen()
            }

            else -> {
                permissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }

    Box(
        modifier = modifier.drawWithContent {
            graphicsLayer.record {
                this@drawWithContent.drawContent()
            }

            drawLayer(graphicsLayer)
        }
    ) {
        content(requestScreenShot)
    }
}