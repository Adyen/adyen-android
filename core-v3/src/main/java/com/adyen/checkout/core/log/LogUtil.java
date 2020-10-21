/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.core.log;

import android.os.Build;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.core.util.KotlinBase;

/**
 * Utility class with methods related to logs.
 */
public final class LogUtil {

    private static final String CHECKOUT_LOG_PREFIX = "CO.";
    private static final String CLASS_NOT_FOUND = "?Unknown?";

    private static final int MAX_TAG_SIZE = 23;

    static {
        KotlinBase.Companion.log();
    }

    /**
     * Get the TAG to be used for logging inside Checkout classes.
     *
     * @return A String to be used as TAG.
     */
    @NonNull
    public static String getTag() {
        return getTag(CHECKOUT_LOG_PREFIX);
    }

    /**
     * Get the TAG to be used for logging by the calling class.
     *
     * @return A String to be used as TAG with the format "Prefix.ClassName"
     */
    //This could be used by merchants if they want to.
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public static String getTag(@NonNull String prefix) {
        final String callerClass = getSimplifiedCallerClassName();
        String tag = prefix + callerClass;

        // Log tags have a size limitation on API lvl 23 and before
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && tag.length() > MAX_TAG_SIZE) {
            tag = tag.substring(0, MAX_TAG_SIZE);
        }

        return tag;
    }

    @NonNull
    private static String getSimplifiedCallerClassName() {
        final String className = getCallerClassName();
        return simplifyClassName(className);
    }

    @NonNull
    private static String getCallerClassName() {
        final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < stElements.length; i++) {
            final StackTraceElement ste = stElements[i];
            final String callerClass = ste.getClassName();
            if (!callerClass.equals(LogUtil.class.getName()) && callerClass.indexOf("java.lang.Thread") != 0) {
                return callerClass;
            }
        }
        return CLASS_NOT_FOUND;
    }

    @NonNull
    private static String simplifyClassName(@NonNull String className) {
        final String[] packageSplit = className.split("\\.");
        if (packageSplit.length == 0) {
            return className;
        }
        return packageSplit[packageSplit.length - 1];
    }

    private LogUtil() {
        throw new NoConstructorException();
    }

}
