/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/12/2023.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.card.R
import com.adyen.checkout.card.databinding.AddressLookupViewBinding
import com.adyen.checkout.card.internal.data.model.LookupAddress
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
internal class AddressLookupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    private val binding: AddressLookupViewBinding = AddressLookupViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var cardDelegate: CardDelegate

    private var addressLookupOptionsAdapter: AddressLookupOptionsAdapter? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is CardDelegate) { "Unsupported delegate type" }
        cardDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        initAddressLookupQuery()
        initAddressFormInput(coroutineScope)
        initAddressOptions()
        initManualEntryErrorTextView()
        initManualEntryInitialTextView()
    }

    override fun highlightValidationErrors() {
        cardDelegate.outputData.let {
            binding.addressFormInput.highlightValidationErrors(false)
        }
    }

    private fun observeDelegate(delegate: CardDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutAddressLookupQuery.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_AddressLookupQuery,
            localizedContext
        )
        binding.addressFormInput.initLocalizedContext(localizedContext)
    }

    private fun initAddressLookupQuery() {
        val addressLookupQueryEditText = binding.textInputLayoutAddressLookupQuery.editText as? AdyenTextInputEditText
        addressLookupQueryEditText?.setOnChangeListener {
            cardDelegate.updateInputData {
                addressLookupQuery = it.toString()
                cardDelegate.onAddressQueryChanged(it.toString())
                binding.textInputLayoutAddressLookupQuery.hideError()
            }
        }

        addressLookupQueryEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val postalCodeValidation = cardDelegate.outputData.addressState.postalCode.validation
            if (hasFocus) {
                binding.textInputLayoutAddressLookupQuery.hideError()
            } else if (postalCodeValidation is Validation.Invalid) {
                binding.textInputLayoutAddressLookupQuery.showError(
                    localizedContext.getString(postalCodeValidation.reason)
                )
            }
        }
    }

    private fun initAddressFormInput(coroutineScope: CoroutineScope) {
        binding.addressFormInput.attachDelegate(cardDelegate, coroutineScope)
    }

    private fun initAddressOptions() {
        addressLookupOptionsAdapter = AddressLookupOptionsAdapter(::onAddressSelected)
        addressLookupOptionsAdapter?.let { adapter ->
            binding.recyclerViewAddressLookupOptions.adapter = adapter
        }
    }

    private fun initManualEntryErrorTextView() {
        binding.textViewManualEntryError.setOnClickListener {
            binding.textViewManualEntryError.isVisible = false
            binding.textViewError.isVisible = false
            binding.addressFormInput.isVisible = true
        }
    }

    private fun initManualEntryInitialTextView() {
        binding.textViewManualEntryInitial.setOnClickListener {
            binding.textViewManualEntryInitial.isVisible = false
            binding.addressFormInput.isVisible = true
        }
    }

    private fun outputDataChanged(outputData: CardOutputData) {
        setAddressOptions(outputData.addressLookupOptions, outputData.shouldDisplayAddressLookupError)
    }

    private fun setAddressOptions(options: List<LookupAddress>, shouldShowError: Boolean) {
        binding.textViewError.isVisible = shouldShowError
        binding.textViewManualEntryError.isVisible = shouldShowError
        binding.recyclerViewAddressLookupOptions.isVisible = options.isNotEmpty()
        if (options.isNotEmpty() || shouldShowError) {
            binding.textViewManualEntryInitial.isVisible = false
        }
        if (addressLookupOptionsAdapter == null) {
            initAddressOptions()
        }
        addressLookupOptionsAdapter?.submitList(options)
    }

    private fun onAddressSelected(lookupAddress: LookupAddress) {
        cardDelegate.updateInputData {
            this.address = lookupAddress.address
        }
        binding.recyclerViewAddressLookupOptions.isVisible = false
        binding.addressFormInput.isVisible = true
    }

    override fun getView(): View = this
}
