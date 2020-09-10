/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/10/2019.
 */

package com.adyen.checkout.example.utils

import androidx.annotation.IdRes
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.uiautomator.UiDevice
import android.support.test.uiautomator.UiObject
import android.support.test.uiautomator.UiSelector
import android.view.View
import android.widget.TextView
import org.hamcrest.Matcher

fun findObjectWithText(search: String): UiObject? {
    val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val selector = UiSelector()
    return device.findObject(selector.textContains(search))
}

fun waitForNewWindowToOpen(value: String) {
    val mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    val myApp = mDevice.findObject(UiSelector().textContains(value))
    myApp.clickAndWaitForNewWindow()
}

fun findViewByIdAndPerformClick(@IdRes viewID: Int): ViewAction {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View>? {
            return null
        }

        override fun getDescription(): String {
            return "Click on specific button"
        }

        override fun perform(uiController: UiController, view: View) {
            val view = view.findViewById<TextView>(viewID)
            view.performClick()
        }
    }
}
