/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/6/2023.
 */

package com.adyen.checkout.atome.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.atome.R
import com.adyen.checkout.atome.databinding.AtomeViewBinding
import com.adyen.checkout.atome.internal.ui.AtomeDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.CountryUtils
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.CountryAdapter
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import kotlinx.coroutines.CoroutineScope

internal class AtomeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = AtomeViewBinding.inflate(LayoutInflater.from(context), this)

    private var countryAdapter: CountryAdapter? = null

    private lateinit var delegate: AtomeDelegate

    private lateinit var localizedContext: Context

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is AtomeDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initLocalizedStrings(localizedContext)

        initFirstNameInput()
        initLastNameInput()
        initCountryCodeInput()
        initMobileNumberInput()
        initAddressFormInput(coroutineScope)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutFirstName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Atome_FirstNameInput,
            localizedContext
        )
        binding.textInputLayoutLastName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Atome_LastNameInput,
            localizedContext
        )
        binding.textInputLayoutMobileNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Atome_PhoneNumberInput,
            localizedContext
        )
        binding.addressFormInput.initLocalizedContext(localizedContext)
    }

    override fun highlightValidationErrors() {
        delegate.outputData.let {
            var isErrorFocused = false
            val firstNameValidation = it.firstNameState.validation
            if (firstNameValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.editTextFirstName.requestFocus()
                binding.textInputLayoutFirstName.error = localizedContext.getString(firstNameValidation.reason)
            }
            val lastNameValidation = it.lastNameState.validation
            if (lastNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextLastName.requestFocus()
                }
                binding.textInputLayoutLastName.error = localizedContext.getString(lastNameValidation.reason)
            }
            val phoneNumberValidation = it.phoneNumberState.validation
            if (phoneNumberValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextMobileNumber.requestFocus()
                }
                binding.textInputLayoutMobileNumber.error = localizedContext.getString(phoneNumberValidation.reason)
            }
            if (binding.addressFormInput.isVisible && !it.billingAddressState.isValid) {
                binding.addressFormInput.highlightValidationErrors(isErrorFocused)
            }
        }
    }

    private fun initFirstNameInput() {
        val firstNameEditText = binding.editTextFirstName as? AdyenTextInputEditText
        firstNameEditText?.setOnChangeListener {
            delegate.updateInputData {
                firstName = it.toString()
            }
            binding.textInputLayoutFirstName.error = null
        }
        firstNameEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val firstNameValidation = delegate.outputData.firstNameState.validation
            if (hasFocus) {
                binding.textInputLayoutFirstName.error = null
            } else if (firstNameValidation is Validation.Invalid) {
                binding.textInputLayoutFirstName.error = localizedContext.getString(firstNameValidation.reason)
            }
        }
    }

    private fun initLastNameInput() {
        val lastNameEditText = binding.editTextLastName as? AdyenTextInputEditText
        lastNameEditText?.setOnChangeListener {
            delegate.updateInputData {
                lastName = it.toString()
            }
            binding.textInputLayoutLastName.error = null
        }
        lastNameEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val lastNameValidation = delegate.outputData.lastNameState.validation
            if (hasFocus) {
                binding.textInputLayoutLastName.error = null
            } else if (lastNameValidation is Validation.Invalid) {
                binding.textInputLayoutLastName.error = localizedContext.getString(lastNameValidation.reason)
            }
        }
    }

    private fun initCountryCodeInput() {
        val countryAutoCompleteTextView = binding.autoCompleteTextViewCountryCode
        val countries = CountryUtils.getCountries().map {
            CountryModel(
                isoCode = it.isoCode,
                countryName = CountryUtils.getCountryName(it.isoCode, delegate.componentParams.shopperLocale),
                callingCode = it.callingCode,
                emoji = it.emoji
            )
        }
        countryAdapter = CountryAdapter(context, localizedContext).apply {
            setItems(countries)
        }
        countryAutoCompleteTextView.apply {
            inputType = 0
            setAdapter(countryAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val country = countryAdapter?.getItem(position) ?: return@OnItemClickListener
                onCountrySelected(country)
            }
            countries.firstOrNull()?.let {
                setText(it.toShortString())
                onCountrySelected(it)
            }
        }
    }

    private fun onCountrySelected(country: CountryModel) {
        delegate.updateInputData {
            countryCode = country.callingCode
        }
    }

    private fun initMobileNumberInput() {
        val mobileNumberEditText = binding.editTextMobileNumber as? AdyenTextInputEditText
        mobileNumberEditText?.setOnChangeListener {
            delegate.updateInputData {
                mobileNumber = it.toString()
            }
            binding.textInputLayoutMobileNumber.error = null
        }
        mobileNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val phoneNumberValidation = delegate.outputData.phoneNumberState.validation
            if (hasFocus) {
                binding.textInputLayoutMobileNumber.error = null
            } else if (phoneNumberValidation is Validation.Invalid) {
                binding.textInputLayoutMobileNumber.error = localizedContext.getString(phoneNumberValidation.reason)
            }
        }
    }

    private fun initAddressFormInput(coroutineScope: CoroutineScope) {
        binding.addressFormInput.attachDelegate(delegate, coroutineScope)
    }

    override fun getView(): View = this
}
