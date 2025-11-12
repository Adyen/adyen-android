/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/1/2023.
 */

package com.adyen.checkout.example.ui.card

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.card.old.CardComponent
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.databinding.ActivityCardBinding
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.old.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class SessionsCardTakenOverActivity : AppCompatActivity(), AddressLookupCallback {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private lateinit var binding: ActivityCardBinding

    private val cardViewModel: SessionsCardTakenOverViewModel by viewModels()

    private var cardComponent: CardComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/sessions/card/takenover"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { cardViewModel.sessionsCardComponentDataFlow.collect(::setupCardView) }
                launch { cardViewModel.cardViewState.collect(::onCardViewState) }
                launch { cardViewModel.events.collect(::onCardEvent) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            cardComponent?.handleIntent(intent)
        }
    }

    private fun onCardViewState(cardViewState: CardViewState) {
        when (cardViewState) {
            CardViewState.Loading -> {
                // We are hiding the CardView here to display our own loading state. If you leave the view visible
                // the built in loading state will be shown.
                binding.progressIndicator.isVisible = true
                binding.cardContainer.isVisible = false
                binding.errorView.isVisible = false
            }

            is CardViewState.ShowComponent -> {
                binding.cardContainer.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.errorView.isVisible = false
            }

            CardViewState.Error -> {
                binding.errorView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.cardContainer.isVisible = false
            }
        }
    }

    private fun setupCardView(sessionsCardComponentData: SessionsCardComponentData) {
        val cardComponent = CardComponent.PROVIDER.get(
            activity = this,
            checkoutSession = sessionsCardComponentData.checkoutSession,
            paymentMethod = sessionsCardComponentData.paymentMethod,
            checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig,
            componentCallback = sessionsCardComponentData.callback,
        )

        cardComponent.setOnRedirectListener {
            Log.d(TAG, "On redirect")
        }

        cardComponent.setAddressLookupCallback(this)

        this.cardComponent = cardComponent

        binding.cardView.attach(cardComponent, this)
    }

    private fun onCardEvent(event: CardEvent) {
        when (event) {
            is CardEvent.PaymentResult -> onPaymentResult(event.result)
            is CardEvent.AdditionalAction -> onAction(event.action)
            is CardEvent.AddressLookup -> onAddressLookup(event.options)
            is CardEvent.AddressLookupCompleted -> onAddressLookupCompleted(event.lookupAddress)
            is CardEvent.AddressLookupError -> onAddressLookupError(event.message)
        }
    }

    private fun onAction(action: Action) {
        cardComponent?.handleAction(action, this)
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onAddressLookup(options: List<LookupAddress>) {
        cardComponent?.updateAddressLookupOptions(options)
    }

    override fun onQueryChanged(query: String) {
        Log.d(TAG, "On address lookup query changed: $query")
        cardViewModel.onAddressLookupQueryChanged(query)
    }

    override fun onLookupCompletion(lookupAddress: LookupAddress): Boolean {
        Log.d(TAG, "On address lookup completion: $lookupAddress")
        cardViewModel.onAddressLookupCompletion(lookupAddress)
        return true
    }

    private fun onAddressLookupCompleted(lookupAddress: LookupAddress) {
        cardComponent?.setAddressLookupResult(AddressLookupResult.Completed(lookupAddress))
    }

    private fun onAddressLookupError(message: String) {
        cardComponent?.setAddressLookupResult(AddressLookupResult.Error(message))
    }

    override fun onDestroy() {
        super.onDestroy()
        cardComponent = null
    }

    companion object {
        private val TAG = getLogTag()
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
