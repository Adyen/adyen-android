/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.CompoundButton
import androidx.annotation.StringRes
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.card.ui.CardNumberInput
import com.adyen.checkout.card.ui.ExpiryDateInput
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.api.ImageLoader.Companion.getInstance
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.google.android.material.textfield.TextInputLayout

/**
 * CardView for [BcmcComponent].
 */
@Suppress("TooManyFunctions")
class BcmcView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<BcmcOutputData, BcmcConfiguration, PaymentComponentState<CardPaymentMethod>, BcmcComponent>(
        context,
        attrs,
        defStyleAttr
    ),
    Observer<BcmcOutputData> {

    private lateinit var cardBrandLogoImageView: RoundCornerImageView
    private lateinit var cardNumberEditText: CardNumberInput
    private lateinit var expiryDateEditText: ExpiryDateInput
    private lateinit var expiryDateInput: TextInputLayout
    private lateinit var cardNumberInput: TextInputLayout
    private lateinit var switchStorePaymentMethod: SwitchCompat
    private lateinit var imageLoader: ImageLoader

    init {
        orientation = VERTICAL
        LayoutInflater.from(getContext()).inflate(R.layout.bcmc_view, this, true)
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        cardNumberInput.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_CardNumberInput)
        expiryDateInput.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_ExpiryDateInput)
        switchStorePaymentMethod.setLocalizedTextFromStyle(R.style.AdyenCheckout_Card_StorePaymentSwitch)
    }

    override fun initView() {
        cardBrandLogoImageView = findViewById(R.id.cardBrandLogo_imageView)
        initCardNumberInput()
        initExpiryDateInput()
        initStorePaymentMethodSwitch()
    }

    override fun onComponentAttached() {
        imageLoader = getInstance(context, component.configuration.environment)
    }

    override fun onChanged(cardOutputData: BcmcOutputData?) {
        if (cardOutputData != null) {
            onCardNumberValidated(cardOutputData.cardNumberField)
        }
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override val isConfirmationRequired = true

    override fun highlightValidationErrors() {
        val outputData: BcmcOutputData? = if (component.outputData != null) {
            component.outputData
        } else {
            return
        }
        var isErrorFocused = false
        val cardNumberValidation = outputData!!.cardNumberField.validation
        if (!cardNumberValidation.isValid()) {
            isErrorFocused = true
            cardNumberEditText.requestFocus()
            val errorReasonResId = (cardNumberValidation as Validation.Invalid).reason
            setCardNumberError(errorReasonResId)
        }
        val expiryFieldValidation = outputData.expiryDateField.validation
        if (!expiryFieldValidation.isValid()) {
            if (!isErrorFocused) {
                expiryDateInput.requestFocus()
            }
            val errorReasonResId = (expiryFieldValidation as Validation.Invalid).reason
            expiryDateInput.error = localizedContext.getString(errorReasonResId)
        }
    }

    private fun notifyInputDataChanged() {
        component.notifyInputDataChanged()
    }

    private fun onCardNumberValidated(validatedNumber: FieldState<String>) {
        if (!component.isCardNumberSupported(validatedNumber.value)) {
            cardBrandLogoImageView.strokeWidth = 0f
            cardBrandLogoImageView.setImageResource(R.drawable.ic_card)
        } else {
            cardBrandLogoImageView.strokeWidth = RoundCornerImageView.DEFAULT_STROKE_WIDTH
            imageLoader.load(BcmcComponent.SUPPORTED_CARD_TYPE.txVariant, cardBrandLogoImageView)
        }
    }

    private fun initCardNumberInput() {
        cardNumberInput = findViewById(R.id.textInputLayout_cardNumber)
        cardNumberEditText = cardNumberInput.editText as CardNumberInput
        cardNumberEditText.setOnChangeListener {
            component.inputData.cardNumber = cardNumberEditText.rawValue
            notifyInputDataChanged()
            setCardNumberError(null)
        }
        cardNumberEditText.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            val cardNumberValidation = outputData?.cardNumberField?.validation
            if (hasFocus) {
                setCardNumberError(null)
            } else if (cardNumberValidation != null && !cardNumberValidation.isValid()) {
                val errorReasonResId = (cardNumberValidation as Validation.Invalid).reason
                setCardNumberError(errorReasonResId)
            }
        }
    }

    private fun setCardNumberError(@StringRes stringResId: Int?) {
        if (stringResId == null) {
            cardNumberInput.error = null
            cardBrandLogoImageView.isVisible = true
        } else {
            cardNumberInput.error = localizedContext.getString(stringResId)
            cardBrandLogoImageView.isVisible = false
        }
    }

    private fun initExpiryDateInput() {
        expiryDateInput = findViewById(R.id.textInputLayout_expiryDate)
        expiryDateEditText = expiryDateInput.editText as ExpiryDateInput
        expiryDateEditText.setOnChangeListener {
            component.inputData.expiryDate = expiryDateEditText.date
            notifyInputDataChanged()
            expiryDateInput.error = null
        }
        expiryDateEditText.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            val outputData = component.outputData
            val expiryDateValidation = outputData?.expiryDateField?.validation
            if (hasFocus) {
                expiryDateInput.error = null
            } else if (expiryDateValidation != null && !expiryDateValidation.isValid()) {
                val errorReasonResId = (expiryDateValidation as Validation.Invalid).reason
                expiryDateInput.error = localizedContext.getString(errorReasonResId)
            }
        }
    }

    private fun initStorePaymentMethodSwitch() {
        switchStorePaymentMethod = findViewById(R.id.switch_storePaymentMethod)
        switchStorePaymentMethod.isVisible = component.configuration.isStorePaymentFieldVisible
        switchStorePaymentMethod.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            component.inputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
    }
}
