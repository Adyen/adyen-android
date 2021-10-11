package com.adyen.checkout.core.util

import android.content.Context
import android.content.pm.ApplicationInfo

object BuildUtils {

    /**
     * Check if the current build is a debug build.
     *
     * @param context [Context]
     * @return whether the build is a debug build or not
     */
    fun isDebugBuild(context: Context): Boolean {
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }
}
