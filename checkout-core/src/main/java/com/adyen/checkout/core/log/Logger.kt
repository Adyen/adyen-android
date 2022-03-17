/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.log

import android.util.Log
import androidx.annotation.IntDef
import com.adyen.checkout.core.BuildConfig

/**
 * Log manager for Checkout.
 * Serves as a proxy managing what and how to log information.
 */
// Keeping method names to match the ones from Logcat
@Suppress("TooManyFunctions")
object Logger {

    @IntDef(SENSITIVE, Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, NONE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class LogLevel

    // TODO: 14/02/2019 The idea is for this class to have a system where we can send a stream of logs to the merchant and/or proxy to Logcat.
    private const val SENSITIVE = -1
    const val NONE = Log.ASSERT + 1

    // The logcat limit changes per device, you can see it using $adb logcat -g
    // 2KB seems like a safe value to be within max payload range
    private const val MAX_LOGCAT_MSG_SIZE = 2048

    @LogLevel
    private var logcatLevel = if (BuildConfig.DEBUG) Log.DEBUG else NONE
    private var isLogcatLevelInitialized = false
    fun updateDefaultLogcatLevel(isDebugBuild: Boolean) {
        if (!isLogcatLevelInitialized) {
            logcatLevel = if (isDebugBuild) Log.DEBUG else NONE
        }
    }

    @JvmStatic
    fun setLogcatLevel(@LogLevel logcatLevel: Int) {
        isLogcatLevelInitialized = true
        this.logcatLevel = logcatLevel
    }

    @JvmStatic
    fun v(tag: String, msg: String) {
        logToLogcat(Log.VERBOSE, tag, msg, null)
    }

    @JvmStatic
    fun v(tag: String, msg: String, tr: Throwable) {
        logToLogcat(Log.VERBOSE, tag, msg, tr)
    }

    @JvmStatic
    fun d(tag: String, msg: String) {
        logToLogcat(Log.DEBUG, tag, msg, null)
    }

    @JvmStatic
    fun d(tag: String, msg: String, tr: Throwable) {
        logToLogcat(Log.DEBUG, tag, msg, tr)
    }

    @JvmStatic
    fun i(tag: String, msg: String) {
        logToLogcat(Log.INFO, tag, msg, null)
    }

    @JvmStatic
    fun i(tag: String, msg: String, tr: Throwable) {
        logToLogcat(Log.INFO, tag, msg, tr)
    }

    @JvmStatic
    fun w(tag: String, msg: String) {
        logToLogcat(Log.WARN, tag, msg, null)
    }

    @JvmStatic
    fun w(tag: String, msg: String, tr: Throwable) {
        logToLogcat(Log.WARN, tag, msg, tr)
    }

    @JvmStatic
    fun e(tag: String, msg: String) {
        logToLogcat(Log.ERROR, tag, msg, null)
    }

    @JvmStatic
    fun e(tag: String, msg: String, tr: Throwable) {
        logToLogcat(Log.ERROR, tag, msg, tr)
    }

    /**
     * Log to be used when you want to debug sensitive information that cannot be committed.
     * Set the [LogLevel] to [SENSITIVE] and make sure to change it back before committing.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    @Suppress("unused")
    fun sensitiveLog(tag: String, msg: String) {
        if (logcatLevel != SENSITIVE) {
            throw SecurityException("Sensitive information should never be logged. Remove before committing.")
        } else {
            logToLogcat(SENSITIVE, tag, msg, null)
        }
    }

    @Suppress("ComplexMethod")
    private fun logToLogcat(@LogLevel logLevel: Int, tag: String, msg: String, tr: Throwable?) {
        if (logcatLevel > logLevel) {
            return
        }

        // Cut the message into multiple logs if it's too big
        if (msg.length > MAX_LOGCAT_MSG_SIZE) {
            val divisions = msg.length / MAX_LOGCAT_MSG_SIZE
            for (i in 0..divisions) {
                val newMessage: String = if (i != divisions) {
                    msg.substring(
                        i * MAX_LOGCAT_MSG_SIZE,
                        (i + 1) * MAX_LOGCAT_MSG_SIZE
                    )
                } else {
                    msg.substring(i * MAX_LOGCAT_MSG_SIZE)
                }
                logToLogcat(logLevel, "$tag-$i", newMessage, tr)
            }
            return
        }

        when (logLevel) {
            SENSITIVE -> if (tr == null) {
                Log.wtf(tag, msg)
            } else {
                Log.wtf(tag, msg, tr)
            }
            Log.VERBOSE -> if (tr == null) {
                Log.v(tag, msg)
            } else {
                Log.v(tag, msg, tr)
            }
            Log.DEBUG -> if (tr == null) {
                Log.d(tag, msg)
            } else {
                Log.d(tag, msg, tr)
            }
            Log.INFO -> if (tr == null) {
                Log.i(tag, msg)
            } else {
                Log.i(tag, msg, tr)
            }
            Log.WARN -> if (tr == null) {
                Log.w(tag, msg)
            } else {
                Log.w(tag, msg, tr)
            }
            Log.ERROR -> if (tr == null) {
                Log.e(tag, msg)
            } else {
                Log.e(tag, msg, tr)
            }
            NONE -> {}
            else -> {}
        }
    }
}
