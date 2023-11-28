/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/11/2023.
 */

package com.adyen.checkout.card.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.card.R
import com.adyen.checkout.card.databinding.AddressLookupViewBinding
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope

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

        initAddressLookupQuery()
        initAddressFormInput(coroutineScope)
    }

    override fun highlightValidationErrors() {
        cardDelegate.outputData.let {
            binding.addressFormInput.highlightValidationErrors(false)
        }
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

    override fun getView(): View = this
}
