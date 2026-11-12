/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import android.app.ActivityManager
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.ActivityInfo
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInRedirectHandlingActivity
import com.adyen.checkout.dropin.internal.helper.RedirectConfigurationProblem.RedirectHandlingDisabled
import com.adyen.checkout.dropin.internal.helper.RedirectConfigurationProblem.RedirectHandlingNotDeclared
import com.adyen.checkout.dropin.internal.helper.RedirectConfigurationProblem.TaskAffinityMismatch
import com.adyen.checkout.dropin.internal.ui.DropInActivity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAppTask

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class RedirectConfigurationCheckerTest {

    private lateinit var context: Application

    private lateinit var checker: RedirectConfigurationChecker

    private val dropInActivity by lazy { ComponentName(context, DropInActivity::class.java) }
    private val redirectHandlingActivity by lazy { ComponentName(context, DropInRedirectHandlingActivity::class.java) }
    private val merchantActivity by lazy { ComponentName(context.packageName, "com.merchant.MainActivity") }

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        checker = RedirectConfigurationChecker(context)
        addActivity(dropInActivity, taskAffinity = APP_AFFINITY)
        addActivity(redirectHandlingActivity, taskAffinity = APP_AFFINITY, exported = true)
    }

    @Test
    fun `when everything is configured, then no problems are found`() {
        addActivity(merchantActivity, taskAffinity = APP_AFFINITY)
        setTask(baseActivity = merchantActivity, topActivity = dropInActivity)

        assertEquals(emptyList<RedirectConfigurationProblem>(), checker.findProblems())
    }

    @Test
    fun `when the root of Drop-in's task has another affinity, then the mismatch is found`() {
        // android:taskAffinity="" is parsed as no affinity
        addActivity(merchantActivity, taskAffinity = null)
        setTask(baseActivity = merchantActivity, topActivity = dropInActivity)

        assertEquals(
            listOf(TaskAffinityMismatch(taskAffinity = null, dropInTaskAffinity = APP_AFFINITY)),
            checker.findProblems(),
        )
    }

    @Test
    fun `when Drop-in is not on top of a task, then the affinity is not checked`() {
        addActivity(merchantActivity, taskAffinity = null)
        setTask(baseActivity = merchantActivity, topActivity = merchantActivity)

        assertEquals(emptyList<RedirectConfigurationProblem>(), checker.findProblems())
    }

    @Test
    fun `when the redirect handling activity is not exported, then it is reported as not declared`() {
        addActivity(redirectHandlingActivity, taskAffinity = APP_AFFINITY, exported = false)

        assertEquals(listOf(RedirectHandlingNotDeclared), checker.findProblems())
    }

    @Test
    fun `when the redirect handling activity is removed from the manifest, then it is reported as not declared`() {
        shadowOf(context.packageManager).removeActivity(redirectHandlingActivity)

        assertEquals(listOf(RedirectHandlingNotDeclared), checker.findProblems())
    }

    @Test
    fun `when redirect handling is turned off at runtime, then it is reported as disabled`() {
        DropIn.setRedirectHandlingEnabled(context, false)

        assertEquals(listOf(RedirectHandlingDisabled), checker.findProblems())
    }

    @Test
    fun `when the redirect handling activity is disabled in the manifest, then it is reported as disabled`() {
        addActivity(redirectHandlingActivity, taskAffinity = APP_AFFINITY, exported = true, enabled = false)

        assertEquals(listOf(RedirectHandlingDisabled), checker.findProblems())
    }

    @Test
    fun `when redirect handling is turned on at runtime, then the manifest value is overridden`() {
        addActivity(redirectHandlingActivity, taskAffinity = APP_AFFINITY, exported = true, enabled = false)
        DropIn.setRedirectHandlingEnabled(context, true)

        assertEquals(emptyList<RedirectConfigurationProblem>(), checker.findProblems())
    }

    @Test
    fun `when the system fails to answer, then the check does not crash`() {
        val activityManager = mock<ActivityManager> {
            on { appTasks } doThrow SecurityException("Not allowed")
        }
        val failingContext = mock<Context> {
            on { applicationContext } doReturn mock
            on { packageName } doReturn context.packageName
            on { getSystemService(ActivityManager::class.java) } doReturn activityManager
        }

        RedirectConfigurationChecker(failingContext).check()
    }

    private fun addActivity(
        component: ComponentName,
        taskAffinity: String?,
        exported: Boolean = false,
        enabled: Boolean = true,
    ) {
        val activityInfo = ActivityInfo().apply {
            packageName = component.packageName
            name = component.className
            this.taskAffinity = taskAffinity
            this.exported = exported
            this.enabled = enabled
        }
        shadowOf(context.packageManager).addOrUpdateActivity(activityInfo)
    }

    private fun setTask(baseActivity: ComponentName, topActivity: ComponentName) {
        val appTask = ShadowAppTask.newInstance()
        shadowOf(appTask).setTaskInfo(
            ActivityManager.RecentTaskInfo().apply {
                this.baseActivity = baseActivity
                this.topActivity = topActivity
            },
        )
        val activityManager = context.getSystemService(ActivityManager::class.java)
        shadowOf(activityManager).setAppTasks(listOf(appTask))
    }

    private companion object {
        private const val APP_AFFINITY = "com.adyen.checkout.test"
    }
}
