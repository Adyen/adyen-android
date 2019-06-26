/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.dropin.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import com.adyen.checkout.base.analytics.AnalyticEvent
import com.adyen.checkout.base.analytics.AnalyticsDispatcher
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.R

/**
 * Activity that presents the available PaymentMethods to the Shopper.
 */
class PaymentMethodPickerActivity : AppCompatActivity() {

    companion object {
        private val TAG = LogUtil.getTag()

        private const val FRAGMENT_TAG = "PAYMENT_METHODS_DIALOG_FRAGMENT"

        private const val PAYMENT_METHODS_RESPONSE_KEY = "payment_methods_response"

        fun createIntent(context: Context, paymentMethodsApiResponse: PaymentMethodsApiResponse): Intent {
            val intent = Intent(context, PaymentMethodPickerActivity::class.java)
            intent.putExtra(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodsApiResponse)
            return intent
        }
    }

    private lateinit var paymentMethodPickerViewModel: PaymentMethodPickerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate - $savedInstanceState")
        setContentView(R.layout.activity_payment_method_picker)
        overridePendingTransition(0, 0)

        paymentMethodPickerViewModel = ViewModelProviders.of(this).get(PaymentMethodPickerViewModel::class.java)
        paymentMethodPickerViewModel.paymentMethodsApiResponse =
            if (savedInstanceState != null && savedInstanceState.containsKey(PAYMENT_METHODS_RESPONSE_KEY)) {
                savedInstanceState.getParcelable(PAYMENT_METHODS_RESPONSE_KEY)!!
            } else {
                intent.getParcelableExtra(PAYMENT_METHODS_RESPONSE_KEY)
            }

        PaymentMethodListDialogFragment.newInstance().show(supportFragmentManager, FRAGMENT_TAG)

        sendAnalyticsEvent()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Logger.d(TAG, "onSaveInstanceState")
        outState?.putParcelable(PAYMENT_METHODS_RESPONSE_KEY, paymentMethodPickerViewModel.paymentMethodsApiResponse)
    }

    override fun onDestroy() {
        super.onDestroy()
        Logger.d(TAG, "onDestroy")
    }

    fun onDialogDismissed() {
        Logger.d(TAG, "onDialogDismissed")
        finish()
        overridePendingTransition(0, R.anim.fade_out)
    }

    private fun sendAnalyticsEvent() {
        Logger.d(TAG, "sendAnalyticsEvent")
        val analyticEvent = AnalyticEvent.create(this, AnalyticEvent.Flavor.DROPIN, "dropin", DropIn.INSTANCE.configuration.shopperLocale)
        AnalyticsDispatcher.dispatchEvent(this, DropIn.INSTANCE.configuration.environment, analyticEvent)
    }
}
