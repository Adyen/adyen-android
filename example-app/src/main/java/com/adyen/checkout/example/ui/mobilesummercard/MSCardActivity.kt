/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/7/2025.
 */

package com.adyen.checkout.example.ui.mobilesummercard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.example.databinding.ActivityCardBinding
import com.adyen.checkout.example.extensions.applyInsetsToRootLayout
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MSCardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider
    private lateinit var binding: ActivityCardBinding
    private val cardViewModel: MSCardViewModel by viewModels()
    lateinit var cardComponent: CardComponent

    // Component: When screen is shown, we initialize the card component
    private fun initializeCardComponent() = cardViewModel.viewModelScope.launch {
        val paymentMethods = cardViewModel.fetchPaymentMethods()
        val cardPaymentMethod =
            cardViewModel.getCardPaymentMethod(paymentMethods)
                ?: error("Card payment method not found")

        cardComponent = CardComponent.PROVIDER.get(
            activity = this@MSCardActivity,
            paymentMethod = cardPaymentMethod,
            checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig,
            callback = cardViewModel,
        )
        cardViewModel.cardComponent = cardComponent

        binding.cardView.attach(cardComponent, this@MSCardActivity)
    }

    // Redirect: Returned back to the app after a redirect
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Successfully redirected back (Docs: https://docs.adyen.com/online-payments/build-your-integration/sessions-flow/?platform=Android&integration=Components&version=5.13.1&utm_source=online-payments-integration&utm_medium=banner&utm_id=v6-banner#handle-a-redirect)
    }

    // Component: When there is a new result
    private fun onCardEvent(event: MSCardEvent) {
        when (event) {
            is MSCardEvent.PaymentFinished -> {
                Toast.makeText(this, event.result, Toast.LENGTH_SHORT).show()
                finish()
            }

            is MSCardEvent.Error -> {
                Toast.makeText(this, event.result, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    // UI: Screen is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/card"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsetsToRootLayout(binding)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        cardViewModel.activity = this
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { cardViewModel.events.collect(::onCardEvent) }
            }
        }

        initializeCardComponent()
    }

    // UI: Screen is removed
    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private val TAG = getLogTag()
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
