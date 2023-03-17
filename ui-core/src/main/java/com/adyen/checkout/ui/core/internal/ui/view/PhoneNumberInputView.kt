/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/3/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.databinding.ViewPhoneNumberInputBinding
import com.adyen.checkout.ui.core.internal.ui.CountryAdapter
import com.adyen.checkout.ui.core.internal.ui.PhoneNumberDelegate
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.ui.model.PhoneNumberOutputData
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.showError

class PhoneNumberInputView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(
    context,
    attrs,
    defStyleAttr
) {

    private val binding = ViewPhoneNumberInputBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var delegate: PhoneNumberDelegate

    private lateinit var localizedContext: Context

    private lateinit var adapter: CountryAdapter

    fun initialize(
        delegate: PhoneNumberDelegate,
        localizedContext: Context,
    ) {
        this.delegate = delegate
        this.localizedContext = localizedContext

        initCountryInput()
        initPhoneNumberInput()
    }

    private fun initCountryInput() {
        adapter = CountryAdapter(context, localizedContext)
        binding.autoCompleteTextViewCountry.apply {
            // disable editing and hide cursor
            inputType = 0
            setAdapter(this@PhoneNumberInputView.adapter)
            setOnItemClickListener { _, _, position, _ ->
                val country = this@PhoneNumberInputView.adapter.getItem(position)
                delegate.updatePhoneNumberInputData {
                    countryCode = country.callingCode
                }
            }
        }
    }

    private fun initPhoneNumberInput() {
        binding.editTextPhoneNumber.setOnChangeListener {
            delegate.updatePhoneNumberInputData {
                everythingAfterCountryCode = it.toString()
            }
            binding.textInputLayoutPhoneNumber.hideError()
        }

        binding.editTextPhoneNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            val outputData = delegate.phoneNumberOutputData
            val phoneNumberValidation = outputData.phoneNumber.validation
            if (hasFocus) {
                binding.textInputLayoutPhoneNumber.hideError()
            } else if (phoneNumberValidation is Validation.Invalid) {
                binding.textInputLayoutPhoneNumber.showError(
                    localizedContext.getString(phoneNumberValidation.reason)
                )
            }
        }
    }

    fun highlightValidationErrors(outputData: PhoneNumberOutputData) {
        val phoneNumberValidation = outputData.phoneNumber.validation
        if (phoneNumberValidation is Validation.Invalid) {
            binding.textInputLayoutPhoneNumber.showError(
                localizedContext.getString(phoneNumberValidation.reason)
            )
        }
    }

    fun setCountries(countries: List<CountryModel>) {
        adapter.setItems(countries)
        val firstCountry = countries.firstOrNull()
        if (firstCountry != null) {
            binding.autoCompleteTextViewCountry.setText(firstCountry.toShortString())
            delegate.updatePhoneNumberInputData {
                countryCode = firstCountry.callingCode
            }
        }
    }
}
