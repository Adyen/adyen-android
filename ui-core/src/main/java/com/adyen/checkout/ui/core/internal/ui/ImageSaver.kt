/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/1/2024.
 */

package com.adyen.checkout.ui.core.internal.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ImageSaver {

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    suspend fun saveImageFromView(
        context: Context,
        view: View,
        fileRelativePath: String,
        fileName: String? = null,
    ): Result<Unit> {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        view.background?.draw(canvas) ?: canvas.drawColor(Color.WHITE)
        view.draw(canvas)

        val timestamp = System.currentTimeMillis()
        val imageName = fileName ?: timestamp.toString()
        val contentValues = ContentValues().apply {
            put(Media.MIME_TYPE, "image/png")
            put(Media.DATE_ADDED, timestamp)
            put(Media.DATE_TAKEN, timestamp)
            put(Media.RELATIVE_PATH, fileRelativePath)
            put(Media.DISPLAY_NAME, imageName)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageApi29AndAbove(context, bitmap, contentValues)
        } else {
            saveImageApi28AndBelow(context, bitmap, contentValues)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveImageApi29AndAbove(
        context: Context,
        bitmap: Bitmap,
        contentValues: ContentValues,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        contentValues.put(Media.IS_PENDING, true)

        val uri = context.contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)
        return@withContext if (uri != null) {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(CompressFormat.PNG, PNG_QUALITY, outputStream)
                    outputStream.close()

                    contentValues.put(Media.IS_PENDING, false)
                    context.contentResolver.update(uri, contentValues, null, null)

                    Logger.d(TAG, "Bitmap successfully saved as am image")
                    Result.success(Unit)
                } else {
                    Result.failure(CheckoutException("Error when saving Bitmap as an image: OutputStream is null"))
                }
            } catch (e: FileNotFoundException) {
                Result.failure(CheckoutException("Error when saving Bitmap as an image: ", e))
            }
        } else {
            Result.failure(CheckoutException("Error when saving Bitmap as an image: URI is null"))
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private suspend fun saveImageApi28AndBelow(
        context: Context,
        bitmap: Bitmap,
        contentValues: ContentValues,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val imageFileFolder =
            Environment.getExternalStoragePublicDirectory(contentValues.getAsString(Media.RELATIVE_PATH))
        if (!imageFileFolder.exists()) {
            imageFileFolder.mkdirs()
        }
        val imageFile = File(imageFileFolder, contentValues.getAsString(Media.DISPLAY_NAME))

        return@withContext try {
            val outputStream = FileOutputStream(imageFile)

            bitmap.compress(CompressFormat.PNG, PNG_QUALITY, outputStream)
            outputStream.close()

            contentValues.put(Media.DATA, imageFile.absolutePath)
            context.contentResolver.insert(Media.EXTERNAL_CONTENT_URI, contentValues)

            Logger.d(TAG, "Bitmap successfully saved as am image")
            Result.success(Unit)
        } catch (e: FileNotFoundException) {
            Result.failure(CheckoutException("Error when saving Bitmap as an image: ", e))
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val PNG_QUALITY = 100
    }
}
