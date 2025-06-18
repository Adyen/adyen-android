/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.ui.PermissionHandler
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.internal.exception.PermissionRequestException
import com.adyen.checkout.ui.core.internal.util.PermissionHandlerResult.PERMISSION_GRANTED
import com.adyen.checkout.ui.core.internal.util.PermissionHandlerResult.PERMISSION_REQUEST_NOT_HANDLED
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import com.google.android.material.R as MaterialR

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ImageSaver(
    private val dispatcher: CoroutineDispatcher = DispatcherProvider.IO,
) {

    @Suppress("LongParameterList")
    suspend fun saveImageFromView(
        context: Context,
        permissionHandler: PermissionHandler,
        view: View,
        @ColorInt backgroundColor: Int? = null,
        fileName: String? = null,
        fileRelativePath: String? = null,
    ): Result<Unit> {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        backgroundColor?.let { color -> canvas.drawColor(color) }
            ?: view.background?.draw(canvas)
            ?: run {
                val defaultColor = ContextCompat.getColor(context, R.color.white)
                val defaultBackgroundColor = MaterialColors.getColor(context, MaterialR.attr.colorSurface, defaultColor)
                canvas.drawColor(defaultBackgroundColor)
            }

        view.draw(canvas)

        return saveImageFromBitmap(context, permissionHandler, bitmap, fileName, fileRelativePath)
    }

    suspend fun saveImageFromUrl(
        context: Context,
        permissionHandler: PermissionHandler,
        imageUrl: String,
        fileName: String? = null,
        fileRelativePath: String? = null,
    ): Result<Unit> = withContext(dispatcher) {
        val url = imageUrl.toURL() ?: return@withContext Result.failure(CheckoutException("Malformed URL"))

        return@withContext try {
            val inputStream = url.openStream()
            val bufferedInputStream = BufferedInputStream(inputStream)
            val bitmap = BitmapFactory.decodeStream(bufferedInputStream)

            saveImageFromBitmap(context, permissionHandler, bitmap, fileName, fileRelativePath)
        } catch (exception: IOException) {
            Result.failure(CheckoutException("Malformed URL: $exception"))
        }
    }

    private suspend fun saveImageFromBitmap(
        context: Context,
        permissionHandler: PermissionHandler,
        bitmap: Bitmap,
        fileName: String? = null,
        fileRelativePath: String? = null,
    ): Result<Unit> {
        val timestamp = System.currentTimeMillis()
        val imageName = fileName ?: timestamp.toString()
        val imagePath = fileRelativePath ?: Environment.DIRECTORY_DOWNLOADS
        val contentValues = ContentValues().apply {
            put(Media.MIME_TYPE, "image/png")
            put(Media.DATE_ADDED, timestamp)
            put(Media.DATE_TAKEN, timestamp)
            put(Media.DISPLAY_NAME, imageName)
            put(Media.RELATIVE_PATH, imagePath)
        }

        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageApi29AndAbove(context, bitmap, contentValues)
        } else {
            saveImageApi28AndBelow(context, permissionHandler, bitmap, contentValues)
        }
        bitmap.recycle()
        return result
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveImageApi29AndAbove(
        context: Context,
        bitmap: Bitmap,
        contentValues: ContentValues,
    ): Result<Unit> = withContext(dispatcher) {
        contentValues.put(Media.IS_PENDING, true)

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext Result.failure(CheckoutException("Error when saving Bitmap as an image: URI is null"))

        return@withContext try {
            val outputStream = context.contentResolver.openOutputStream(uri)
                ?: return@withContext Result.failure(CheckoutException("Output stream is null"))

            contentValues.put(Media.IS_PENDING, false)
            context.contentResolver.update(uri, contentValues, null, null)

            bitmap.compress(CompressFormat.PNG, PNG_QUALITY, outputStream)
            outputStream.close()

            adyenLog(AdyenLogLevel.DEBUG) { "Bitmap successfully saved as an image" }
            Result.success(Unit)
        } catch (e: FileNotFoundException) {
            Result.failure(CheckoutException("File not found: ", e))
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun saveImageApi28AndBelow(
        context: Context,
        permissionHandler: PermissionHandler,
        bitmap: Bitmap,
        contentValues: ContentValues,
    ): Result<Unit> = withContext(dispatcher) {
        when (permissionHandler.checkPermission(context, REQUIRED_PERMISSION)) {
            PERMISSION_GRANTED -> saveImageApi28AndBelowWhenPermissionGranted(bitmap, contentValues)
            PERMISSION_REQUEST_NOT_HANDLED -> {
                adyenLog(AdyenLogLevel.ERROR) { "Permission request not handled" }
                Result.failure(PermissionRequestException("Permission request not handled"))
            }

            else -> Result.failure(PermissionRequestException("The $REQUIRED_PERMISSION permission is denied"))
        }
    }

    @RequiresPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private suspend fun saveImageApi28AndBelowWhenPermissionGranted(
        bitmap: Bitmap,
        contentValues: ContentValues
    ): Result<Unit> = withContext(dispatcher) {
        val fileName = contentValues.getAsString(Media.DISPLAY_NAME)
        val filePath = contentValues.getAsString(Media.RELATIVE_PATH)
        val imageFileFolder =
            Environment.getExternalStoragePublicDirectory(filePath)
        if (!imageFileFolder.exists()) {
            imageFileFolder.mkdirs()
        }
        val imageFile = File(imageFileFolder, fileName)

        return@withContext try {
            val outputStream = FileOutputStream(imageFile)

            bitmap.compress(CompressFormat.PNG, PNG_QUALITY, outputStream)
            outputStream.close()

            adyenLog(AdyenLogLevel.DEBUG) { "Bitmap successfully saved as an image" }
            Result.success(Unit)
        } catch (e: FileNotFoundException) {
            Result.failure(CheckoutException("File not found: ", e))
        } catch (e: SecurityException) {
            Result.failure(CheckoutException("Security violation: ", e))
        }
    }

    private fun String.toURL(): URL? {
        return try {
            URL(this)
        } catch (e: MalformedURLException) {
            adyenLog(AdyenLogLevel.ERROR) { "Failed to convert String to URL: $e" }
            null
        }
    }

    companion object {
        private const val PNG_QUALITY = 100
        private const val REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
