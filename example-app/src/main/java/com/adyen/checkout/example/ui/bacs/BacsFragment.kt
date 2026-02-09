/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/1/2023.
 */

package com.adyen.checkout.example.ui.bacs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.example.databinding.FragmentBacsBinding
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.ui.configuration.CheckoutConfigurationProvider
import com.adyen.checkout.redirect.old.RedirectComponent
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class BacsFragment : BottomSheetDialogFragment() {

    @Inject
    internal lateinit var checkoutConfigurationProvider: CheckoutConfigurationProvider

    private var _binding: FragmentBacsBinding? = null
    private val binding: FragmentBacsBinding get() = requireNotNull(_binding)

    private val viewModel: BacsViewModel by viewModels()

    private var bacsComponent: BacsDirectDebitComponent? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(requireActivity().applicationContext)
        arguments = (arguments ?: bundleOf()).apply {
            putString(RETURN_URL_EXTRA, returnUrl)
        }

        _binding = FragmentBacsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.bacsComponentDataFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { setupBacsComponent(it) }
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : BottomSheetDialog(requireContext(), theme) {
            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                onBackPressedDispatcher.addCallback(this@BacsFragment) {
                    if (bacsComponent?.handleBackPress() == true) return@addCallback
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        }
    }

    private fun setupBacsComponent(bacsComponentData: BacsComponentData) {
        val bacsComponent = BacsDirectDebitComponent.PROVIDER.get(
            fragment = this,
            paymentMethod = bacsComponentData.paymentMethod,
            checkoutConfiguration = checkoutConfigurationProvider.checkoutConfig,
            callback = bacsComponentData.callback,
        )

        this.bacsComponent = bacsComponent

        binding.componentView.attach(bacsComponent, viewLifecycleOwner)
    }

    private fun onViewState(state: BacsViewState) {
        when (state) {
            is BacsViewState.Error -> {
                binding.errorView.isVisible = true
                val errorMessage = if (state.arg != null) {
                    getString(state.message, state.arg)
                } else {
                    getString(state.message)
                }
                binding.errorView.text = errorMessage
                binding.componentView.isVisible = false
                binding.progressIndicator.isVisible = false
            }

            BacsViewState.Loading -> {
                binding.errorView.isVisible = false
                binding.componentView.isVisible = false
                binding.progressIndicator.isVisible = true
            }

            BacsViewState.ShowComponent -> {
                binding.errorView.isVisible = false
                binding.componentView.isVisible = true
                binding.progressIndicator.isVisible = false
            }
        }
    }

    private fun onEvent(event: BacsEvent) {
        when (event) {
            is BacsEvent.AdditionalAction -> bacsComponent?.handleAction(event.action, requireActivity())
            is BacsEvent.PaymentResult -> onPaymentResult(event.result)
        }
    }

    private fun onPaymentResult(result: String) {
        Toast.makeText(requireContext(), result, Toast.LENGTH_SHORT).show()
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bacsComponent = null
    }

    companion object {

        private val TAG = getLogTag()

        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"

        fun show(fragmentManager: FragmentManager) {
            BacsFragment().show(fragmentManager, TAG)
        }
    }
}
