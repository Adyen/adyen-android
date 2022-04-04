/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/10/2019.
 */

package com.adyen.checkout.example.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

// TODO: 21/10/2020 check InstrumentationRegistry deprecation alternative
// fun Int.getValue(): String {
//     val targetContext = InstrumentationRegistry.getTargetContext()
//     return targetContext.resources.getString(this)
// }

fun Int.asIdViewMatcher() = ViewMatchers.withId(this)

fun matchView(matcher: Matcher<View>): ViewInteraction = Espresso.onView(matcher)

fun Int.matchView(): ViewInteraction = matchView(asIdViewMatcher())

fun Int.performClick() = matchView().performClick()

fun Int.performTypeText(text: String) = matchView().performTypeText(text)

fun Int.findItemByTextinRecyclerAndPerformClick(textViewID: Int, text: String) =
    matchView()
        .perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                ViewMatchers.hasDescendant(
                    CoreMatchers.allOf(ViewMatchers.withId(textViewID), ViewMatchers.withText(text))
                ),
                ViewActions.click()
            )
        )

fun <T : RecyclerView.ViewHolder> Int.performActionOnRecyclerItemAtPosition(position: Int, action: ViewAction) =
    matchView().performActionOnRecyclerItemAtPosition<T>(position, action)

fun ViewInteraction.performClick() = perform(ViewActions.click())

fun ViewInteraction.performTypeText(text: String) = perform(ViewActions.typeText(text), ViewActions.closeSoftKeyboard())

fun <T : RecyclerView.ViewHolder> ViewInteraction.performActionOnRecyclerItemAtPosition(
    position: Int,
    action: ViewAction
) {
    perform(RecyclerViewActions.actionOnItemAtPosition<T>(position, action))
}
