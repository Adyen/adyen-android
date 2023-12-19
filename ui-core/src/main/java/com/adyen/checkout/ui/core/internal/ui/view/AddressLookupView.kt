/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/12/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.RestrictTo
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.databinding.AddressLookupViewBinding
import com.adyen.checkout.ui.core.internal.ui.AddressLookupDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupEvent
import com.adyen.checkout.ui.core.internal.ui.model.AddressLookupState
import com.adyen.checkout.ui.core.internal.ui.model.LookupAddress
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class AddressLookupView @JvmOverloads constructor(
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

    private lateinit var addressLookupDelegate: AddressLookupDelegate

    private var addressLookupOptionsAdapter: AddressLookupOptionsAdapter? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is AddressLookupDelegate) { "Unsupported delegate type" }
        addressLookupDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        addressLookupDelegate.updateAddressLookupInputData {
            reset()
        }

        observeDelegate(delegate, coroutineScope)

        initAddressLookupQuery()
        initAddressFormInput(coroutineScope)
        initAddressOptions()
        initManualEntryErrorTextView()
        initManualEntryInitialTextView()
    }

    override fun highlightValidationErrors() {
        binding.addressFormInput.highlightValidationErrors(false)
    }

    private fun observeDelegate(delegate: AddressLookupDelegate, coroutineScope: CoroutineScope) {
        delegate.addressLookupStateFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutAddressLookupQuery.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_AddressLookup_Query,
            localizedContext
        )
        binding.addressFormInput.initLocalizedContext(localizedContext)
    }

    private fun initAddressLookupQuery() {
        val addressLookupQueryEditText = binding.textInputLayoutAddressLookupQuery.editText as? AdyenTextInputEditText
        addressLookupQueryEditText?.setOnChangeListener {
            onQueryChanged(it)
        }
    }

    private fun initAddressFormInput(coroutineScope: CoroutineScope) {
        binding.addressFormInput.attachDelegate(addressLookupDelegate.addressDelegate, coroutineScope)
    }

    private fun initAddressOptions() {
        addressLookupOptionsAdapter = AddressLookupOptionsAdapter(::onAddressSelected)
        addressLookupOptionsAdapter?.let { adapter ->
            binding.recyclerViewAddressLookupOptions.adapter = adapter
        }
    }

    private fun initManualEntryErrorTextView() {
        binding.textViewManualEntryError.setOnClickListener {
            clearQuery()
            addressLookupDelegate.addressLookupEventChannel.trySend(AddressLookupEvent.Manual)
        }
    }

    private fun initManualEntryInitialTextView() {
        binding.textViewManualEntryInitial.setOnClickListener {
            clearQuery()
            addressLookupDelegate.addressLookupEventChannel.trySend(AddressLookupEvent.Manual)
        }
    }

    private fun outputDataChanged(addressLookupState: AddressLookupState) {
        when (addressLookupState) {
            AddressLookupState.Error -> {
                binding.recyclerViewAddressLookupOptions.isVisible = false
                binding.textViewManualEntryInitial.isVisible = false
                binding.textViewError.isVisible = true
                binding.textViewManualEntryError.isVisible = true
                binding.addressFormInput.isVisible = false
                binding.progressBar.isVisible = false
            }

            AddressLookupState.Initial -> {
                binding.recyclerViewAddressLookupOptions.isVisible = false
                binding.textViewManualEntryInitial.isVisible = true
                binding.textViewError.isVisible = false
                binding.textViewManualEntryError.isVisible = false
                binding.addressFormInput.isVisible = false
                binding.progressBar.isVisible = false
            }

            AddressLookupState.Loading -> {
                binding.recyclerViewAddressLookupOptions.isVisible = false
                binding.textViewManualEntryInitial.isVisible = false
                binding.textViewError.isVisible = false
                binding.textViewManualEntryError.isVisible = false
                binding.addressFormInput.isVisible = false
                binding.progressBar.isVisible = true
            }

            is AddressLookupState.Form -> {
                binding.recyclerViewAddressLookupOptions.isVisible = false
                binding.textViewManualEntryInitial.isVisible = false
                binding.textViewError.isVisible = false
                binding.textViewManualEntryError.isVisible = false
                binding.addressFormInput.isVisible = true
                binding.progressBar.isVisible = false
                addressLookupDelegate.updateAddressLookupInputData {
                    if (addressLookupState.selectedAddress == null) {
                        selectedAddress.resetAll()
                    } else {
                        selectedAddress.set(addressLookupState.selectedAddress)
                    }
                }
            }

            is AddressLookupState.SearchResult -> {
                binding.recyclerViewAddressLookupOptions.isVisible = true
                binding.textViewManualEntryInitial.isVisible = false
                binding.textViewError.isVisible = false
                binding.textViewManualEntryError.isVisible = false
                binding.addressFormInput.isVisible = false
                binding.progressBar.isVisible = false
                setAddressOptions(addressLookupState.options)
            }
        }
    }

    private fun clearQuery() {
        binding.editTextAddressLookupQuery.setOnChangeListener(null)
        binding.editTextAddressLookupQuery.text = null
        binding.editTextAddressLookupQuery.setOnChangeListener(::onQueryChanged)
    }

    private fun onQueryChanged(editable: Editable) {
        addressLookupDelegate.onAddressQueryChanged(editable.toString())
        binding.textInputLayoutAddressLookupQuery.hideError()
    }

    private fun setAddressOptions(options: List<LookupAddress>) {
        if (addressLookupOptionsAdapter == null) {
            initAddressOptions()
        }
        addressLookupOptionsAdapter?.submitList(options)
    }

    private fun onAddressSelected(lookupAddress: LookupAddress): Boolean {
        val isLoading = addressLookupDelegate.onAddressLookupCompleted(lookupAddress.id)
        addressLookupDelegate.addressLookupEventChannel.trySend(
            AddressLookupEvent.OptionSelected(
                lookupAddress,
                isLoading
            )
        )
        clearQuery()
        return isLoading
    }

    override fun getView(): View = this
}
