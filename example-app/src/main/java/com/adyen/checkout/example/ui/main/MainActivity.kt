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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInCallback
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.api.CheckoutApiService
import com.adyen.checkout.example.databinding.ActivityMainBinding
import com.adyen.checkout.example.ui.card.CardActivity
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.example.ui.configuration.ConfigurationActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DropInCallback {

    companion object {
        private val TAG: String = LogUtil.getTag()
    }

    private lateinit var binding: ActivityMainBinding
    private val paymentMethodsViewModel: PaymentMethodsViewModel by viewModels()

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private val dropInLauncher = DropIn.registerForDropInResult(this, this)

    private var isWaitingPaymentMethods = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Logger.d(TAG, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.componentList.adapter = ComponentItemAdapter(
            ComponentItemProvider.getComponentItems(),
            ::onComponentEntryClick
        )

        val result = DropIn.getDropInResultFromIntent(intent)
        if (result != null) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }

        paymentMethodsViewModel.paymentMethodResponseLiveData.observe(this) {
            if (it != null) {
                Logger.d(
                    TAG,
                    "Got paymentMethods response - oneClick? ${it.storedPaymentMethods?.size ?: 0}"
                )
                if (isWaitingPaymentMethods) startDropIn(it)
            } else {
                Logger.v(TAG, "API response is null")
            }
        }
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

    private fun onComponentEntryClick(entry: ComponentItem.Entry) {
        if (!CheckoutApiService.isRealUrlAvailable()) {
            Toast.makeText(
                this@MainActivity,
                "No server URL configured on local.gradle file.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        when (entry) {
            ComponentItem.Entry.DropIn -> {
                val currentResponse = paymentMethodsViewModel.paymentMethodResponseLiveData.value
                if (currentResponse != null) {
                    startDropIn(currentResponse)
                } else {
                    setLoading(true)
                }
            }
            ComponentItem.Entry.Card -> {
                val intent = Intent(this, CardActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun startDropIn(paymentMethodsApiResponse: PaymentMethodsApiResponse) {
        Logger.d(TAG, "startDropIn")
        setLoading(false)

        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        DropIn.startPayment(
            this,
            dropInLauncher,
            paymentMethodsApiResponse,
            checkoutConfigurationProvider.getDropInConfiguration(this),
        )
    }

    private fun setLoading(isLoading: Boolean) {
        isWaitingPaymentMethods = isLoading
        if (isLoading) {
            binding.componentList.isVisible = false
            binding.progressIndicator.show()
        } else {
            binding.componentList.isVisible = true
            binding.progressIndicator.hide()
        }
    }
}
