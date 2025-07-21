/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 7/6/2022.
 */

package com.adyen.checkout.econtext.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.econtext.R
import com.adyen.checkout.econtext.databinding.EcontextViewBinding
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.ui.core.old.internal.ui.ComponentView
import com.adyen.checkout.ui.core.old.internal.ui.CountryAdapter
import com.adyen.checkout.ui.core.old.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.old.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.old.internal.util.setLocalizedHintFromStyle
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class EContextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: EcontextViewBinding = EcontextViewBinding.inflate(LayoutInflater.from(context), this)

    private var countryAdapter: CountryAdapter? = null

    private lateinit var localizedContext: Context

    private lateinit var delegate: EContextDelegate<*, *>

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is EContextDelegate<*, *>) { "Unsupported delegate type" }
        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        initFirstNameInput()
        initLastNameInput()
        initCountryCodeInput()
        initMobileNumberInput()
        initEmailAddressInput()
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
            val emailAddressValidation = it.emailAddressState.validation
            if (emailAddressValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    @Suppress("UNUSED_VALUE")
                    isErrorFocused = true
                    binding.editTextEmailAddress.requestFocus()
                }
                binding.textInputLayoutEmailAddress.error = localizedContext.getString(emailAddressValidation.reason)
            }
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutFirstName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_FirstNameInput,
            localizedContext,
        )
        binding.textInputLayoutLastName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_LastNameInput,
            localizedContext,
        )
        binding.textInputLayoutMobileNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_PhoneNumberInput,
            localizedContext,
        )
        binding.textInputLayoutEmailAddress.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_ShopperEmailInput,
            localizedContext,
        )
    }

    private fun initFirstNameInput() {
        binding.editTextFirstName.setOnChangeListener {
            delegate.updateInputData {
                firstName = it.toString()
            }
            binding.textInputLayoutFirstName.error = null
        }
        binding.editTextFirstName.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
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
        val countryAutoCompleteTextView = binding.autoCompleteTextViewCountry
        val countries = delegate.getSupportedCountries()
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
            delegate.getInitiallySelectedCountry()?.let {
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

    private fun initEmailAddressInput() {
        val emailAddressEditText = binding.editTextEmailAddress as? AdyenTextInputEditText
        emailAddressEditText?.setOnChangeListener {
            delegate.updateInputData {
                emailAddress = it.toString()
            }
            binding.textInputLayoutEmailAddress.error = null
        }
        emailAddressEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val emailAddressValidation = delegate.outputData.emailAddressState.validation
            if (hasFocus) {
                binding.textInputLayoutEmailAddress.error = null
            } else if (emailAddressValidation is Validation.Invalid) {
                binding.textInputLayoutEmailAddress.error = localizedContext.getString(emailAddressValidation.reason)
            }
        }
    }

    override fun getView(): View {
        return this
    }
}
