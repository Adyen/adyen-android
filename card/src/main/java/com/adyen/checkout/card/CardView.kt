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

        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            mCardInputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
        if (component.isStoredPaymentMethod()) {
            component.getStoredPaymentInputData()?.let {
                setStoredCardInterface(it)
            }
        } else {
            binding.textInputLayoutCardHolder.isVisible = component.isHolderNameRequire()
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
            onCardNumberValidated(cardOutputData.cardNumberState, cardOutputData.detectedCardTypes)
            onExpiryDateValidated(cardOutputData.expiryDateState)
            setSocialSecurityNumberVisibility(cardOutputData.socialSecurityNumberVisibility)

            when (cardOutputData.cvcUIState) {
                CvcUIState.REQUIRED -> {
                    binding.textInputLayoutSecurityCode.isVisible = true
                    binding.textInputLayoutSecurityCode.setHint(R.string.checkout_card_security_code_hint)
                }
                CvcUIState.OPTIONAL -> {
                    binding.textInputLayoutSecurityCode.isVisible = true
                    binding.textInputLayoutSecurityCode.setHint(R.string.checkout_card_security_code_optional_hint)
                }
                CvcUIState.HIDDEN -> {
                    binding.textInputLayoutSecurityCode.isVisible = false
                    // We don't expect the hidden status to change back to isVisible, so we don't worry about putting the margin back.
                    val params = binding.textInputLayoutExpiryDate.layoutParams as LayoutParams
                    params.marginEnd = 0
                    binding.textInputLayoutExpiryDate.layoutParams = params
                }
            }
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
            if (!it.cardNumberState.validation.isValid()) {
                isErrorFocused = true
                binding.editTextCardNumber.requestFocus()
                setCardNumberError(R.string.checkout_card_number_not_valid)
            }
            if (!it.expiryDateState.validation.isValid()) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutExpiryDate.requestFocus()
                }
                binding.textInputLayoutExpiryDate.error = mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid)
            }
            if (!it.securityCodeState.validation.isValid()) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutSecurityCode.requestFocus()
                }
                binding.textInputLayoutSecurityCode.error = mLocalizedContext.getString(R.string.checkout_security_code_not_valid)
            }
            if (binding.textInputLayoutCardHolder.isVisible && !it.holderNameState.validation.isValid()) {
                if (!isErrorFocused) {
                    binding.textInputLayoutCardHolder.requestFocus()
                }
                binding.textInputLayoutCardHolder.error = mLocalizedContext.getString(R.string.checkout_holder_name_not_valid)
            }
        }
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(mCardInputData)
    }

    private fun onCardNumberValidated(numberState: FieldState<String>, detectedCardTypes: List<DetectedCardType>) {
        if (numberState.validation.isValid()) {
            changeFocusOfInput(numberState.value)
        }
        if (detectedCardTypes.isEmpty()) {
            binding.cardBrandLogoImageView.setStrokeWidth(0f)
            binding.cardBrandLogoImageView.setImageResource(R.drawable.ic_card)
            binding.editTextCardNumber.setAmexCardFormat(false)
        } else {
            binding.cardBrandLogoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH)
            mImageLoader?.load(detectedCardTypes[0].cardType.txVariant, binding.cardBrandLogoImageView)
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

    private fun changeFocusOfInput(numberValue: String) {
        val length = numberValue.length
        if (length == CardValidationUtils.GENERAL_CARD_NUMBER_LENGTH ||
            length == CardValidationUtils.AMEX_CARD_NUMBER_LENGTH &&
            CardType.estimate(numberValue).contains(CardType.AMERICAN_EXPRESS)
        ) {
            goToNextInputIfFocus(binding.editTextCardNumber)
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
                val outputData = component.outputData
                if (hasFocus) {
                    setCardNumberError(null)
                } else if (outputData != null && !outputData.cardNumberState.validation.isValid()) {
                    setCardNumberError(R.string.checkout_card_number_not_valid)
                }
            }
        }
    }

    private fun setCardNumberError(@StringRes stringResId: Int?) {
        if (stringResId == null) {
            binding.textInputLayoutCardNumber.error = null
            binding.cardBrandLogoImageView.isVisible = true
        } else {
            binding.textInputLayoutCardNumber.error = mLocalizedContext.getString(stringResId)
            binding.cardBrandLogoImageView.isVisible = false
        }
    }

    private fun initExpiryDateInput() {
        binding.editTextExpiryDate.setOnChangeListener {
            val date = binding.editTextExpiryDate.date
            mCardInputData.expiryDate = date
            notifyInputDataChanged()
            binding.textInputLayoutExpiryDate.error = null
        }
        binding.editTextExpiryDate.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                binding.textInputLayoutExpiryDate.error = null
            } else if (outputData != null && !outputData.expiryDateState.validation.isValid()) {
                binding.textInputLayoutExpiryDate.error = mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid)
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
        securityCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                binding.textInputLayoutSecurityCode.error = null
            } else if (outputData != null && !outputData.securityCodeState.validation.isValid()) {
                binding.textInputLayoutSecurityCode.error = mLocalizedContext.getString(R.string.checkout_security_code_not_valid)
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
        cardHolderEditText?.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                binding.textInputLayoutCardHolder.error = null
            } else if (outputData != null && !outputData.holderNameState.validation.isValid()) {
                binding.textInputLayoutCardHolder.error = mLocalizedContext.getString(R.string.checkout_holder_name_not_valid)
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
        socialSecurityNumberEditText?.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            val outputData = component.outputData
            if (hasFocus) {
                binding.textInputLayoutSocialSecurityNumber.error = null
            } else if (outputData != null && !outputData.socialSecurityNumberState.validation.isValid()) {
                binding.textInputLayoutSocialSecurityNumber.error = mLocalizedContext.getString(R.string.checkout_card_number_not_valid)
            }
        }
    }

    private fun setSocialSecurityNumberVisibility(socialSecurityNumberVisibility: SocialSecurityNumberVisibility?) {
        binding.textInputLayoutSocialSecurityNumber.isVisible = socialSecurityNumberVisibility == SocialSecurityNumberVisibility.SHOW
    }

    private fun setStoredCardInterface(storedCardInput: CardInputData) {
        binding.editTextCardNumber.setText(mLocalizedContext.getString(R.string.card_number_4digit, storedCardInput.cardNumber))
        binding.editTextCardNumber.isEnabled = false
        binding.editTextExpiryDate.setDate(storedCardInput.expiryDate)
        binding.editTextExpiryDate.isEnabled = false
        binding.switchStorePaymentMethod.isVisible = false
        binding.textInputLayoutCardHolder.isVisible = false
    }

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }
}
