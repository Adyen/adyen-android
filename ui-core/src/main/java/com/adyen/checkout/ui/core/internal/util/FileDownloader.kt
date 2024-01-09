/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/1/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.PermissionException
import com.adyen.checkout.core.internal.ui.PermissionHandler
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.core.internal.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.MalformedURLException
import java.net.URL

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Suppress("LongParameterList")
class FileDownloader {

    suspend fun download(
        context: Context,
        permissionHandler: PermissionHandler,
        stringUrl: String,
        fileName: String,
        filePath: String,
        mimeType: String? = null
    ): Result<Unit> {
        val url = stringUrl.toURL() ?: return Result.failure(CheckoutException("Malformed URL"))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            downloadAndSaveApi29AndAbove(context, url, fileName, filePath, mimeType ?: "*/*")
        } else {
            downloadAndSaveApi28AndBelow(context, permissionHandler, url, fileName, filePath)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun downloadAndSaveApi29AndAbove(
        context: Context,
        url: URL,
        fileName: String,
        filePath: String,
        mimeType: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, filePath)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return@withContext Result.failure(CheckoutException("URI is null"))

        url.openStream().use { input ->
            resolver.openOutputStream(uri).use { output ->
                if (output == null) return@withContext Result.failure(CheckoutException("out is null"))
                input.copyTo(output, DEFAULT_BUFFER_SIZE)
                return@withContext Result.success(Unit)
            }
        }
    }

    private suspend fun downloadAndSaveApi28AndBelow(
        context: Context,
        permissionHandler: PermissionHandler,
        url: URL,
        fileName: String,
        filePath: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (permissionHandler.checkPermission(context, REQUIRED_PERMISSION) == true) {
            downloadAndSaveApi28AndBelowWhenPermissionGranted(context, url, fileName, filePath)
        } else {
            Result.failure(PermissionException("The $REQUIRED_PERMISSION permission is denied"))
        }
    }

    private suspend fun downloadAndSaveApi28AndBelowWhenPermissionGranted(
        context: Context,
        url: URL,
        fileName: String,
        filePath: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val imageFile = File(context.getExternalFilesDir(filePath), fileName)
        url.openStream().use { input ->
            FileOutputStream(imageFile).use { output ->
                input.copyTo(output)

                val insertImageResult = MediaStore.Images.Media.insertImage(
                    context.contentResolver,
                    imageFile.absolutePath,
                    fileName,
                    null
                )

                return@withContext if (insertImageResult == null) {
                    Result.failure(CheckoutException("couldn't insert image to gallery"))
                } else {
                    Result.success(Unit)
                }
            }
        }
    }

    private fun String.toURL(): URL? {
        return try {
            URL(this)
        } catch (e: MalformedURLException) {
            Logger.e(TAG, "toURL: $e")
            null
        }
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val REQUIRED_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    }
}
