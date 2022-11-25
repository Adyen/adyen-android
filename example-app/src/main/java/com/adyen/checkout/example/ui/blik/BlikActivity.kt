/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/11/2022.
 */

package com.adyen.checkout.example.ui.blik

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.blik.BlikComponent
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.example.databinding.ActivityBilkBinding
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ACTION_FRAGMENT_TAG = "ACTION_DIALOG_FRAGMENT"

@AndroidEntryPoint
class BlikActivity : AppCompatActivity(), BlikListener {
    private lateinit var binding: ActivityBilkBinding
    private val blikViewModel: BlikViewModel by viewModels()

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/blik"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)
        binding = ActivityBilkBinding.inflate(layoutInflater)
        binding.payButton.setOnClickListener { blikViewModel.onPayClick() }
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { blikViewModel.blikViewState.collect(::onBlikViewState) }
                launch { blikViewModel.events.collect(::onBlikEvent) }
            }
        }
        blikViewModel.onCreate()
    }

    private fun onBlikViewState(blikViewState: BlikViewState) {
        when (blikViewState) {
            BlikViewState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.errorView.isVisible = false
                binding.blikView.isVisible = false
                binding.blinkLogo.isVisible = false
                binding.payButton.isVisible = false
            }
            is BlikViewState.ShowComponent -> {
                binding.progressIndicator.isVisible = false
                binding.errorView.isVisible = false
                binding.blikView.isVisible = true
                binding.payButton.isVisible = true
                binding.blinkLogo.isVisible = true

                setupBlikView(blikViewState.paymentMethod)
            }
            BlikViewState.Error -> {
                binding.errorView.isVisible = true
                binding.progressIndicator.isVisible = false
            }
        }
    }

    private fun setupBlikView(paymentMethod: PaymentMethod) {
        val blikComponent = BlikComponent.PROVIDER.get(
            this,
            paymentMethod,
            checkoutConfigurationProvider.getBlikConfiguration(),
            application
        )
        binding.blikView.attach(blikComponent, this)
        blikComponent.observe(this, blikViewModel::onPaymentComponentEvent)
    }

    private fun onBlikEvent(event: BlikEvent) {
        when (event) {
            BlikEvent.Invalid -> {
                binding.blikView.highlightValidationErrors()
            }
            is BlikEvent.PaymentResult -> {
                onPaymentResult(event.result)
            }
            is BlikEvent.AdditionalAction -> {
                onAdditionalAction(event.action)
            }
        }
    }

    private fun onAdditionalAction(blikAction: BlikAction) {
        when (blikAction) {
            is BlikAction.Awaiting -> {
                val actionFragment = BlikBottomSheetFragment.newInstance(
                    blikAction.action,
                    checkoutConfigurationProvider.getAwaitConfiguration()
                )
                actionFragment.show(supportFragmentManager, ACTION_FRAGMENT_TAG)
            }
            is BlikAction.Unsupported -> {
                Toast.makeText(this, "This action is not implemented", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActionComponentEvent(event: ActionComponentEvent) {
        blikViewModel.onActionComponentEvent(event)
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
