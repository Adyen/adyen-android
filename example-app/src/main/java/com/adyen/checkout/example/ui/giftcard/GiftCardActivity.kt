/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/4/2023.
 */

package com.adyen.checkout.example.ui.giftcard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.databinding.ActivityGiftCardBinding
import com.adyen.checkout.example.extensions.applyInsetsToRootLayout
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GiftCardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private lateinit var binding: ActivityGiftCardBinding

    private val giftCardViewModel: GiftCardViewModel by viewModels()

    private var giftCardComponent: GiftCardComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/giftcard"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        binding = ActivityGiftCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsetsToRootLayout(binding)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupReloadButton()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { giftCardViewModel.giftCardComponentDataFlow.collect(::setupGiftCardView) }
                launch { giftCardViewModel.giftCardViewStateFlow.collect(::onGiftCardViewState) }
                launch { giftCardViewModel.events.collect(::onGiftCardEvent) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            giftCardComponent?.handleIntent(intent)
        }
    }

    private fun onGiftCardViewState(giftCardViewState: GiftCardViewState) {
        when (giftCardViewState) {
            GiftCardViewState.Loading -> {
                // We are hiding the CardView here to display our own loading state. If you leave the view visible
                // the built in loading state will be shown.
                binding.progressIndicator.isVisible = true
                binding.giftCardContainer.isVisible = false
                binding.errorView.isVisible = false
            }
            GiftCardViewState.ShowComponent -> {
                binding.giftCardContainer.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.errorView.isVisible = false
            }
            GiftCardViewState.Error -> {
                binding.errorView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.giftCardContainer.isVisible = false
            }
        }
    }

    private fun setupGiftCardView(giftCardComponentData: GiftCardComponentData) {
        val giftCardComponent = GiftCardComponent.PROVIDER.get(
            activity = this,
            paymentMethod = giftCardComponentData.paymentMethod,
            checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig,
            callback = giftCardComponentData.callback,
        )

        this.giftCardComponent = giftCardComponent

        binding.giftCardView.attach(giftCardComponent, this)
    }

    private fun setupReloadButton() {
        binding.loadNewGiftCard.setOnClickListener {
            giftCardViewModel.reloadComponentWithOrder()
        }
    }

    private fun reloadGiftCardWithOrder(giftCardComponentData: GiftCardComponentData, orderRequest: OrderRequest) {
        val giftCardComponent = GiftCardComponent.PROVIDER.get(
            activity = this,
            paymentMethod = giftCardComponentData.paymentMethod,
            checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig,
            callback = giftCardComponentData.callback,
            order = orderRequest,
            key = KEY_SECONDARY_GIFT_CARD_COMPONENT
        )

        this.giftCardComponent = giftCardComponent
        binding.giftCardView.attach(giftCardComponent, this)
    }

    private fun onGiftCardEvent(event: GiftCardEvent) {
        when (event) {
            is GiftCardEvent.PaymentResult -> onPaymentResult(event.result)
            is GiftCardEvent.AdditionalAction -> onAction(event.action)
            is GiftCardEvent.Balance -> {
                giftCardComponent?.resolveBalanceResult(event.balanceResult)
            }
            is GiftCardEvent.OrderCreated -> {
                giftCardComponent?.resolveOrderResponse(event.order)
            }
            is GiftCardEvent.ReloadComponent -> {
                reloadGiftCardWithOrder(event.giftCardComponentData, event.orderRequest)
            }
            is GiftCardEvent.ReloadComponentSessions -> Unit
        }
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
    }

    private fun onAction(action: Action) {
        giftCardComponent?.handleAction(action, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        giftCardComponent = null
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"

        private const val KEY_SECONDARY_GIFT_CARD_COMPONENT = "KEY_SECONDARY_GIFT_CARD_COMPONENT"
    }
}
