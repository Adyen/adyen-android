/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.robot

import com.adevinta.android.barista.assertion.BaristaVisibilityAssertions.assertDisplayed

internal class DropInRobot : TestRobot {

    override fun verifyIsOnScreen() {
        // Check if bottom sheet is displayed, because checking the layout from DropInActivity doesn't work
        assertDisplayed(com.google.android.material.R.id.design_bottom_sheet)
    }
}

internal inline fun onDropIn(action: DropInRobot.() -> Unit) {
    DropInRobot().apply {
        verifyIsOnScreen()
        action()
    }
}
