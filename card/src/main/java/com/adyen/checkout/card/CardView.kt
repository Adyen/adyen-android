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
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.ui.CardNumberInput
import com.adyen.checkout.card.ui.ExpiryDateInput
import com.adyen.checkout.card.ui.SecurityCodeInput
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.components.validation.ValidatedField
import com.google.android.material.textfield.TextInputLayout

/**
 * CardView for [CardComponent].
 */
class CardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AdyenLinearLayout<CardOutputData?, CardConfiguration?, CardComponentState?, CardComponent?>(context, attrs, defStyleAttr),
    Observer<CardOutputData?> {

    private var mCardBrandLogoImageView: RoundCornerImageView? = null
    private var mCardNumberEditText: CardNumberInput? = null
    private var mExpiryDateEditText: ExpiryDateInput? = null
    private var mExpiryDateInput: TextInputLayout? = null
    private var mSecurityCodeInput: TextInputLayout? = null
    private var mCardNumberInput: TextInputLayout? = null
    private var mStorePaymentMethodSwitch: SwitchCompat? = null
    private var mCardHolderInput: TextInputLayout? = null
    private val mCardInputData = CardInputData()
    private var mImageLoader: ImageLoader? = null

    /**
     * View for CardComponent.
     */
    init {
        orientation = VERTICAL
        LayoutInflater.from(getContext()).inflate(R.layout.card_view, this, true)
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Prevent taking screenshot and screen on recents.
        val activity = getActivity(context)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val activity = getActivity(context)
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun initView() {
        initCardNumberInput()
        initExpiryDateInput()
        initSecurityCodeInput()
        initHolderNameInput()
        mCardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView)
        mStorePaymentMethodSwitch = findViewById(R.id.switch_storePaymentMethod)
        mStorePaymentMethodSwitch?.setOnCheckedChangeListener { _, isChecked ->
            mCardInputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
        if (component.isStoredPaymentMethod()) {
            component.getStoredPaymentInputData()?.let {
                setStoredCardInterface(it)
            }

        } else {
            mCardHolderInput?.visibility = if (component.isHolderNameRequire()) VISIBLE else GONE
            mStorePaymentMethodSwitch?.visibility = if (component.showStorePaymentField()) VISIBLE else GONE
        }
        notifyInputDataChanged()
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        var myAttrs = intArrayOf(android.R.attr.hint)
        var typedArray: TypedArray

        // Card Number
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_CardNumberInput, myAttrs)
        mCardNumberInput?.hint = typedArray.getString(0)
        typedArray.recycle()

        // Expiry Date
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_ExpiryDateInput, myAttrs)
        mExpiryDateInput?.hint = typedArray.getString(0)
        typedArray.recycle()

        // Security Code
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_SecurityCodeInput, myAttrs)
        mSecurityCodeInput?.hint = typedArray.getString(0)
        typedArray.recycle()

        // Card Holder
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_HolderNameInput, myAttrs)
        mCardHolderInput?.hint = typedArray.getString(0)
        typedArray.recycle()

        // Store Switch
        myAttrs = intArrayOf(android.R.attr.text)
        typedArray = localizedContext.obtainStyledAttributes(R.style.AdyenCheckout_Card_StorePaymentSwitch, myAttrs)
        mStorePaymentMethodSwitch?.text = typedArray.getString(0)
        typedArray.recycle()
    }

    override fun onComponentAttached() {
        mImageLoader = ImageLoader.getInstance(context, component.configuration.environment)
    }

    override fun onChanged(cardOutputData: CardOutputData?) {
        if (cardOutputData != null) {
            onCardNumberValidated(cardOutputData.cardNumberField, cardOutputData.detectedCardTypes)
            onExpiryDateValidated(cardOutputData.expiryDateField)
            mSecurityCodeInput?.visibility = if (cardOutputData.isCvcHidden) GONE else VISIBLE
            if (cardOutputData.isCvcHidden) {
                // We don't expect the hidden status to change back to visible, so we don't worry about putting the margin back.
                val params = mExpiryDateInput?.layoutParams as LayoutParams
                params.marginEnd = 0
                mExpiryDateInput?.layoutParams = params
            }
        }
        if (component.isStoredPaymentMethod() && component.requiresInput()) {
            mSecurityCodeInput?.editText?.requestFocus()
        }
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun isConfirmationRequired(): Boolean {
        return true
    }

    override fun highlightValidationErrors() {
        val outputData: CardOutputData? = if (component.outputData != null) {
            component.outputData
        } else {
            return
        }
        var isErrorFocused = false
        if (outputData?.cardNumberField?.isValid == false) {
            isErrorFocused = true
            mCardNumberEditText?.requestFocus()
            setCardNumberError(R.string.checkout_card_number_not_valid)
        }
        if (outputData?.expiryDateField?.isValid == false) {
            if (!isErrorFocused) {
                isErrorFocused = true
                mExpiryDateInput?.requestFocus()
            }
            mExpiryDateInput?.error = mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid)
        }
        if (outputData?.securityCodeField?.isValid == false) {
            if (!isErrorFocused) {
                isErrorFocused = true
                mSecurityCodeInput?.requestFocus()
            }
            mSecurityCodeInput?.error = mLocalizedContext.getString(R.string.checkout_security_code_not_valid)
        }
        if (mCardHolderInput?.visibility == VISIBLE && outputData?.holderNameField?.isValid == false) {
            if (!isErrorFocused) {
                mCardHolderInput?.requestFocus()
            }
            mCardHolderInput?.error = mLocalizedContext.getString(R.string.checkout_holder_name_not_valid)
        }
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(mCardInputData)
    }

    private fun onCardNumberValidated(validatedNumber: ValidatedField<String>, detectedCardTypes: List<DetectedCardType>) {
        if (validatedNumber.validation == ValidatedField.Validation.VALID) {
            changeFocusOfInput(validatedNumber.value)
        }
        if (detectedCardTypes.isEmpty()) {
            mCardBrandLogoImageView?.setStrokeWidth(0f)
            mCardBrandLogoImageView?.setImageResource(R.drawable.ic_card)
            mCardNumberEditText?.setAmexCardFormat(false)
        } else {
            val logoImageView = mCardBrandLogoImageView
            if (logoImageView != null) {
                logoImageView.setStrokeWidth(RoundCornerImageView.DEFAULT_STROKE_WIDTH)
                mImageLoader?.load(detectedCardTypes[0].cardType.txVariant, logoImageView)
            }
            // TODO: 29/01/2021 get this logic from OutputData
            var isAmex = false
            for ((cardType) in detectedCardTypes) {
                if (cardType == CardType.AMERICAN_EXPRESS) {
                    isAmex = true
                }
            }
            mCardNumberEditText?.setAmexCardFormat(isAmex)
        }
    }

    private fun onExpiryDateValidated(validatedExpiryDate: ValidatedField<ExpiryDate>) {
        if (validatedExpiryDate.validation == ValidatedField.Validation.VALID) {
            goToNextInputIfFocus(mExpiryDateEditText)
        }
    }

    private fun changeFocusOfInput(numberValue: String) {
        val length = numberValue.length
        if (length == CardValidationUtils.GENERAL_CARD_NUMBER_LENGTH
            || length == CardValidationUtils.AMEX_CARD_NUMBER_LENGTH && CardType.estimate(numberValue).contains(CardType.AMERICAN_EXPRESS)
        ) {
            goToNextInputIfFocus(mCardNumberEditText)
        }
    }

    private fun goToNextInputIfFocus(view: View?) {
        if (rootView.findFocus() === view && view != null) {
            findViewById<View>(view.nextFocusForwardId).requestFocus()
        }
    }

    private fun initCardNumberInput() {
        mCardNumberInput = findViewById(R.id.textInputLayout_cardNumber)
        mCardNumberEditText = mCardNumberInput?.editText as CardNumberInput?
        mCardNumberEditText?.setOnChangeListener {
            mCardInputData.cardNumber = mCardNumberEditText?.rawValue.orEmpty()
            notifyInputDataChanged()
            setCardNumberError(null)
        }
        mCardNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            if (!component.isStoredPaymentMethod()) {
                val outputData = component.outputData
                if (hasFocus) {
                    setCardNumberError(null)
                } else if (outputData != null && !outputData.cardNumberField.isValid) {
                    setCardNumberError(R.string.checkout_card_number_not_valid)
                }
            }
        }
    }

    private fun setCardNumberError(@StringRes stringResId: Int?) {
        if (stringResId == null) {
            mCardNumberInput?.error = null
            mCardBrandLogoImageView?.visibility = VISIBLE
        } else {
            mCardNumberInput?.error = mLocalizedContext.getString(stringResId)
            mCardBrandLogoImageView?.visibility = GONE
        }
    }

    private fun initExpiryDateInput() {
        mExpiryDateInput = findViewById(R.id.textInputLayout_expiryDate)
        val expiryDateInput = mExpiryDateInput
        mExpiryDateEditText = mExpiryDateInput?.editText as ExpiryDateInput?
        mExpiryDateEditText?.setOnChangeListener {
            val date = mExpiryDateEditText?.date
            if (date != null) {
                mCardInputData.expiryDate = date
            }
            notifyInputDataChanged()
            mExpiryDateInput?.error = null
        }
        mExpiryDateEditText?.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                mExpiryDateInput?.error = null
            } else if (outputData != null && !outputData.expiryDateField.isValid) {
                mExpiryDateInput?.error = mLocalizedContext.getString(R.string.checkout_expiry_date_not_valid)
            }
        }
    }

    private fun initSecurityCodeInput() {
        mSecurityCodeInput = findViewById(R.id.textInputLayout_securityCode)
        val securityCodeEditText = mSecurityCodeInput?.editText as SecurityCodeInput?
        securityCodeEditText?.setOnChangeListener { editable: Editable ->
            mCardInputData.securityCode = editable.toString()
            notifyInputDataChanged()
            mSecurityCodeInput?.error = null
        }
        securityCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                mSecurityCodeInput?.error = null
            } else if (outputData != null && !outputData.securityCodeField.isValid) {
                mSecurityCodeInput?.error = mLocalizedContext.getString(R.string.checkout_security_code_not_valid)
            }
        }
    }

    private fun initHolderNameInput() {
        mCardHolderInput = findViewById(R.id.textInputLayout_cardHolder)
        val cardHolderEditText = mCardHolderInput?.editText as AdyenTextInputEditText?
        cardHolderEditText?.setOnChangeListener { editable: Editable ->
            mCardInputData.holderName = editable.toString()
            notifyInputDataChanged()
            mCardHolderInput?.error = null
        }
        cardHolderEditText?.onFocusChangeListener = OnFocusChangeListener { v: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            if (hasFocus) {
                mCardHolderInput?.error = null
            } else if (outputData != null && !outputData.holderNameField.isValid) {
                mCardHolderInput?.error = mLocalizedContext.getString(R.string.checkout_holder_name_not_valid)
            }
        }
    }

    private fun setStoredCardInterface(storedCardInput: CardInputData) {
        mCardNumberEditText?.setText(mLocalizedContext.getString(R.string.card_number_4digit, storedCardInput.cardNumber))
        mCardNumberEditText?.isEnabled = false
        mExpiryDateEditText?.setDate(storedCardInput.expiryDate)
        mExpiryDateEditText?.isEnabled = false
        mStorePaymentMethodSwitch?.visibility = GONE
        mCardHolderInput?.visibility = GONE
    }

    private fun getActivity(context: Context): Activity? {
        if (context is Activity) {
            return context
        }
        return if (context is ContextWrapper) {
            getActivity(context.baseContext)
        } else null
    }
}