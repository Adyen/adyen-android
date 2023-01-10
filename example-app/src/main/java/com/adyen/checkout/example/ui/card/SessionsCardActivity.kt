/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/1/2023.
 */

package com.adyen.checkout.example.ui.card

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardComponentProvider
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.example.databinding.ActivityCardBinding
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SessionsCardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private lateinit var binding: ActivityCardBinding

    private val cardViewModel: SessionsCardViewModel by viewModels()

    private var cardComponent: CardComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/sessions/card"
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
        // TODO sessions: change back to CardComponent.PROVIDER
        val cardComponent = CardComponentProvider().get(
            savedStateRegistryOwner = this,
            viewModelStoreOwner = this,
            lifecycleOwner = this,
            checkoutSession = sessionsCardComponentData.checkoutSession,
            paymentMethod = sessionsCardComponentData.paymentMethod,
            configuration = checkoutConfigurationProvider.getCardConfiguration(),
            application = application,
            defaultArgs = null,
            key = null,
            callback = sessionsCardComponentData.callback
        )

        this.cardComponent = cardComponent

        binding.cardView.attach(cardComponent, this)
    }

    private fun onCardEvent(event: CardEvent) {
        when (event) {
            is CardEvent.PaymentResult -> onPaymentResult(event.result)
            is CardEvent.AdditionalAction -> onAction(event.action)
        }
    }

    private fun onAction(action: Action) {
        cardComponent?.handleAction(action, this)
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cardComponent = null
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
