/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.base.model.PaymentMethodsApiResponse
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.service.ExampleSimplifiedDropInService
import com.adyen.checkout.example.ui.configuration.ConfigurationActivity
import com.adyen.checkout.googlepay.GooglePayConfiguration
import kotlinx.android.synthetic.main.activity_main.progressBar
import kotlinx.android.synthetic.main.activity_main.startCheckoutButton
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG: String = LogUtil.getTag()
    }

    private val paymentMethodsViewModel: PaymentMethodsViewModel by viewModel()
    private val keyValueStorage: KeyValueStorage by inject()

    private var isWaitingPaymentMethods = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.d(TAG, "onCreate")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        if (intent.hasExtra(DropIn.RESULT_KEY)) {
            Toast.makeText(this, intent.getStringExtra(DropIn.RESULT_KEY), Toast.LENGTH_SHORT).show()
        }

        startCheckoutButton.setOnClickListener {
            if (!CheckoutApiService.isRealUrlAvailable()) {
                Toast.makeText(
                    this@MainActivity,
                    "No server URL configured on local.gradle file.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val currentResponse = paymentMethodsViewModel.paymentMethodResponseLiveData.value
            if (currentResponse != null) {
                startDropIn(currentResponse)
            } else {
                setLoading(true)
            }
        }

        paymentMethodsViewModel.paymentMethodResponseLiveData.observe(
            this,
            {
                if (it != null) {
                    Logger.d(TAG, "Got paymentMethods response - oneClick? ${it.storedPaymentMethods?.size ?: 0}")
                    if (isWaitingPaymentMethods) startDropIn(it)
                } else {
                    Logger.v(TAG, "API response is null")
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (CheckoutApiService.isRealUrlAvailable()) {
            paymentMethodsViewModel.requestPaymentMethods()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Logger.d(TAG, "onOptionsItemSelected")
        if (item.itemId == R.id.settings) {
            val intent = Intent(this@MainActivity, ConfigurationActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Logger.d(TAG, "onNewIntent")
        if (intent?.hasExtra(DropIn.RESULT_KEY) == true) {
            Toast.makeText(this, intent.getStringExtra(DropIn.RESULT_KEY), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DropIn.DROP_IN_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            Logger.d(TAG, "DropIn CANCELED")
        }
    }

    private fun startDropIn(paymentMethodsApiResponse: PaymentMethodsApiResponse) {
        Logger.d(TAG, "startDropIn")
        setLoading(false)

        val shopperLocaleString = keyValueStorage.getShopperLocale()
        val shopperLocale = LocaleUtil.fromLanguageTag(shopperLocaleString)

        val cardConfiguration = CardConfiguration.Builder(this@MainActivity)
            .setPublicKey(BuildConfig.PUBLIC_KEY)
            .setShopperReference(keyValueStorage.getShopperReference())
            .setShopperLocale(shopperLocale)
            .setEnvironment(Environment.TEST)
            .setHideCvcStoredCard(true)
            .build()

        val googlePayConfig = GooglePayConfiguration.Builder(this@MainActivity, keyValueStorage.getMerchantAccount())
            .setCountryCode(keyValueStorage.getCountry())
            .setEnvironment(Environment.TEST)
            .build()

        val bcmcConfiguration = BcmcConfiguration.Builder(this@MainActivity)
            .setPublicKey(BuildConfig.PUBLIC_KEY)
            .setShopperLocale(shopperLocale)
            .setEnvironment(Environment.TEST)
            .build()

        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val dropInConfigurationBuilder = DropInConfiguration.Builder(
            this@MainActivity,
            resultIntent,
            ExampleSimplifiedDropInService::class.java
        )
            .setEnvironment(Environment.TEST)
            .setClientKey(BuildConfig.CLIENT_KEY)
            .setShopperLocale(shopperLocale)
            .addCardConfiguration(cardConfiguration)
            .addBcmcConfiguration(bcmcConfiguration)
            .addGooglePayConfiguration(googlePayConfig)

        val amount = keyValueStorage.getAmount()

        try {
            dropInConfigurationBuilder.setAmount(amount)
        } catch (e: CheckoutException) {
            Logger.e(TAG, "Amount $amount not valid", e)
        }

        DropIn.startPayment(this@MainActivity, paymentMethodsApiResponse, dropInConfigurationBuilder.build())
    }

    private fun setLoading(isLoading: Boolean) {
        isWaitingPaymentMethods = isLoading
        if (isLoading) {
            startCheckoutButton.visibility = View.GONE
            progressBar.show()
        } else {
            startCheckoutButton.visibility = View.VISIBLE
            progressBar.hide()
        }
    }
}
