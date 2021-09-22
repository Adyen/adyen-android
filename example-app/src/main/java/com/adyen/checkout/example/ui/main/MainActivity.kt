/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInCallback
import com.adyen.checkout.dropin.DropInConfiguration
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.databinding.ActivityMainBinding
import com.adyen.checkout.example.service.ExampleAsyncDropInService
import com.adyen.checkout.example.ui.configuration.ConfigurationActivity
import com.adyen.checkout.googlepay.GooglePayConfiguration
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity(), DropInCallback {

    companion object {
        private val TAG: String = LogUtil.getTag()
    }

    private lateinit var binding: ActivityMainBinding
    private val paymentMethodsViewModel: PaymentMethodsViewModel by viewModel()
    private val keyValueStorage: KeyValueStorage by inject()

    private val dropInLauncher = DropIn.registerForDropInResult(this, this)

    private var isWaitingPaymentMethods = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.d(TAG, "onCreate")
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(findViewById(R.id.toolbar))

        val result = DropIn.getDropInResultFromIntent(intent)
        if (result != null) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }

        binding.startCheckoutButton.setOnClickListener {
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
        if (intent == null) return
        val result = DropIn.getDropInResultFromIntent(intent) ?: return
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
    }

    override fun onDropInResult(dropInResult: DropInResult?) {
        if (dropInResult == null) return
        when (dropInResult) {
            is DropInResult.CancelledByUser -> Toast.makeText(this, "Canceled by user", Toast.LENGTH_SHORT).show()
            is DropInResult.Error -> Toast.makeText(this, dropInResult.reason, Toast.LENGTH_SHORT).show()
            is DropInResult.Finished -> Toast.makeText(this, dropInResult.result, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startDropIn(paymentMethodsApiResponse: PaymentMethodsApiResponse) {
        Logger.d(TAG, "startDropIn")
        setLoading(false)

        val shopperLocaleString = keyValueStorage.getShopperLocale()
        val shopperLocale = LocaleUtil.fromLanguageTag(shopperLocaleString)

        val amount = keyValueStorage.getAmount()

        val cardConfiguration = CardConfiguration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
            .setShopperReference(keyValueStorage.getShopperReference())
            .build()

        val googlePayConfig = GooglePayConfiguration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
            .setCountryCode(keyValueStorage.getCountry())
            .setAmount(amount)
            .build()

        val bcmcConfiguration = BcmcConfiguration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
            .setShopperReference(keyValueStorage.getShopperReference())
            .setShowStorePaymentField(true)
            .build()

        val adyen3DS2Configuration = Adyen3DS2Configuration.Builder(shopperLocale, Environment.TEST, BuildConfig.CLIENT_KEY)
            .build()

        val dropInConfigurationBuilder = DropInConfiguration.Builder(
            this@MainActivity,
            ExampleAsyncDropInService::class.java,
            BuildConfig.CLIENT_KEY
        )
            .setEnvironment(Environment.TEST)
            .setShopperLocale(shopperLocale)
            .addCardConfiguration(cardConfiguration)
            .addBcmcConfiguration(bcmcConfiguration)
            .addGooglePayConfiguration(googlePayConfig)
            .add3ds2ActionConfiguration(adyen3DS2Configuration)

        try {
            dropInConfigurationBuilder.setAmount(amount)
        } catch (e: CheckoutException) {
            Logger.e(TAG, "Amount $amount not valid", e)
        }

        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        DropIn.startPayment(this, dropInLauncher, paymentMethodsApiResponse, dropInConfigurationBuilder.build(), resultIntent)
    }

    private fun setLoading(isLoading: Boolean) {
        isWaitingPaymentMethods = isLoading
        if (isLoading) {
            binding.startCheckoutButton.visibility = View.GONE
            binding.progressBar.show()
        } else {
            binding.startCheckoutButton.visibility = View.VISIBLE
            binding.progressBar.hide()
        }
    }
}
