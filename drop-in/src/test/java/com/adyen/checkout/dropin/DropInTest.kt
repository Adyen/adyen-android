/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin

import android.content.Context
import android.content.pm.PackageManager
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class DropInTest {

    private val packageManager = mock<PackageManager>()

    private val context = mock<Context> {
        on { this.packageManager } doReturn packageManager
    }

    @Test
    fun `when redirect handling is disabled, then the redirect handling activity is disabled without killing the app`() {
        DropIn.setRedirectHandlingEnabled(context, false)

        verify(packageManager).setComponentEnabledSetting(
            any(),
            eq(PackageManager.COMPONENT_ENABLED_STATE_DISABLED),
            eq(PackageManager.DONT_KILL_APP),
        )
    }

    @Test
    fun `when redirect handling is enabled, then the redirect handling activity is enabled without killing the app`() {
        DropIn.setRedirectHandlingEnabled(context, true)

        verify(packageManager).setComponentEnabledSetting(
            any(),
            eq(PackageManager.COMPONENT_ENABLED_STATE_ENABLED),
            eq(PackageManager.DONT_KILL_APP),
        )
    }

    @Test
    fun `when the redirect handling activity was removed from the manifest, then the call does not crash`() {
        // What Android throws for a component that the merged manifest does not contain
        doThrow(IllegalArgumentException("Unknown component"))
            .whenever(packageManager).setComponentEnabledSetting(any(), any(), any())

        DropIn.setRedirectHandlingEnabled(context, false)
    }
}
