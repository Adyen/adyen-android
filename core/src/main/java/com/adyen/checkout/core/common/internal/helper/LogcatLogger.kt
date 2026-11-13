/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/2/2024.
 */

package com.adyen.checkout.core.common.internal.helper

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import com.adyen.checkout.core.common.AdyenLogger.LogLevel

internal class LogcatLogger : LogWriter {

    override fun write(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        val priority = level.toLogcatPriority() ?: return

        // Before API 26 tags have a max length
        val trimmedTag = if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tag
        } else {
            tag.substring(0, MAX_TAG_LENGTH)
        }

        val fullMessage = concatThrowable(message, throwable)

        if (fullMessage.length < MAX_LOG_LENGTH) {
            logToLogcat(priority, trimmedTag, fullMessage)
            return
        }

        val divisions = fullMessage.length / MAX_LOG_LENGTH
        for (i in 0..divisions) {
            val newMessage: String = if (i != divisions) {
                fullMessage.substring(i * MAX_LOG_LENGTH, (i + 1) * MAX_LOG_LENGTH)
            } else {
                fullMessage.substring(i * MAX_LOG_LENGTH)
            }
            logToLogcat(priority, "$trimmedTag-$i", newMessage)
        }
    }

    private fun concatThrowable(message: String, throwable: Throwable?): String {
        return if (throwable != null) {
            "$message: ${Log.getStackTraceString(throwable)}"
        } else {
            message
        }
    }

    @SuppressLint("NotAdyenLog")
    private fun logToLogcat(
        priority: Int,
        tag: String,
        message: String,
    ) {
        when (priority) {
            Log.ASSERT -> {
                Log.wtf(tag, message)
            }

            else -> {
                Log.println(priority, tag, message)
            }
        }
    }

    companion object {
        private const val MAX_TAG_LENGTH = 23
        private const val MAX_LOG_LENGTH = 2048
    }
}

internal fun LogLevel.toLogcatPriority(): Int? = when (this) {
    LogLevel.VERBOSE -> Log.VERBOSE
    LogLevel.DEBUG -> Log.DEBUG
    LogLevel.INFO -> Log.INFO
    LogLevel.WARN -> Log.WARN
    LogLevel.ERROR -> Log.ERROR
    LogLevel.ASSERT -> Log.ASSERT
    LogLevel.OFF -> null
}
