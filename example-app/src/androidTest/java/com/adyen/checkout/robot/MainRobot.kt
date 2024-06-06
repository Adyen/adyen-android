/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.robot

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed
import com.adevinta.android.barista.interaction.BaristaCheckboxInteractions.check
import com.adevinta.android.barista.interaction.BaristaClickInteractions.clickOn
import com.adyen.checkout.example.R

internal class MainRobot : TestRobot {

    override fun verifyIsOnScreen() {
        assertDisplayed(R.id.main_container)
    }

    fun enableSessions() {
        check(R.id.switch_sessions)
    }

    fun startDropIn() {
        clickOn("Start")
    }
}

internal inline fun onMain(action: MainRobot.() -> Unit) {
    MainRobot().apply {
        verifyIsOnScreen()
        action()
    }
}
