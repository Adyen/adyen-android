/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import com.adyen.checkout.dropin.internal.ui.DropInActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
internal class DropInRedirectHandlingActivityTest {

    @Test
    fun `when a redirect returns, then only its action and data are passed to Drop-in`() {
        val incomingIntent = redirectIntent()
            .putExtra("injected", "value")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        Robolectric.buildActivity(DropInRedirectHandlingActivity::class.java, incomingIntent).use { controller ->
            controller.create()

            val activity = controller.get()
            val forwardedIntent = shadowOf(activity).nextStartedActivity
            assertEquals(ComponentName(activity, DropInActivity::class.java), forwardedIntent.component)
            assertEquals(Intent.ACTION_VIEW, forwardedIntent.action)
            assertEquals(RETURN_URI, forwardedIntent.data)
            assertNull(forwardedIntent.extras)
            assertEquals(0, forwardedIntent.flags)
            assertTrue(activity.isFinishing)
        }
    }

    @Test
    fun `when an intent with another action arrives, then nothing is started`() {
        val incomingIntent = redirectIntent().setAction(Intent.ACTION_SEND)

        Robolectric.buildActivity(DropInRedirectHandlingActivity::class.java, incomingIntent).use { controller ->
            controller.create()

            assertNull(shadowOf(controller.get()).nextStartedActivity)
            assertTrue(controller.get().isFinishing)
        }
    }

    @Test
    fun `when a redirect without data arrives, then nothing is started`() {
        val incomingIntent = redirectIntent().setData(null)

        Robolectric.buildActivity(DropInRedirectHandlingActivity::class.java, incomingIntent).use { controller ->
            controller.create()

            assertNull(shadowOf(controller.get()).nextStartedActivity)
            assertTrue(controller.get().isFinishing)
        }
    }

    private fun redirectIntent() = Intent(
        RuntimeEnvironment.getApplication(),
        DropInRedirectHandlingActivity::class.java,
    )
        .setAction(Intent.ACTION_VIEW)
        .setData(RETURN_URI)

    private companion object {
        private val RETURN_URI = Uri.parse("adyencheckout://com.adyen.checkout.test/dropin?redirectResult=abc")
    }
}
