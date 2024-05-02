/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 31/3/2023.
 */

package com.adyen.checkout.boleto.internal.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.boleto.R
import com.adyen.checkout.boleto.databinding.BoletoViewBinding
import com.adyen.checkout.boleto.internal.ui.BoletoDelegate
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.hideKeyboard
import com.adyen.checkout.ui.core.internal.util.isVisible
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import com.adyen.checkout.ui.core.internal.util.showKeyboard
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class BoletoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = BoletoViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var boletoDelegate: BoletoDelegate

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is BoletoDelegate) { "Unsupported delegate type" }
        boletoDelegate = delegate

        this.localizedContext = localizedContext

        initLocalizedStrings(localizedContext)
        initFirstNameInput()
        initLastNameInput()
        initSocialSecurityNumberInput()
        initAddressFormInput(coroutineScope)
        initEmail(delegate.outputData.isEmailVisible)
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewPersonalInformationHeader.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Boleto_PersonalDetailsHeader,
            localizedContext
        )
        binding.textInputLayoutFirstName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Boleto_FirstNameInput,
            localizedContext
        )
        binding.textInputLayoutLastName.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Boleto_LastNameInput,
            localizedContext
        )
        binding.textInputLayoutSocialSecurityNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Boleto_SocialNumberInput,
            localizedContext
        )
        binding.addressFormInput.initLocalizedContext(localizedContext)
        binding.switchSendEmailCopy.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Boleto_EmailCopySwitch,
            localizedContext
        )
        binding.textInputLayoutShopperEmail.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Boleto_ShopperEmailInput,
            localizedContext
        )
    }

    private fun initFirstNameInput() {
        binding.editTextFirstName.setOnChangeListener { editable: Editable ->
            boletoDelegate.updateInputData { firstName = editable.toString() }
            binding.textInputLayoutFirstName.hideError()
        }
        binding.editTextFirstName.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val firstNameValidation = boletoDelegate.outputData.firstNameState.validation
            if (hasFocus) {
                binding.textInputLayoutFirstName.hideError()
            } else if (firstNameValidation is Validation.Invalid) {
                binding.textInputLayoutFirstName.showError(localizedContext.getString(firstNameValidation.reason))
            }
        }
    }

    private fun initLastNameInput() {
        binding.editTextLastName.setOnChangeListener { editable: Editable ->
            boletoDelegate.updateInputData { lastName = editable.toString() }
            binding.textInputLayoutLastName.hideError()
        }
        binding.editTextLastName.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val lastNameValidation = boletoDelegate.outputData.lastNameState.validation
            if (hasFocus) {
                binding.textInputLayoutLastName.hideError()
            } else if (lastNameValidation is Validation.Invalid) {
                binding.textInputLayoutLastName.showError(localizedContext.getString(lastNameValidation.reason))
            }
        }
    }

    private fun initSocialSecurityNumberInput() {
        binding.editTextSocialSecurityNumber.setOnChangeListener { editable ->
            boletoDelegate.updateInputData { socialSecurityNumber = editable.toString() }
            binding.textInputLayoutSocialSecurityNumber.hideError()
        }
        binding.editTextSocialSecurityNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val socialSecurityNumberValidation = boletoDelegate.outputData.socialSecurityNumberState.validation
            if (hasFocus) {
                binding.textInputLayoutSocialSecurityNumber.hideError()
            } else if (socialSecurityNumberValidation is Validation.Invalid) {
                binding.textInputLayoutSocialSecurityNumber.showError(
                    localizedContext.getString(socialSecurityNumberValidation.reason)
                )
            }
        }
    }

    private fun initAddressFormInput(coroutineScope: CoroutineScope) {
        binding.addressFormInput.attachDelegate(boletoDelegate, coroutineScope)
    }

    private fun initEmail(isEmailVisible: Boolean) {
        binding.switchSendEmailCopy.isVisible = isEmailVisible
        if (isEmailVisible) {
            binding.switchSendEmailCopy.setOnCheckedChangeListener { _, isChecked ->
                binding.textInputLayoutShopperEmail.isVisible = isChecked
                if (isChecked) {
                    binding.editTextShopperEmail.requestFocus()
                    binding.editTextShopperEmail.showKeyboard()
                } else {
                    binding.editTextShopperEmail.clearFocus()
                    hideKeyboard()
                }
                boletoDelegate.updateInputData { isSendEmailSelected = isChecked }
            }
            initEmailInput()
        }
    }

    private fun initEmailInput() {
        binding.editTextShopperEmail.setOnChangeListener {
            boletoDelegate.updateInputData { shopperEmail = it.toString().trim() }
            binding.textInputLayoutShopperEmail.hideError()
        }
        binding.editTextShopperEmail.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val shopperEmailValidation = boletoDelegate.outputData.shopperEmailState.validation
            if (hasFocus) {
                binding.textInputLayoutShopperEmail.hideError()
            } else if (shopperEmailValidation is Validation.Invalid) {
                binding.textInputLayoutShopperEmail.showError(localizedContext.getString(shopperEmailValidation.reason))
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun highlightValidationErrors() {
        boletoDelegate.outputData.let {
            var isErrorFocused = false
            val firstNameValidation = it.firstNameState.validation
            if (binding.textInputLayoutFirstName.isVisible && firstNameValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.textInputLayoutFirstName.requestFocus()
                binding.textInputLayoutFirstName.showError(localizedContext.getString(firstNameValidation.reason))
            }
            val lastNameValidation = it.lastNameState.validation
            if (binding.textInputLayoutLastName.isVisible && lastNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutLastName.requestFocus()
                }
                binding.textInputLayoutLastName.showError(localizedContext.getString(lastNameValidation.reason))
            }
            val socialSecurityNumberValidation = it.socialSecurityNumberState.validation
            if (binding.textInputLayoutSocialSecurityNumber.isVisible &&
                socialSecurityNumberValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutSocialSecurityNumber.requestFocus()
                }
                binding.textInputLayoutSocialSecurityNumber.showError(
                    localizedContext.getString(socialSecurityNumberValidation.reason)
                )
            }
            if (binding.addressFormInput.isVisible && !it.addressState.isValid) {
                binding.addressFormInput.highlightValidationErrors(isErrorFocused)
            }
            val shopperEmailValidation = it.shopperEmailState.validation
            if (shopperEmailValidation is Validation.Invalid && binding.textInputLayoutShopperEmail.isVisible) {
                if (!isErrorFocused) {
                    @Suppress("UNUSED_VALUE")
                    isErrorFocused = true
                    binding.textInputLayoutShopperEmail.requestFocus()
                }
                binding.textInputLayoutShopperEmail.showError(
                    localizedContext.getString(shopperEmailValidation.reason)
                )
            }
        }
    }

    override fun getView(): View = this
}
