/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 8/1/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore.Images.Media
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.ui.PermissionHandler
import com.adyen.checkout.core.internal.ui.PermissionHandlerCallback
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import com.adyen.checkout.ui.core.internal.exception.PermissionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import kotlin.coroutines.resume

// TODO: Test this class if possible
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ImageSaver {

    suspend fun saveImageFromView(
        context: Context,
        permissionHandler: PermissionHandler,
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

    private suspend fun saveImageApi28AndBelow(
        context: Context,
        permissionHandler: PermissionHandler,
        bitmap: Bitmap,
        contentValues: ContentValues,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (checkPermission(context, permissionHandler) == true) {
            saveImageApi28AndBelowWhenPermissionGranted(context, bitmap, contentValues)
        } else {
            Result.failure(PermissionException("The $REQUIRED_PERMISSION permission is denied"))
        }
    }

    private suspend fun checkPermission(context: Context, permissionHandler: PermissionHandler): Boolean? =
        suspendCancellableCoroutine { continuation ->
            if (ContextCompat.checkSelfPermission(context, REQUIRED_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }

            permissionHandler.requestPermission(context, REQUIRED_PERMISSION, object : PermissionHandlerCallback {
                override fun onPermissionGranted(requestedPermission: String) {
                    if (requestedPermission == REQUIRED_PERMISSION) {
                        continuation.resume(true)
                    } else {
                        Logger.e(TAG, "The $requestedPermission is not the requested $REQUIRED_PERMISSION permission")
                        continuation.resume(null)
                    }
                }

                override fun onPermissionDenied(requestedPermission: String) {
                    continuation.resume(false)
                }
            })
        }

    private suspend fun saveImageApi28AndBelowWhenPermissionGranted(
        context: Context,
        bitmap: Bitmap,
        contentValues: ContentValues
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
        private const val REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
