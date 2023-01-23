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
import com.adyen.authentication.AuthenticationLauncher
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
class CardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private lateinit var binding: ActivityCardBinding

    private val cardViewModel: CardViewModel by viewModels()

    private var cardComponent: CardComponent? = null
    private var authenticationLauncher: AuthenticationLauncher? = null

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

        initAuthenticationLauncher()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { cardViewModel.cardComponentDataFlow.collect(::setupCardView) }
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

    private fun initAuthenticationLauncher() {
        try {
            authenticationLauncher = AuthenticationLauncher(this)
        } catch (e: Throwable) {
            // ignore as Adyen Authentication SDK is not added to the project
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

    private fun setupCardView(cardComponentData: CardComponentData) {
        // TODO sessions: change back to CardComponent.PROVIDER
        val cardComponent = CardComponentProvider().get(
            savedStateRegistryOwner = this,
            viewModelStoreOwner = this,
            lifecycleOwner = this,
            paymentMethod = cardComponentData.paymentMethod,
            configuration = checkoutConfigurationProvider.getCardConfiguration(),
            application = application,
            defaultArgs = null,
            key = null,
            componentCallback = cardComponentData.callback
        )
        authenticationLauncher?.let {
            cardComponent.initDelegatedAuthentication(it)
        }

        this.cardComponent = cardComponent

        binding.cardView.attach(cardComponent, this)
    }

    private fun onCardEvent(event: CardEvent) {
        when (event) {
            is CardEvent.PaymentResult -> onPaymentResult(event.result)
            is CardEvent.AdditionalAction -> onAction(event.action)
        }
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun onAction(action: Action) {
        cardComponent?.handleAction(action, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cardComponent = null
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
