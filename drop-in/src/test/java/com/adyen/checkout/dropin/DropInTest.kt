/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin

import android.content.ComponentName
import android.content.pm.PackageManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class DropInTest {

    private val context = RuntimeEnvironment.getApplication()

    private val redirectHandlingActivity = ComponentName(context, DropInRedirectHandlingActivity::class.java)

    @Test
    fun `when redirect handling is disabled, then the redirect handling activity is disabled`() {
        DropIn.setRedirectHandlingEnabled(context, false)

        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            context.packageManager.getComponentEnabledSetting(redirectHandlingActivity),
        )
    }

    @Test
    fun `when redirect handling is enabled again, then the redirect handling activity is enabled`() {
        DropIn.setRedirectHandlingEnabled(context, false)

        DropIn.setRedirectHandlingEnabled(context, true)

        assertEquals(
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            context.packageManager.getComponentEnabledSetting(redirectHandlingActivity),
        )
    }
}
