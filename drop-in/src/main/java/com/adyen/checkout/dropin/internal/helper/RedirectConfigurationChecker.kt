/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.dropin.DropInRedirectHandlingActivity
import com.adyen.checkout.dropin.internal.ui.DropInActivity

/**
 * Logs the app configurations that keep a redirect from returning to Drop-in. Meant to run when Drop-in starts a
 * redirect, so the log appears right before the problem it predicts. It only logs, Drop-in continues either way.
 *
 * @param context Any context of the app, its application context is used.
 */
internal class RedirectConfigurationChecker(context: Context) {

    private val context = context.applicationContext

    fun check() {
        findProblems().forEach { problem ->
            adyenLog(AdyenLogLevel.ERROR) { "${problem.message()} See $DOCS for how to fix this." }
        }
    }

    @VisibleForTesting
    internal fun findProblems(): List<RedirectConfigurationProblem> {
        return listOfNotNull(findTaskAffinityMismatch(), findRedirectHandlingProblem())
    }

    // Drop-in lives in the task that launched it, and the return finds that task by its affinity, which is the
    // affinity of the task's root activity.
    private fun findTaskAffinityMismatch(): RedirectConfigurationProblem.TaskAffinityMismatch? {
        val dropInActivity = ComponentName(context, DropInActivity::class.java)
        val rootActivity = findTaskRoot(dropInActivity) ?: return null

        val taskAffinity = getActivityInfo(rootActivity)?.taskAffinity
        val dropInTaskAffinity = getActivityInfo(dropInActivity)?.taskAffinity
        return if (taskAffinity == dropInTaskAffinity) {
            null
        } else {
            RedirectConfigurationProblem.TaskAffinityMismatch(taskAffinity, dropInTaskAffinity)
        }
    }

    // Drop-in is on top of its task, the redirect has not left the app yet
    private fun findTaskRoot(topActivity: ComponentName): ComponentName? {
        val activityManager = context.getSystemService(ActivityManager::class.java) ?: return null
        return activityManager.appTasks
            .mapNotNull { appTask -> runCatching { appTask.taskInfo }.getOrNull() }
            .firstOrNull { it.topActivity == topActivity }
            ?.baseActivity
    }

    private fun findRedirectHandlingProblem(): RedirectConfigurationProblem? {
        val component = ComponentName(context, DropInRedirectHandlingActivity::class.java)
        val activityInfo = getActivityInfo(component)

        return when {
            activityInfo == null || !activityInfo.exported -> RedirectConfigurationProblem.RedirectHandlingNotDeclared
            !isEnabled(component, activityInfo) -> RedirectConfigurationProblem.RedirectHandlingDisabled
            else -> null
        }
    }

    // The runtime setting takes precedence, android:enabled from the manifest only applies while there is none
    private fun isEnabled(component: ComponentName, activityInfo: ActivityInfo): Boolean {
        return when (context.packageManager.getComponentEnabledSetting(component)) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> activityInfo.enabled
            else -> false
        }
    }

    // GET_DISABLED_COMPONENTS has the same value as MATCH_DISABLED_COMPONENTS, which needs API 24
    @Suppress("DEPRECATION")
    private fun getActivityInfo(component: ComponentName): ActivityInfo? {
        return runCatching {
            context.packageManager.getActivityInfo(component, PackageManager.GET_DISABLED_COMPONENTS)
        }.getOrNull()
    }

    private fun RedirectConfigurationProblem.message(): String = when (this) {
        is RedirectConfigurationProblem.TaskAffinityMismatch ->
            "The root activity of the task that launched Drop-in has task affinity \"${taskAffinity.orEmpty()}\", " +
                "Drop-in has \"${dropInTaskAffinity.orEmpty()}\", so a redirect can't return to Drop-in. Give the " +
                "root activity android:taskAffinity=\"${dropInTaskAffinity.orEmpty()}\"."

        RedirectConfigurationProblem.RedirectHandlingNotDeclared ->
            "${DropInRedirectHandlingActivity::class.java.name} is not exported, so a redirect can't return to " +
                "Drop-in. Declare it in your manifest with android:exported=\"true\" and an intent filter matching " +
                "your returnUrl."

        RedirectConfigurationProblem.RedirectHandlingDisabled ->
            "${DropInRedirectHandlingActivity::class.java.name} is disabled, so a redirect can't return to Drop-in. " +
                "Enable it with DropIn.setRedirectHandlingEnabled or in your manifest."
    }

    private companion object {
        private const val DOCS = "docs/v6/redirects.md in the adyen-android repository"
    }
}

internal sealed interface RedirectConfigurationProblem {

    data class TaskAffinityMismatch(
        val taskAffinity: String?,
        val dropInTaskAffinity: String?,
    ) : RedirectConfigurationProblem

    data object RedirectHandlingNotDeclared : RedirectConfigurationProblem

    data object RedirectHandlingDisabled : RedirectConfigurationProblem
}
