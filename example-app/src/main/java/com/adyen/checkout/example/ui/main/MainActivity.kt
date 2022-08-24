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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInCallback
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.example.R
import com.adyen.checkout.example.databinding.ActivityMainBinding
import com.adyen.checkout.example.service.ExampleFullAsyncDropInService
import com.adyen.checkout.example.service.ExampleSessionsDropInService
import com.adyen.checkout.example.ui.card.CardActivity
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.example.ui.configuration.ConfigurationActivity
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), DropInCallback {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private val dropInLauncher = DropIn.registerForDropInResult(this, this)

    private var componentItemAdapter: ComponentItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, RedirectComponent.getReturnUrl(applicationContext))

        Logger.d(TAG, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        componentItemAdapter = ComponentItemAdapter(
            viewModel::onComponentEntryClick
        )
        binding.componentList.adapter = componentItemAdapter

        val result = DropIn.getDropInResultFromIntent(intent)
        if (result != null) {
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.viewState.collect(::onViewState) }
                launch { viewModel.navigateTo.collect(::onNavigateTo) }
            }
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

    private fun onViewState(viewState: MainViewState) {
        when (viewState) {
            is MainViewState.Error -> {
                setLoading(false)
                Toast.makeText(this, viewState.message, Toast.LENGTH_SHORT).show()
            }
            MainViewState.Loading -> setLoading(true)
            is MainViewState.Result -> {
                setLoading(false)
                componentItemAdapter?.items = viewState.items
            }
        }
    }

    private fun onNavigateTo(navigation: MainNavigation) {
        when (navigation) {
            MainNavigation.Card -> {
                val intent = Intent(this, CardActivity::class.java)
                startActivity(intent)
            }
            is MainNavigation.DropIn -> {
                DropIn.startPayment(
                    this,
                    dropInLauncher,
                    navigation.paymentMethodsApiResponse,
                    checkoutConfigurationProvider.getDropInConfiguration(this),
                    ExampleFullAsyncDropInService::class.java,
                )
            }
            is MainNavigation.DropInWithSession -> {
                DropIn.startPaymentWithSession(
                    this,
                    dropInLauncher,
                    navigation.session,
                    checkoutConfigurationProvider.getDropInConfiguration(this)
                )
            }
            is MainNavigation.DropInWithCustomSession -> {
                DropIn.startPaymentWithSession(
                    this,
                    dropInLauncher,
                    navigation.session,
                    checkoutConfigurationProvider.getDropInConfiguration(this),
                    ExampleSessionsDropInService::class.java
                )
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressIndicator.show()
        } else {
            binding.progressIndicator.hide()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        componentItemAdapter = null
    }

    companion object {
        private val TAG = LogUtil.getTag()

        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
