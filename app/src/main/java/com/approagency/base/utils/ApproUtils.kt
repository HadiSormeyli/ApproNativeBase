package com.approagency.base.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import androidx.core.graphics.scale
import com.approagency.base.model.ui.UiText
import com.approagency.base.presentation.BaseActivity
import java.io.File
import java.io.FileOutputStream

object ApproUtils {
    fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            val exif = context.contentResolver.openInputStream(uri)?.use { ExifInterface(it) }
            val rotation = when (exif?.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            return if (rotation != 0f) {
                val matrix = Matrix()
                matrix.postRotate(rotation)
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            } else {
                bitmap
            }
        } catch (_: Exception) {
            null
        }
    }

    fun upscaleBitmap(bitmap: Bitmap, scaleFactor: Float = 2f): Bitmap {
        val width = (bitmap.width * scaleFactor).toInt()
        val height = (bitmap.height * scaleFactor).toInt()
        return bitmap.scale(width, height)
    }

    fun ensureHighQuality(bitmap: Bitmap): Bitmap {
        return if (bitmap.config != Bitmap.Config.ARGB_8888) {
            bitmap.copy(Bitmap.Config.ARGB_8888, true)
        } else bitmap
    }


    fun saveBitmapToGallery(
        activity: BaseActivity,
        bitmap: Bitmap,
        filePrefix: String = "Screenshot"
    ) {
        val filename = "${filePrefix}_${System.currentTimeMillis()}"
        val mimeType = "image/png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val relativeLocation = Environment.DIRECTORY_PICTURES + "/ToolBox"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val contentResolver = activity.contentResolver
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                try {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)
                    activity.showSnackBar(UiText.DynamicString(buildString {
                        append("تصویر در مسیر ")
                        append(relativeLocation)
                        append(" ذخیره شد.")
                    }))
                } catch (_: Exception) {
                    activity.showSnackBar(UiText.DynamicString("مشکلی در ذخیره سازی تصویر پیش آمده است."))
                }
            } ?: run {
                activity.showSnackBar(UiText.DynamicString("مشکلی در ذخیره سازی تصویر پیش آمده است."))
            }
        } else {
            val picturesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val screenshotsDir = File(picturesDir, "LiveTSE")
            if (!screenshotsDir.exists()) screenshotsDir.mkdirs()

            val imageFile = File(screenshotsDir, filename)

            try {
                FileOutputStream(imageFile).use { outputStream ->
                    upscaleBitmap(bitmap).let {
                        ensureHighQuality(it).let {
                            it.density = DisplayMetrics.DENSITY_XHIGH
                            it.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                outputStream
                            )
                        }
                    }

                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, imageFile.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, mimeType)
                }
                activity.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

                activity.showSnackBar(UiText.DynamicString(buildString {
                    append("تصویر در مسیر ")
                    append("Pictures/ToolBox")
                    append(" ذخیره شد.")
                }))
            } catch (_: Exception) {
                activity.showSnackBar(UiText.DynamicString("مشکلی در ذخیره سازی تصویر پیش آمده است."))
            }
        }
    }
}