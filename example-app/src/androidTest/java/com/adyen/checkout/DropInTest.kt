/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.adyen.checkout.example.ui.main.MainActivity
import com.adyen.checkout.robot.onDropIn
import com.adyen.checkout.robot.onMain
import com.adyen.checkout.rule.IdlingDispatcherRule
import com.adyen.checkout.server.CheckoutMockWebServer
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
internal class DropInTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule(order = 2)
    var dispatcherRule = IdlingDispatcherRule()

    @Before
    fun before() {
        CheckoutMockWebServer.start()
    }

    @After
    fun after() {
        CheckoutMockWebServer.stop()
    }

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
