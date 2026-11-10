/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
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
internal class DropInActivityTest {

    @Test
    fun `when a redirect arrives while no Drop-in is running, then the activity finishes without a result`() {
        Robolectric.buildActivity(DropInActivity::class.java, redirectIntent()).use { controller ->
            controller.create()

            val activity = controller.get()
            assertTrue(activity.isFinishing)
            assertEquals(Activity.RESULT_CANCELED, shadowOf(activity).resultCode)
        }
    }

    @Test
    fun `when Drop-in finishes without input, then destroying it does not crash`() {
        val controller = Robolectric.buildActivity(DropInActivity::class.java, redirectIntent())

        // Would throw if the view model, which needs the input, was created
        controller.create().destroy()
    }

    @Test
    fun `when a redirect arrives after the activity closed without input, then it is ignored`() {
        Robolectric.buildActivity(DropInActivity::class.java, redirectIntent()).use { controller ->
            controller.create()

            // Would throw if the view model, which needs the input, was created
            controller.newIntent(redirectIntent())
        }
    }

    private fun redirectIntent() = Intent(RuntimeEnvironment.getApplication(), DropInActivity::class.java)
        .setAction(Intent.ACTION_VIEW)
        .setData(Uri.parse("adyencheckout://com.adyen.checkout.test/dropin?redirectResult=abc"))
}
