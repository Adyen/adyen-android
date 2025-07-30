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
import com.adyen.checkout.card.card
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.AnalyticsLevel
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.core.Environment
import com.adyen.checkout.example.BuildConfig
import com.adyen.checkout.example.data.storage.AnalyticsMode
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.databinding.ActivityCardBinding
import com.adyen.checkout.example.extensions.applyInsetsToRootLayout
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class MSCardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var keyValueStorage: KeyValueStorage

    private val shopperLocale: Locale?
        get() {
            val shopperLocaleString = keyValueStorage.getShopperLocale()
            return shopperLocaleString?.let { Locale.forLanguageTag(it) }
        }

    private lateinit var binding: ActivityCardBinding

    private val cardViewModel: MSCardViewModel by viewModels()

    private var cardComponent: CardComponent? = null

    // Component: When screen is shown, we initialize the card component
    private fun initializeCardComponent() = cardViewModel.viewModelScope.launch {
        val paymentMethods = cardViewModel.fetchPaymentMethods()
        val cardPaymentMethod =
            cardViewModel.getCardPaymentMethod(paymentMethods)
                ?: error("Card payment method not found")

        val cardConfiguration = CheckoutConfiguration(
            environment = Environment.TEST,
            clientKey = BuildConfig.CLIENT_KEY,
            shopperLocale = shopperLocale,
            amount = keyValueStorage.getAmount(),
            analyticsConfiguration = getAnalyticsConfiguration()
        ) {
            card {
                shopperReference = keyValueStorage.getShopperReference()
            }
        }

        cardComponent = CardComponent.PROVIDER.get(
            activity = this@MSCardActivity,
            paymentMethod = cardPaymentMethod,
            checkoutConfiguration = cardConfiguration,
            callback = cardViewModel,
        )

        binding.cardView.attach(cardComponent!!, this@MSCardActivity)
    }

    // Component: When there is a new result
    private fun onCardEvent(event: MSCardEvent) {
        when (event) {
            is MSCardEvent.Action -> cardComponent?.handleAction(event.action, this)

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

    // Redirect: Returned back to the app after a redirect
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            cardComponent?.handleIntent(intent)
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
        cardComponent = null
    }

    private fun getAnalyticsConfiguration(): AnalyticsConfiguration {
        val analyticsLevel = when (keyValueStorage.getAnalyticsMode()) {
            AnalyticsMode.ALL -> AnalyticsLevel.ALL
            AnalyticsMode.NONE -> AnalyticsLevel.NONE
        }
        return AnalyticsConfiguration(level = analyticsLevel)
    }

    companion object {
        private val TAG = getLogTag()
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
