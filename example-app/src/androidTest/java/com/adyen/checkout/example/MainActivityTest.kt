/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/10/2019.
 */

package com.adyen.checkout.example

import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.example.data.CREDIT_CARD
import com.adyen.checkout.example.data.FIRST_STORED_PAYMENT_METHOD
import com.adyen.checkout.example.data.IDEAL
import com.adyen.checkout.example.data.IDEAL_WEBVIEW_REDIRECT_KEY
import com.adyen.checkout.example.data.RESULT_KEY_AUTHORISED
import com.adyen.checkout.example.data.RESULT_KEY_REFUSED
import com.adyen.checkout.example.data.normalScheme
import com.adyen.checkout.example.ui.main.MainActivity
import com.adyen.checkout.example.utils.findItemByTextinRecyclerAndPerformClick
import com.adyen.checkout.example.utils.findObjectWithText
import com.adyen.checkout.example.utils.performActionOnRecyclerItemAtPosition
import com.adyen.checkout.example.utils.performClick
import com.adyen.checkout.example.utils.performTypeText
import okhttp3.OkHttpClient
import org.hamcrest.CoreMatchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.inject

@RunWith(AndroidJUnit4::class)
@LargeTest
class MainActivityTest : KoinTest {

    private val okHttpClient: OkHttpClient by inject()

    @get:Rule
    var activityRule: IntentsTestRule<MainActivity> = IntentsTestRule(MainActivity::class.java)

    // TODO: 21/10/2020 Re enable after getValue() is fixes on Expresso utils class
//    @Test
//    fun e2e_3ds2_scheme_success() {
//        R.id.startCheckoutButton.performClick()
//        R.id.recyclerView_paymentMethods.performActionOnRecyclerItemAtPosition<RecyclerView.ViewHolder>(
//            FOOTER_OF_PAYEMENT_METHODS_FOOTER, findViewByIdAndPerformClick(R.id.others)
//        )
//
//        R.id.recyclerView_paymentMethods.findItemByTextinRecyclerAndPerformClick(R.id.textView_text, CREDIT_CARD)
//
//        R.id.editText_securityCode.performTypeText(threeds2Scheme.cvc)
//        R.id.editText_cardNumber.performTypeText(threeds2Scheme.cardNumber)
//        R.id.editText_expiryDate.performTypeText(threeds2Scheme.expiryDate)
//
//        R.id.payButton.performClick()
//        waitForNewWindowToOpen(R.string.app_name.getValue())
//
//        com.adyen.threeds2.R.id.editText_text.performTypeText(threeds2Scheme.threeds2!!.password)
//        com.adyen.threeds2.R.id.button_continue.performClick()
//
//        verifyAuthorisedPayment()
//    }

    @Test
    fun e2e_storedPaymentMethod_scheme_success() {
        R.id.startCheckoutButton.performClick()
        R.id.recyclerView_paymentMethods.performActionOnRecyclerItemAtPosition<RecyclerView.ViewHolder>(
            FIRST_STORED_PAYMENT_METHOD, click()
        )
        R.id.editText_securityCode.performTypeText(normalScheme.cvc)
        R.id.payButton.performClick()

        verifyAuthorisedPayment()
    }

    @Test
    fun e2e_storedPaymentMethod_scheme_failed() {
        R.id.startCheckoutButton.performClick()
        R.id.recyclerView_paymentMethods.performActionOnRecyclerItemAtPosition<RecyclerView.ViewHolder>(
            FIRST_STORED_PAYMENT_METHOD, click()
        )
        // wrong cvc
        R.id.editText_securityCode.performTypeText("888")
        R.id.payButton.performClick()

        verifyRejectedPayment()
    }

    @Test
    fun e2e_normal_scheme_success() {
        R.id.startCheckoutButton.performClick()
//        R.id.recyclerView_paymentMethods.performActionOnRecyclerItemAtPosition<RecyclerView.ViewHolder>(
//            FOOTER_OF_PAYEMENT_METHODS_FOOTER, findViewByIdAndPerformClick(R.id.change_payment_method_button)
//        )

        R.id.recyclerView_paymentMethods.findItemByTextinRecyclerAndPerformClick(R.id.textView_text, CREDIT_CARD)

        R.id.editText_securityCode.performTypeText(normalScheme.cvc)
        R.id.editText_cardNumber.performTypeText(normalScheme.cardNumber)
        R.id.editText_expiryDate.performTypeText(normalScheme.expiryDate)

        R.id.payButton.performClick()

        verifyAuthorisedPayment()
    }

    @Test
    fun e2e_IDEAL_success() {
        R.id.startCheckoutButton.performClick()

//        R.id.recyclerView_paymentMethods
//            .performActionOnRecyclerItemAtPosition<RecyclerView.ViewHolder>(
//                FOOTER_OF_PAYEMENT_METHODS_FOOTER,
//                findViewByIdAndPerformClick(R.id.change_payment_method_button)
//            )

        R.id.recyclerView_paymentMethods.findItemByTextinRecyclerAndPerformClick(R.id.textView_text, IDEAL)

        R.id.recycler_issuers.performActionOnRecyclerItemAtPosition<RecyclerView.ViewHolder>(0, click())

        findObjectWithText(IDEAL_WEBVIEW_REDIRECT_KEY)!!.clickAndWaitForNewWindow()

        verifyAuthorisedPayment()
    }

    private fun verifyAuthorisedPayment() {
        this.verifyIntentWithResultKey(RESULT_KEY_AUTHORISED)
    }

    private fun verifyRejectedPayment() {
        this.verifyIntentWithResultKey(RESULT_KEY_REFUSED)
    }

    private fun verifyIntentWithResultKey(value: String) {
        intended(allOf(hasExtra(DropIn.RESULT_KEY, value), hasComponent(MainActivity::class.java.name)))
    }
}
