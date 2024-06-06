/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.rule

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.adyen.checkout.example.ui.main.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CheckoutTestRule(
    testInstance: Any
) : TestRule {

    private val hiltRule = HiltAndroidRule(testInstance)
    private val activityRule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    override fun apply(base: Statement, description: Description?): Statement =
        /*
         * The order of the rules below is important!
         * - HiltAndroidRule has to be the outer rule to ensure the dependency graph is built only once
         * - MockServerRule comes second to make sure the backend is ready asap
         * - IdlingDispatcherRule comes after MockServerRule
         * - Rules after ActivityScenarioRule will be executed after the activity is launched
         */
        RuleChain
        .outerRule(hiltRule)
        .around(MockServerRule())
        .around(IdlingDispatcherRule())
        .around(activityRule)
        .apply(base, description)
}
