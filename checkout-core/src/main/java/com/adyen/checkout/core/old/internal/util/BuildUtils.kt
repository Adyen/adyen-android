package com.adyen.checkout.core.old.internal.util

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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
