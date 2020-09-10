/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/10/2019.
 */

package com.adyen.checkout.example.utils

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.ViewAction
import android.support.test.espresso.ViewInteraction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

fun Int.getValue(): String {
    val targetContext = InstrumentationRegistry.getTargetContext()
    return targetContext.resources.getString(this)
}

fun Int.asIdViewMatcher() = ViewMatchers.withId(this)

fun matchView(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)

fun Int.matchView(): ViewInteraction = matchView(asIdViewMatcher())

fun Int.performClick() = matchView().performClick()

fun Int.performTypeText(text: String) = matchView().performTypeText(text)

fun Int.findItemByTextinRecyclerAndPerformClick(textViewID: Int, text: String) =
    matchView()
        .perform(RecyclerViewActions.actionOnItem<androidx.recyclerview.widget.RecyclerView.ViewHolder>
        (ViewMatchers.hasDescendant(CoreMatchers.allOf(ViewMatchers.withId(textViewID),
            ViewMatchers.withText(text))),
            ViewActions.click()))

fun <T : androidx.recyclerview.widget.RecyclerView.ViewHolder> Int.performActionOnRecyclerItemAtPosition(position: Int, action: ViewAction) =
    matchView().performActionOnRecyclerItemAtPosition<T>(position, action)

fun ViewInteraction.performClick() = perform(ViewActions.click())

fun ViewInteraction.performTypeText(text: String) = perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())

fun <T : androidx.recyclerview.widget.RecyclerView.ViewHolder> ViewInteraction.performActionOnRecyclerItemAtPosition(position: Int, action: ViewAction) {
    perform(RecyclerViewActions.actionOnItemAtPosition<T>(position, action))
}
