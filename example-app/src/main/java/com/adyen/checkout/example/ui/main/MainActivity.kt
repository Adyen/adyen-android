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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.adyen.checkout.dropin.DropIn
import com.adyen.checkout.dropin.DropInCallback
import com.adyen.checkout.dropin.SessionDropInCallback
import com.adyen.checkout.example.R
import com.adyen.checkout.example.databinding.ActivityMainBinding
import com.adyen.checkout.example.extensions.applyInsetsToRootLayout
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.service.ExampleAdvancedDropInService
import com.adyen.checkout.example.service.ExampleSessionsDropInService
import com.adyen.checkout.example.ui.bacs.BacsFragment
import com.adyen.checkout.example.ui.blik.BlikActivity
import com.adyen.checkout.example.ui.card.CardActivity
import com.adyen.checkout.example.ui.card.SessionsCardTakenOverActivity
import com.adyen.checkout.example.ui.card.compose.SessionsCardActivity
import com.adyen.checkout.example.ui.giftcard.GiftCardActivity
import com.adyen.checkout.example.ui.giftcard.SessionsGiftCardActivity
import com.adyen.checkout.example.ui.googlepay.GooglePayFragment
import com.adyen.checkout.example.ui.googlepay.compose.SessionsGooglePayActivity
import com.adyen.checkout.example.ui.instant.InstantFragment
import com.adyen.checkout.example.ui.settings.SettingsActivity
import com.adyen.checkout.example.ui.v6.V6Activity
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    private val dropInLauncher = DropIn.registerForDropInResult(
        this,
        DropInCallback { dropInResult -> viewModel.onDropInResult(dropInResult) },
    )

    private val sessionDropInLauncher = DropIn.registerForDropInResult(
        this,
        SessionDropInCallback { sessionDropInResult -> viewModel.onDropInResult(sessionDropInResult) },
    )

    private var componentItemAdapter: ComponentItemAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, RedirectComponent.getReturnUrl(applicationContext))

        Log.d(TAG, "onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsetsToRootLayout(binding)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.switchSessions.setOnCheckedChangeListener { _, isChecked -> viewModel.onSessionsToggled(isChecked) }

        componentItemAdapter = ComponentItemAdapter(
            viewModel::onComponentEntryClick,
        )
        binding.componentList.adapter = componentItemAdapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.mainViewState.collect(::onMainViewState) }
                launch { viewModel.eventFlow.collect(::onMainEvent) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        when (intent.data?.path) {
            InstantFragment.RETURN_URL_PATH -> {
                (supportFragmentManager.findFragmentByTag(InstantFragment.TAG) as? InstantFragment)
                    ?.onNewIntent(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        if (item.itemId == R.id.settings) {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onMainViewState(mainViewState: MainViewState) {
        onListItems(mainViewState.listItems)
        setLoading(mainViewState.showLoading)
        setUseSessionsSwitchChecked(mainViewState.useSessions)
    }

    private fun onListItems(items: List<ComponentItem>) {
        componentItemAdapter?.submitList(items)
    }

    private fun onMainEvent(event: MainEvent) {
        when (event) {
            is MainEvent.NavigateTo -> onNavigateTo(event.destination)
            is MainEvent.Toast -> Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    private fun onNavigateTo(navigation: MainNavigation) {
        when (navigation) {
            is MainNavigation.DropIn -> {
                DropIn.startPayment(
                    this,
                    dropInLauncher,
                    navigation.paymentMethodsApiResponse,
                    navigation.checkoutConfiguration,
                    ExampleAdvancedDropInService::class.java,
                )
            }

            is MainNavigation.DropInWithSession -> {
                DropIn.startPayment(
                    this,
                    sessionDropInLauncher,
                    navigation.checkoutSession,
                    navigation.checkoutConfiguration,
                )
            }

            is MainNavigation.DropInWithCustomSession -> {
                DropIn.startPayment(
                    this,
                    sessionDropInLauncher,
                    navigation.checkoutSession,
                    navigation.checkoutConfiguration,
                    ExampleSessionsDropInService::class.java,
                )
            }

            is MainNavigation.Bacs -> {
                BacsFragment.show(supportFragmentManager)
            }

            is MainNavigation.Blik -> {
                startActivity(Intent(this, BlikActivity::class.java))
            }

            is MainNavigation.Card -> {
                startActivity(Intent(this, CardActivity::class.java))
            }

            is MainNavigation.CardWithSession -> {
                startActivity(Intent(this, SessionsCardActivity::class.java))
            }

            is MainNavigation.GiftCard -> {
                startActivity(Intent(this, GiftCardActivity::class.java))
            }

            is MainNavigation.GiftCardWithSession -> {
                startActivity(Intent(this, SessionsGiftCardActivity::class.java))
            }

            is MainNavigation.CardWithSessionTakenOver -> {
                startActivity(Intent(this, SessionsCardTakenOverActivity::class.java))
            }

            is MainNavigation.Instant -> {
                InstantFragment.show(supportFragmentManager, navigation.paymentMethodType)
            }

            is MainNavigation.GooglePay -> {
                GooglePayFragment.show(supportFragmentManager)
            }

            is MainNavigation.GooglePayWithSession -> {
                startActivity(Intent(this, SessionsGooglePayActivity::class.java))
            }

            is MainNavigation.V6 -> {
                startActivity(Intent(this, V6Activity::class.java))
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

    private fun setUseSessionsSwitchChecked(isChecked: Boolean) {
        binding.switchSessions.isChecked = isChecked
    }

    override fun onDestroy() {
        super.onDestroy()
        componentItemAdapter = null
    }

    companion object {
        private val TAG = getLogTag()

        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
