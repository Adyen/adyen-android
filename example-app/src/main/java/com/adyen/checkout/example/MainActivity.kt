/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/2/2019.
 */

package com.adyen.checkout.example

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.example.arch.PaymentMethodsViewModel
import com.adyen.checkout.googlepay.GooglePayConfiguration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = LogUtil.getTag()
    }

    private lateinit var mPaymentMethodsApiResponse: PaymentMethodsApiResponse
    private var isWaitingPaymentMethods = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Logger.d(TAG, "onCreate")
        setContentView(R.layout.activity_main)

        if (intent.hasExtra(DropIn.RESULT_KEY)) {
            Toast.makeText(this, intent.getStringExtra(DropIn.RESULT_KEY), Toast.LENGTH_SHORT).show()
        }

        val myViewModel = ViewModelProviders.of(this).get(PaymentMethodsViewModel::class.java)

        startCheckoutButton.setOnClickListener {
            Logger.d(TAG, "Click")

            if (::mPaymentMethodsApiResponse.isInitialized) {
                startCheckout()
            } else {
                isWaitingPaymentMethods = true
                it.visibility = View.GONE
                progressBar.show()
            }
        }

        myViewModel.paymentMethodResponseLiveData.observe(this, Observer {
            if (null != it) {
                Logger.d(TAG, "Got paymentMethods response")
                mPaymentMethodsApiResponse = it
                if (isWaitingPaymentMethods) {
                    startCheckout()
                }
            }
        })
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.d(TAG, "onNewIntent")
        if (intent?.hasExtra(DropIn.RESULT_KEY) == true) {
            Toast.makeText(this, intent.getStringExtra(DropIn.RESULT_KEY), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startCheckout() {
        Logger.d(TAG, "startCheckout")

        val googlePayConfig = GooglePayConfiguration.Builder(this@MainActivity, "TestMerchantCheckout").build()

        val cardConfiguration =
                CardConfiguration.Builder(this@MainActivity, BuildConfig.PUBLIC_KEY)
                .setShopperReference(BuildConfig.SHOPPER_REFERENCE)
                .build()

        val dropInConfiguration = DropInConfiguration.Builder(this@MainActivity, ExampleDropInService::class.java)
            .addCardConfiguration(cardConfiguration)
            .addGooglePayConfiguration(googlePayConfig)
            .build()

        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        DropIn.INSTANCE.startPayment(this@MainActivity, mPaymentMethodsApiResponse, dropInConfiguration, resultIntent)
    }
}
