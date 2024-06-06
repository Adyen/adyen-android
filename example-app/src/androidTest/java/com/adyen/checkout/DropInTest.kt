/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout

import com.adyen.checkout.robot.onDropIn
import com.adyen.checkout.robot.onMain
import com.adyen.checkout.rule.CheckoutTestRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
internal class DropInTest {

    @get:Rule
    var checkoutTestRule = CheckoutTestRule(this)

    @Test
    fun whenDropInIsOpened_thenItIsDisplayed() {
        onMain {
            enableSessions()
            startDropIn()
        }

        onDropIn {
            verifyIsOnScreen()
        }
    }
}
