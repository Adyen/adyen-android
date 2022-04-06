/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.log

import android.os.Build

/**
 * Utility class with methods related to logs.
 */
object LogUtil {

    private const val CHECKOUT_LOG_PREFIX = "CO."
    private const val CLASS_NOT_FOUND = "?Unknown?"
    private const val MAX_TAG_SIZE = 23

    /**
     * Get the TAG to be used for logging inside Checkout classes.
     *
     * @return A String to be used as TAG.
     */
    @JvmStatic
    fun getTag(): String = getTag(CHECKOUT_LOG_PREFIX)

    /**
     * Get the TAG to be used for logging by the calling class.
     *
     * @return A String to be used as TAG with the format "Prefix.ClassName"
     */
    // This could be used by merchants if they want to.
    @JvmStatic
    fun getTag(prefix: String): String {
        val callerClass = simplifiedCallerClassName
        var tag = prefix + callerClass

        // Log tags have a size limitation on API lvl 23 and before
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && tag.length > MAX_TAG_SIZE) {
            tag = tag.substring(0, MAX_TAG_SIZE)
        }
        return tag
    }

    private val simplifiedCallerClassName: String
        get() {
            val className = callerClassName
            return simplifyClassName(className)
        }

    private val callerClassName: String
        get() {
            val stElements = Thread.currentThread().stackTrace
            for (ste in stElements.drop(1)) {
                val callerClass = ste.className
                if (callerClass != LogUtil::class.java.name && callerClass.indexOf("java.lang.Thread") != 0) {
                    return callerClass
                }
            }
            return CLASS_NOT_FOUND
        }

    private fun simplifyClassName(className: String): String {
        val packageSplit = className.split(".").toTypedArray()
        return if (packageSplit.isEmpty()) {
            className
        } else packageSplit[packageSplit.size - 1]
    }
}
