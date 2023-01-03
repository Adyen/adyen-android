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
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.example.R
import com.adyen.checkout.example.databinding.ActivityBlikBinding
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BlikActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlikBinding

    private val blikViewModel: BlikViewModel by viewModels()

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private var blikComponent: BlikComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/blik"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        binding = ActivityBlikBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.blik_title)

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch { blikViewModel.blikViewState.collect(::onBlikViewState) }
                launch { blikViewModel.events.collect(::onBlikEvent) }
            }
        }
    }

    private fun onBlikViewState(blikViewState: BlikViewState) {
        when (blikViewState) {
            BlikViewState.Loading -> {
                binding.progressIndicator.isVisible = true
                binding.errorView.isVisible = false
                binding.componentView.isVisible = false
            }
            is BlikViewState.ShowComponent -> {
                binding.progressIndicator.isVisible = false
                binding.errorView.isVisible = false
                binding.componentView.isVisible = true

                setupBlikView(blikViewState.paymentMethod)
            }
            is BlikViewState.Await -> {
                binding.progressIndicator.isVisible = false
                binding.componentView.isVisible = true
                binding.errorView.isVisible = false
                blikComponent?.handleAction(blikViewState.action, this)
            }
            is BlikViewState.Error -> {
                binding.errorView.isVisible = true
                binding.errorView.text = getString(blikViewState.stringId)
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

        this.blikComponent = blikComponent

        binding.componentView.attach(blikComponent, this)
        blikComponent.observe(this, blikViewModel::onPaymentComponentEvent)
    }

    private fun onBlikEvent(event: BlikEvent) {
        when (event) {
            BlikEvent.Invalid -> {
                binding.componentView.highlightValidationErrors()
            }
            is BlikEvent.PaymentResult -> {
                onPaymentResult(event.result)
            }
            is BlikEvent.Unsupported -> {
                Toast.makeText(this, "This action is not implemented", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
