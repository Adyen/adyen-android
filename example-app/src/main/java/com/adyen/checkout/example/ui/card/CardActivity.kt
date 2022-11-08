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
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.example.databinding.ActivityCardBinding
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private lateinit var binding: ActivityCardBinding

    private val cardViewModel: CardViewModel by viewModels()

    private var redirectComponent: RedirectComponent? = null

    private var threeDS2Component: Adyen3DS2Component? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/card"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        binding = ActivityCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.payButton.setOnClickListener { cardViewModel.onPayClick() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { cardViewModel.cardViewState.collect(::onCardViewState) }
                launch { cardViewModel.events.collect(::onCardEvent) }
            }
        }

        cardViewModel.onCreate()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            redirectComponent?.handleIntent(intent)
            threeDS2Component?.handleIntent(intent)
        }
    }

    private fun onCardViewState(cardViewState: CardViewState) {
        when (cardViewState) {
            CardViewState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.cardContainer.isVisible = false
                binding.errorView.isVisible = false
            }
            is CardViewState.ShowComponent -> {
                binding.cardContainer.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.errorView.isVisible = false

                setupCardView(cardViewState.paymentMethod)
            }
            CardViewState.Error -> {
                binding.errorView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.cardContainer.isVisible = false
            }
        }
    }

    private fun setupCardView(paymentMethod: PaymentMethod) {
        val cardComponent = CardComponent.PROVIDER.get(
            this,
            paymentMethod,
            checkoutConfigurationProvider.getCardConfiguration()
        )

        binding.cardView.attach(cardComponent, this)

        cardComponent.observe(this, cardViewModel::onPaymentComponentEvent)
    }

    private fun onCardEvent(event: CardEvent) {
        when (event) {
            is CardEvent.PaymentResult -> onPaymentResult(event.result)
            is CardEvent.AdditionalAction -> onAdditionalAction(event.action)
            CardEvent.Invalid -> binding.cardView.highlightValidationErrors()
        }
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onAdditionalAction(cardAction: CardAction) {
        when (cardAction) {
            is CardAction.Redirect -> setupRedirectComponent(cardAction.action)
            is CardAction.ThreeDS2 -> setupThreeDS2Component(cardAction.action)
            CardAction.Unsupported -> {
                Toast.makeText(this, "This action is not implemented", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRedirectComponent(action: Action) {
        redirectComponent = RedirectComponent.PROVIDER.get(
            this,
            application,
            checkoutConfigurationProvider.getRedirectConfiguration()
        ).apply {
            observe(this@CardActivity, cardViewModel::onActionComponentEvent)
            handleAction(action, this@CardActivity)
        }
    }

    private fun setupThreeDS2Component(action: Action) {
        threeDS2Component = Adyen3DS2Component.PROVIDER.get(
            this,
            application,
            checkoutConfigurationProvider.get3DS2Configuration()
        ).apply {
            observe(this@CardActivity, cardViewModel::onActionComponentEvent)
            handleAction(action, this@CardActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        redirectComponent = null
        threeDS2Component = null
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
