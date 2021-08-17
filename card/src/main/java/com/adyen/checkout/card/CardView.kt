/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */
package com.adyen.checkout.card

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.TypedArray
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.databinding.CardViewBinding
import com.adyen.checkout.card.ui.SecurityCodeInput
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.components.ui.view.RoundCornerImageView

/**
 * CardView for [CardComponent].
 */
@Suppress("TooManyFunctions")
class CardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenLinearLayout<CardOutputData?, CardConfiguration?, CardComponentState?, CardComponent?>(context, attrs, defStyleAttr),
    Observer<CardOutputData?> {

    private val binding: CardViewBinding = CardViewBinding.inflate(LayoutInflater.from(context), this)

    private val mCardInputData = CardInputData()
    private var mImageLoader: ImageLoader? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!BuildConfig.DEBUG) {
            // Prevent taking screenshot and screen on recents.
            getActivity(context)?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!BuildConfig.DEBUG) {
            getActivity(context)?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun initView() {
        initCardNumberInput()
        initExpiryDateInput()
        initSecurityCodeInput()
        initHolderNameInput()
        initSocialSecurityNumberInput()
        initKcpAuthenticationInput()
        initPostalCodeInput()

        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            mCardInputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
        if (component.isStoredPaymentMethod()) {
            component.getStoredPaymentInputData()?.let {
                setStoredCardInterface(it)
            }
        } else {
            binding.textInputLayoutCardHolder.isVisible = component.isHolderNameRequired()
            binding.switchStorePaymentMethod.isVisible = component.showStorePaymentField()
        }
        notifyInputDataChanged()
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        var myAttrs = intArrayOf(android.R.attr.hint)

        // Card Number
        var typedArray: TypedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_CardNumberInput, myAttrs)
        binding.textInputLayoutCardNumber.hint = typedArray.getString(0)
        typedArray.recycle()

        // Expiry Date
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_ExpiryDateInput, myAttrs)
        binding.textInputLayoutExpiryDate.hint = typedArray.getString(0)
        typedArray.recycle()

        // Security Code
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_SecurityCodeInput, myAttrs)
        binding.textInputLayoutSecurityCode.hint = typedArray.getString(0)
        typedArray.recycle()

        // Card Holder
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_HolderNameInput, myAttrs)
        binding.textInputLayoutCardHolder.hint = typedArray.getString(0)
        typedArray.recycle()

        // Postal code
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_PostalCodeInput, myAttrs)
        binding.textInputLayoutPostalCode.hint = typedArray.getString(0)
        typedArray.recycle()

        // Store Switch
        myAttrs = intArrayOf(android.R.attr.text)
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_StorePaymentSwitch, myAttrs)
        binding.switchStorePaymentMethod.text = typedArray.getString(0)
        typedArray.recycle()
    }

    override fun onComponentAttached() {
        mImageLoader = ImageLoader.getInstance(context, component.configuration.environment)
    }

    override fun onChanged(cardOutputData: CardOutputData?) {
        if (cardOutputData != null) {
            onCardNumberValidated(cardOutputData.detectedCardTypes)
            onExpiryDateValidated(cardOutputData.expiryDateState)
            setSocialSecurityNumberVisibility(cardOutputData.isSocialSecurityNumberRequired)
            setKcpAuthVisibility(cardOutputData.isKCPAuthRequired)
            setPostalCodeVisibility(cardOutputData.isPostalCodeRequired)
            handleCvcUIState(cardOutputData.cvcUIState)
            handleExpiryDateUIState(cardOutputData.expiryDateUIState)
        }
        if (component.isStoredPaymentMethod() && component.requiresInput()) {
            binding.textInputLayoutSecurityCode.editText?.requestFocus()
        }
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun isConfirmationRequired(): Boolean {
        return true
    }

    override fun highlightValidationErrors() {
        component.outputData?.let {
            var isErrorFocused = false
            val cardNumberValidation = it.cardNumberState.validation
            if (cardNumberValidation is Validation.Invalid) {
                isErrorFocused = true
                binding.editTextCardNumber.requestFocus()
                setCardNumberError(cardNumberValidation.reason)
            }
            val expiryDateValidation = it.expiryDateState.validation
            if (expiryDateValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutExpiryDate.requestFocus()
                }
                binding.textInputLayoutExpiryDate.error = mLocalizedContext.getString(expiryDateValidation.reason)
            }
            val securityCodeValidation = it.securityCodeState.validation
            if (securityCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutSecurityCode.requestFocus()
                }
                binding.textInputLayoutSecurityCode.error = mLocalizedContext.getString(securityCodeValidation.reason)
            }
            val holderNameValidation = it.holderNameState.validation
            if (binding.textInputLayoutCardHolder.isVisible && holderNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    binding.textInputLayoutCardHolder.requestFocus()
                }
                binding.textInputLayoutCardHolder.error = mLocalizedContext.getString(holderNameValidation.reason)
            }
            val postalCodeValidation = it.postalCodeState.validation
            if (binding.textInputLayoutPostalCode.isVisible && postalCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    binding.textInputLayoutPostalCode.requestFocus()
                }
                binding.textInputLayoutPostalCode.error = mLocalizedContext.getString(postalCodeValidation.reason)
            }
        }
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(mCardInputData)
    }

    private fun onCardNumberValidated(detectedCardTypes: List<DetectedCardType>) {
        if (detectedCardTypes.isEmpty()) {
            binding.cardBrandLogoImageViewPrimary.setStrokeWidth(0f)
            binding.cardBrandLogoImageViewPrimary.setImageResource(R.drawable.ic_card)
            binding.cardBrandLogoImageViewSecondary.isVisible = false
            binding.editTextCardNumber.setAmexCardFormat(false)
        } else {
            binding.cardBrandLogoImageViewPrimary.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH)
            mImageLoader?.load(detectedCardTypes[0].cardType.txVariant, binding.cardBrandLogoImageViewPrimary, 0, R.drawable.ic_card)

            detectedCardTypes.getOrNull(1)?.takeIf { it.isReliable }?.let {
                binding.cardBrandLogoImageViewSecondary.isVisible = true
                binding.cardBrandLogoImageViewSecondary.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH)
                mImageLoader?.load(it.cardType.txVariant, binding.cardBrandLogoImageViewSecondary, 0, R.drawable.ic_card)
            } ?: run { binding.cardBrandLogoImageViewSecondary.isVisible = false }
            // TODO: 29/01/2021 get this logic from OutputData
            var isAmex = false
            for ((cardType) in detectedCardTypes) {
                if (cardType == CardType.AMERICAN_EXPRESS) {
                    isAmex = true
                }
            }
            binding.editTextCardNumber.setAmexCardFormat(isAmex)
        }
    }

    private fun onExpiryDateValidated(expiryDateState: FieldState<ExpiryDate>) {
        if (expiryDateState.validation.isValid()) {
            goToNextInputIfFocus(binding.editTextExpiryDate)
        }
    }

    private fun goToNextInputIfFocus(view: View?) {
        if (rootView.findFocus() === view && view != null) {
            findViewById<View>(view.nextFocusForwardId).requestFocus()
        }
    }

    private fun initCardNumberInput() {
        binding.editTextCardNumber.setOnChangeListener {
            mCardInputData.cardNumber = binding.editTextCardNumber.rawValue
            notifyInputDataChanged()
            setCardNumberError(null)
        }
        binding.editTextCardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!component.isStoredPaymentMethod()) {
                val cardNumberValidation = component.outputData?.cardNumberState?.validation
                if (hasFocus) {
                    setCardNumberError(null)
                } else if (cardNumberValidation != null && cardNumberValidation is Validation.Invalid) {
                    setCardNumberError(cardNumberValidation.reason)
                }
            }
        }
    }

    private fun setCardNumberError(@StringRes stringResId: Int?) {
        if (stringResId == null) {
            binding.textInputLayoutCardNumber.error = null
            binding.cardBrandLogoImageViewPrimary.isVisible = true
        } else {
            binding.textInputLayoutCardNumber.error = mLocalizedContext.getString(stringResId)
            binding.cardBrandLogoImageViewPrimary.isVisible = false
        }
    }

    private fun initExpiryDateInput() {
        binding.editTextExpiryDate.setOnChangeListener {
            val date = binding.editTextExpiryDate.date
            mCardInputData.expiryDate = date
            notifyInputDataChanged()
            binding.textInputLayoutExpiryDate.error = null
        }
        binding.editTextExpiryDate.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val expiryDateValidation = component.outputData?.expiryDateState?.validation
            if (hasFocus) {
                binding.textInputLayoutExpiryDate.error = null
            } else if (expiryDateValidation != null && expiryDateValidation is Validation.Invalid) {
                binding.textInputLayoutExpiryDate.error = mLocalizedContext.getString(expiryDateValidation.reason)
            }
        }
    }

    private fun initSecurityCodeInput() {
        val securityCodeEditText = binding.textInputLayoutSecurityCode.editText as? SecurityCodeInput
        securityCodeEditText?.setOnChangeListener { editable: Editable ->
            mCardInputData.securityCode = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSecurityCode.error = null
        }
        securityCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val securityCodeValidation = component.outputData?.securityCodeState?.validation
            if (hasFocus) {
                binding.textInputLayoutSecurityCode.error = null
            } else if (securityCodeValidation != null && securityCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSecurityCode.error = mLocalizedContext.getString(securityCodeValidation.reason)
            }
        }
    }

    private fun initHolderNameInput() {
        val cardHolderEditText = binding.textInputLayoutCardHolder.editText as? AdyenTextInputEditText
        cardHolderEditText?.setOnChangeListener { editable: Editable ->
            mCardInputData.holderName = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutCardHolder.error = null
        }
        cardHolderEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val holderNameValidation = component.outputData?.holderNameState?.validation
            if (hasFocus) {
                binding.textInputLayoutCardHolder.error = null
            } else if (holderNameValidation != null && holderNameValidation is Validation.Invalid) {
                binding.textInputLayoutCardHolder.error = mLocalizedContext.getString(holderNameValidation.reason)
            }
        }
    }

    private fun initSocialSecurityNumberInput() {
        val socialSecurityNumberEditText = binding.textInputLayoutSocialSecurityNumber.editText as? AdyenTextInputEditText
        socialSecurityNumberEditText?.setOnChangeListener { editable ->
            mCardInputData.socialSecurityNumber = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSocialSecurityNumber.error = null
        }
        socialSecurityNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val socialSecurityNumberValidation = component.outputData?.socialSecurityNumberState?.validation
            if (hasFocus) {
                binding.textInputLayoutSocialSecurityNumber.error = null
            } else if (socialSecurityNumberValidation != null && socialSecurityNumberValidation is Validation.Invalid) {
                binding.textInputLayoutSocialSecurityNumber.error = mLocalizedContext.getString(socialSecurityNumberValidation.reason)
            }
        }
    }

    private fun initKcpAuthenticationInput() {
        initKcpBirthDateOrTaxNumberInput()
        initKcpCardPasswordInput()
    }

    private fun initKcpBirthDateOrTaxNumberInput() {
        val kcpBirthDateOrRegistrationNumberEditText = binding.textInputLayoutKcpBirthDateOrTaxNumber.editText as? AdyenTextInputEditText
        kcpBirthDateOrRegistrationNumberEditText?.setOnChangeListener {
            mCardInputData.kcpBirthDateOrTaxNumber = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutKcpBirthDateOrTaxNumber.error = null
            val hintResourceId = component.getKcpBirthDateOrTaxNumberHint(it.toString())
            binding.textInputLayoutKcpBirthDateOrTaxNumber.hint = mLocalizedContext.getString(hintResourceId)
        }

        kcpBirthDateOrRegistrationNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val kcpBirthDateOrTaxNumberValidation = component.outputData?.kcpBirthDateOrTaxNumberState?.validation
            if (hasFocus) {
                binding.textInputLayoutKcpBirthDateOrTaxNumber.error = null
            } else if (kcpBirthDateOrTaxNumberValidation != null && kcpBirthDateOrTaxNumberValidation is Validation.Invalid) {
                binding.textInputLayoutKcpBirthDateOrTaxNumber.error = mLocalizedContext.getString(kcpBirthDateOrTaxNumberValidation.reason)
            }
        }
    }

    private fun initKcpCardPasswordInput() {
        val kcpPasswordEditText = binding.textInputLayoutKcpCardPassword.editText as? AdyenTextInputEditText
        kcpPasswordEditText?.setOnChangeListener {
            mCardInputData.kcpCardPassword = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutKcpCardPassword.error = null
        }

        kcpPasswordEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val kcpBirthDateOrRegistrationNumberValidation = component.outputData?.kcpCardPasswordState?.validation
            if (hasFocus) {
                binding.textInputLayoutKcpCardPassword.error = null
            } else if (kcpBirthDateOrRegistrationNumberValidation != null && kcpBirthDateOrRegistrationNumberValidation is Validation.Invalid) {
                binding.textInputLayoutKcpCardPassword.error = mLocalizedContext.getString(kcpBirthDateOrRegistrationNumberValidation.reason)
            }
        }
    }

    private fun initPostalCodeInput() {
        val postalCodeEditText = binding.textInputLayoutPostalCode.editText as? AdyenTextInputEditText
        postalCodeEditText?.setOnChangeListener {
            mCardInputData.postalCode = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutPostalCode.error = null
        }

        postalCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val postalCodeValidation = component.outputData?.postalCodeState?.validation
            if (hasFocus) {
                binding.textInputLayoutPostalCode.error = null
            } else if (postalCodeValidation != null && postalCodeValidation is Validation.Invalid) {
                binding.textInputLayoutPostalCode.error = mLocalizedContext.getString(postalCodeValidation.reason)
            }
        }
    }

    private fun handleCvcUIState(cvcUIState: InputFieldUIState) {
        when (cvcUIState) {
            InputFieldUIState.REQUIRED -> {
                binding.textInputLayoutSecurityCode.isVisible = true
                binding.textInputLayoutSecurityCode.setHint(R.string.checkout_card_security_code_hint)
            }
            InputFieldUIState.OPTIONAL -> {
                binding.textInputLayoutSecurityCode.isVisible = true
                binding.textInputLayoutSecurityCode.setHint(R.string.checkout_card_security_code_optional_hint)
            }
            InputFieldUIState.HIDDEN -> {
                binding.textInputLayoutSecurityCode.isVisible = false
                // We don't expect the hidden status to change back to isVisible, so we don't worry about putting the margin back.
                val params = binding.textInputLayoutExpiryDate.layoutParams as LayoutParams
                params.marginEnd = 0
                binding.textInputLayoutExpiryDate.layoutParams = params
            }
        }
    }

    private fun handleExpiryDateUIState(expiryDateUIState: InputFieldUIState) {
        when (expiryDateUIState) {
            InputFieldUIState.REQUIRED -> {
                binding.textInputLayoutExpiryDate.isVisible = true
                binding.textInputLayoutExpiryDate.setHint(R.string.checkout_card_expiry_date_hint)
            }
            InputFieldUIState.OPTIONAL -> {
                binding.textInputLayoutExpiryDate.isVisible = true
                binding.textInputLayoutExpiryDate.setHint(R.string.checkout_card_expiry_date_optional_hint)
            }
            InputFieldUIState.HIDDEN -> {
                binding.textInputLayoutExpiryDate.isVisible = false
                val params = binding.textInputLayoutSecurityCode.layoutParams as LayoutParams
                params.marginStart = 0
                binding.textInputLayoutSecurityCode.layoutParams = params
            }
        }
    }

    private fun setSocialSecurityNumberVisibility(shouldShowSocialSecurityNumber: Boolean) {
        binding.textInputLayoutSocialSecurityNumber.isVisible = shouldShowSocialSecurityNumber
    }

    private fun setKcpAuthVisibility(shouldShowKCPAuth: Boolean) {
        binding.textInputLayoutKcpBirthDateOrTaxNumber.isVisible = shouldShowKCPAuth
        binding.textInputLayoutKcpCardPassword.isVisible = shouldShowKCPAuth
    }

    private fun setPostalCodeVisibility(shouldShowPostalCode: Boolean) {
        binding.textInputLayoutPostalCode.isVisible = shouldShowPostalCode
    }

    private fun setStoredCardInterface(storedCardInput: CardInputData) {
        binding.editTextCardNumber.setText(mLocalizedContext.getString(R.string.card_number_4digit, storedCardInput.cardNumber))
        binding.editTextCardNumber.isEnabled = false
        binding.editTextExpiryDate.setDate(storedCardInput.expiryDate)
        binding.editTextExpiryDate.isEnabled = false
        binding.switchStorePaymentMethod.isVisible = false
        binding.textInputLayoutCardHolder.isVisible = false
        binding.textInputLayoutPostalCode.isVisible = false
    }

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }
}
