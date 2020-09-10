/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.core.log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.adyen.checkout.core.BuildConfig;
import com.adyen.checkout.core.exception.NoConstructorException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Log manager for Checkout.
 * Serves as a proxy managing what and how to log information.
 */
// Keeping method names to match the ones from Logcat
@SuppressWarnings("checkstyle:MethodName")
public final class Logger {

    // TODO: 14/02/2019 The idea is for this class to have a system where we can send a stream of logs to the merchant and/or proxy to Logcat.

    private static final int SENSITIVE = -1;
    public static final int NONE = Log.ASSERT + 1;

    // The logcat limit changes per device, you can see it using $adb logcat -g
    // 2KB seems like a safe value to be within max payload range
    private static final int MAX_LOGCAT_MSG_SIZE = 2048;

    @LogLevel
    private static int sLogcatLevel = BuildConfig.DEBUG ?  Log.DEBUG : NONE;

    @IntDef({SENSITIVE, Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LogLevel{}

    public static void setLogcatLevel(@LogLevel int logcatLevel) {
        sLogcatLevel = logcatLevel;
    }

    public static void v(@NonNull String tag, @NonNull String msg) {
        logToLogcat(Log.VERBOSE, tag, msg, null);
    }

    public static void v(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        logToLogcat(Log.VERBOSE, tag, msg, tr);
    }

    public static void d(@NonNull String tag, @NonNull String msg) {
        logToLogcat(Log.DEBUG, tag, msg, null);
    }

    public static void d(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        logToLogcat(Log.DEBUG, tag, msg, tr);
    }

    public static void i(@NonNull String tag, @NonNull String msg) {
        logToLogcat(Log.INFO, tag, msg, null);
    }

    public static void i(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        logToLogcat(Log.INFO, tag, msg, tr);
    }

    public static void w(@NonNull String tag, @NonNull String msg) {
        logToLogcat(Log.WARN, tag, msg, null);
    }

    public static void w(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        logToLogcat(Log.WARN, tag, msg, tr);
    }

    public static void e(@NonNull String tag, @NonNull String msg) {
        logToLogcat(Log.ERROR, tag, msg, null);
    }

    public static void e(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
        logToLogcat(Log.ERROR, tag, msg, tr);
    }

    /**
     * Log to be used when you want to debug sensitive information that cannot be committed.
     * Set the {@link LogLevel} to {@link LogLevel#SENSITIVE} and make sure to change it back before committing.
     *
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    public static void sensitiveLog(@NonNull String tag, @NonNull String msg) {
        if (sLogcatLevel != SENSITIVE) {
            throw new SecurityException("Sensitive information should never be logged. Remove before committing.");
        } else {
            logToLogcat(SENSITIVE, tag, msg, null);
        }
    }

    private static void logToLogcat(@LogLevel int logLevel, @NonNull String tag, @NonNull String msg, @Nullable Throwable tr) {
        if (sLogcatLevel > logLevel) {
            return;
        }

        // Cut the message into multiple logs if it's too big
        if (msg.length() > MAX_LOGCAT_MSG_SIZE) {
            final int divisions = msg.length() / MAX_LOGCAT_MSG_SIZE;
            for (int i = 0; i <= divisions; i++) {
                final String newMessage;
                if (i != divisions) {
                    newMessage = msg.substring(i * MAX_LOGCAT_MSG_SIZE, (i + 1) * MAX_LOGCAT_MSG_SIZE);
                } else {
                    newMessage = msg.substring(i * MAX_LOGCAT_MSG_SIZE);
                }
                logToLogcat(logLevel, tag + "-" + i, newMessage, tr);
            }
            return;
        }

        switch (logLevel) {
            case SENSITIVE:
                if (tr == null) {
                    Log.wtf(tag, msg);
                } else {
                    Log.wtf(tag, msg, tr);
                }
                break;
            case Log.VERBOSE:
                if (tr == null) {
                    Log.v(tag, msg);
                } else {
                    Log.v(tag, msg, tr);
                }
                break;
            case Log.DEBUG:
                if (tr == null) {
                    Log.d(tag, msg);
                } else {
                    Log.d(tag, msg, tr);
                }
                break;
            case Log.INFO:
                if (tr == null) {
                    Log.i(tag, msg);
                } else {
                    Log.i(tag, msg, tr);
                }
                break;
            case Log.WARN:
                if (tr == null) {
                    Log.w(tag, msg);
                } else {
                    Log.w(tag, msg, tr);
                }
                break;
            case Log.ERROR:
                if (tr == null) {
                    Log.e(tag, msg);
                } else {
                    Log.e(tag, msg, tr);
                }
                break;
            case NONE:
                // intentional fallthrough
            default:
                // Don't Log anything
        }
    }

    private Logger() {
        throw new NoConstructorException();
    }

}
