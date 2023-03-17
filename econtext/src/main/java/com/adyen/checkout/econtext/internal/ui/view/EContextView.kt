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
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.CountryUtils
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.econtext.R
import com.adyen.checkout.econtext.databinding.EcontextViewBinding
import com.adyen.checkout.econtext.internal.ui.EContextDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import kotlinx.coroutines.CoroutineScope

@Suppress("TooManyFunctions")
internal class EContextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding: EcontextViewBinding = EcontextViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: EContextDelegate<*, *>

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is EContextDelegate<*, *>) throw IllegalArgumentException("Unsupported delegate type")
        this.delegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        initFirstNameInput()
        initLastNameInput()
        initPhoneNumberInput()
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
            val emailAddressValidation = it.emailAddressState.validation
            if (emailAddressValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.editTextEmailAddress.requestFocus()
                }
                binding.textInputLayoutEmailAddress.error = localizedContext.getString(emailAddressValidation.reason)
            }
        }
        binding.phoneNumberInput.highlightValidationErrors(delegate.phoneNumberOutputData)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutFirstName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_FirstNameInput,
            localizedContext
        )
        binding.textInputLayoutLastName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_LastNameInput,
            localizedContext
        )
        binding.textInputLayoutEmailAddress.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_EContext_ShopperEmailInput,
            localizedContext
        )
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

    private fun initPhoneNumberInput() {
        binding.phoneNumberInput.initialize(delegate, localizedContext)
        val countries = CountryUtils.getCountries().map {
            CountryModel(
                isoCode = it.isoCode,
                countryName = CountryUtils.getCountryName(it.isoCode, delegate.componentParams.shopperLocale),
                callingCode = it.callingCode,
                emoji = it.emoji
            )
        }
        binding.phoneNumberInput.setCountries(countries)
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

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
