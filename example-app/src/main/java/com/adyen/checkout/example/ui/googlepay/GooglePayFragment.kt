/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.example.databinding.FragmentGooglePayBinding
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.redirect.RedirectComponent
import com.google.android.gms.wallet.button.ButtonConstants.ButtonType
import com.google.android.gms.wallet.button.ButtonOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class GooglePayFragment : BottomSheetDialogFragment() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private var _binding: FragmentGooglePayBinding? = null
    private val binding: FragmentGooglePayBinding get() = requireNotNull(_binding)

    private val viewModel: GooglePayViewModel by viewModels()

    private var googlePayComponent: GooglePayComponent? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(requireActivity().applicationContext)
        arguments = (arguments ?: bundleOf()).apply {
            putString(RETURN_URL_EXTRA, returnUrl)
        }

        _binding = FragmentGooglePayBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.googleComponentDataFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { setupGooglePayComponent(it) }
            .launchIn(lifecycleScope)

        viewModel.viewState
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { onViewState(it) }
            .launchIn(lifecycleScope)

        viewModel.events
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { onEvent(it) }
            .launchIn(lifecycleScope)
    }

    private fun setupGooglePayComponent(googlePayComponentData: GooglePayComponentData) {
        val googlePayComponent = with(googlePayComponentData) {
            GooglePayComponent.PROVIDER.get(
                fragment = this@GooglePayFragment,
                paymentMethod = paymentMethod,
                configuration = googlePayConfiguration,
                callback = callback,
            )
        }

        this.googlePayComponent = googlePayComponent

        binding.componentView.attach(googlePayComponent, viewLifecycleOwner)

        loadGooglePayButton()
    }

    private fun onViewState(state: GooglePayViewState) {
        when (state) {
            is GooglePayViewState.Error -> {
                binding.errorView.isVisible = true
                binding.errorView.text = getString(state.message)
                binding.componentView.isVisible = false
                binding.progressIndicator.isVisible = false
                binding.googlePayButton.isVisible = false
            }

            GooglePayViewState.Loading -> {
                binding.errorView.isVisible = false
                binding.componentView.isVisible = false
                binding.progressIndicator.isVisible = true
                binding.googlePayButton.isVisible = false
            }

            GooglePayViewState.ShowButton -> {
                binding.errorView.isVisible = false
                binding.componentView.isVisible = false
                binding.progressIndicator.isVisible = false
                binding.googlePayButton.isVisible = true
            }

            GooglePayViewState.ShowComponent -> {
                binding.errorView.isVisible = false
                binding.componentView.isVisible = true
                binding.progressIndicator.isVisible = false
                binding.googlePayButton.isVisible = false
            }
        }
    }

    private fun onEvent(event: GooglePayEvent) {
        when (event) {
            is GooglePayEvent.AdditionalAction -> googlePayComponent?.handleAction(event.action, requireActivity())
            is GooglePayEvent.PaymentResult -> onPaymentResult(event.result)
        }
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    private fun loadGooglePayButton() {
        val allowedPaymentMethods = googlePayComponent?.getGooglePayButtonParameters()?.allowedPaymentMethods.orEmpty()
        val buttonOptions = ButtonOptions
            .newBuilder()
            .setButtonType(ButtonType.PAY)
            .setAllowedPaymentMethods(allowedPaymentMethods)
            .build()
        binding.googlePayButton.initialize(buttonOptions)

        binding.googlePayButton.setOnClickListener {
            googlePayComponent?.startGooglePayScreen(requireActivity(), ACTIVITY_RESULT_CODE)
        }
    }

    // It is required to use onActivityResult with the Google Pay library (AutoResolveHelper).
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ACTIVITY_RESULT_CODE) {
            googlePayComponent?.handleActivityResult(resultCode, data)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        googlePayComponent = null
    }

    companion object {

        internal val TAG = getLogTag()

        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
        internal const val ACTIVITY_RESULT_CODE = 1

        fun show(fragmentManager: FragmentManager) {
            GooglePayFragment().show(fragmentManager, TAG)
        }
    }
}
