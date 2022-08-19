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
import android.text.Editable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.WindowManager
import android.widget.AdapterView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.databinding.CardViewBinding
import com.adyen.checkout.card.ui.SecurityCodeInput
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.card.util.InstallmentUtils
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.extensions.isVisible
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.util.BuildUtils

/**
 * CardView for [CardComponent].
 */
@Suppress("TooManyFunctions")
class CardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    AdyenLinearLayout<CardOutputData, CardConfiguration, CardComponentState, CardComponent>(
        context,
        attrs,
        defStyleAttr
    ),
    Observer<CardOutputData> {

    companion object {
        private const val UNSELECTED_BRAND_LOGO_ALPHA = 0.2f
        private const val SELECTED_BRAND_LOGO_ALPHA = 1f
        private const val PRIMARY_BRAND_INDEX = 0
        private const val SECONDARY_BRAND_INDEX = 1
    }

    private val binding: CardViewBinding = CardViewBinding.inflate(LayoutInflater.from(context), this)

    private var imageLoader: ImageLoader? = null
    private var installmentListAdapter: InstallmentListAdapter? = null
    private var cardListAdapter: CardListAdapter? = null

    init {
        orientation = VERTICAL
        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!BuildUtils.isDebugBuild(context)) {
            // Prevent taking screenshot and screen on recents.
            getActivity(context)?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (!BuildUtils.isDebugBuild(context)) {
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
        initAddressFormInput()

        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            component.inputData.isStorePaymentSelected = isChecked
            notifyInputDataChanged()
        }
        notifyInputDataChanged()
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutCardNumber.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_CardNumberInput)
        binding.textInputLayoutExpiryDate.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_ExpiryDateInput)
        binding.textInputLayoutSecurityCode.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_SecurityCodeInput)
        binding.textInputLayoutCardHolder.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_HolderNameInput)
        binding.textInputLayoutPostalCode.setLocalizedHintFromStyle(R.style.AdyenCheckout_Card_PostalCodeInput)
        binding.textInputLayoutSocialSecurityNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_SocialSecurityNumberInput
        )
        binding.textInputLayoutKcpBirthDateOrTaxNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_KcpBirthDateOrTaxNumber
        )
        binding.textInputLayoutKcpCardPassword.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_KcpCardPassword
        )
        binding.textInputLayoutInstallments.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_DropdownTextInputLayout_Installments
        )
        binding.switchStorePaymentMethod.setLocalizedTextFromStyle(R.style.AdyenCheckout_Card_StorePaymentSwitch)
        binding.addressFormInput.initLocalizedContext(localizedContext)
    }

    override fun onComponentAttached() {
        imageLoader = ImageLoader.getInstance(context, component.configuration.environment)
    }

    override fun onChanged(cardOutputData: CardOutputData?) {
        cardOutputData ?: return

        if (isStoredPaymentMethod(cardOutputData)) setStoredCardInterface(cardOutputData)

        onCardNumberValidated(cardOutputData)
        onExpiryDateValidated(cardOutputData.expiryDateState)
        setSocialSecurityNumberVisibility(cardOutputData.isSocialSecurityNumberRequired)
        setKcpAuthVisibility(cardOutputData.isKCPAuthRequired)
        setKcpHint(cardOutputData.kcpBirthDateOrTaxNumberHint)
        setAddressInputVisibility(cardOutputData.addressUIState)
        handleCvcUIState(cardOutputData.cvcUIState)
        handleExpiryDateUIState(cardOutputData.expiryDateUIState)
        handleHolderNameUIState(cardOutputData.holderNameUIState)
        setStorePaymentSwitchVisibility(cardOutputData.showStorePaymentField)
        updateInstallments(cardOutputData)
        updateCountries(cardOutputData.countryOptions)
        updateStates(cardOutputData.stateOptions)
        setSupportedCardsList(cardOutputData.supportedCardTypes)
        setFilteredCards(cardOutputData.detectedCardTypes.map { it.cardType })
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override val isConfirmationRequired: Boolean
        get() = true

    @Suppress("ComplexMethod", "LongMethod")
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
                binding.textInputLayoutExpiryDate.error = localizedContext.getString(expiryDateValidation.reason)
            }
            val securityCodeValidation = it.securityCodeState.validation
            if (securityCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutSecurityCode.requestFocus()
                }
                binding.textInputLayoutSecurityCode.error = localizedContext.getString(securityCodeValidation.reason)
            }
            val holderNameValidation = it.holderNameState.validation
            if (binding.textInputLayoutCardHolder.isVisible && holderNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutCardHolder.requestFocus()
                }
                binding.textInputLayoutCardHolder.error = localizedContext.getString(holderNameValidation.reason)
            }
            val postalCodeValidation = it.addressState.postalCode.validation
            if (binding.textInputLayoutPostalCode.isVisible && postalCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutPostalCode.requestFocus()
                }
                binding.textInputLayoutPostalCode.error = localizedContext.getString(postalCodeValidation.reason)
            }
            val socialSecurityNumberValidation = it.socialSecurityNumberState.validation
            if (binding.textInputLayoutSocialSecurityNumber.isVisible &&
                socialSecurityNumberValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutSocialSecurityNumber.requestFocus()
                }
                binding.textInputLayoutSocialSecurityNumber.error =
                    localizedContext.getString(socialSecurityNumberValidation.reason)
            }
            val kcpBirthDateOrTaxNumberValidation = it.kcpBirthDateOrTaxNumberState.validation
            if (binding.textInputLayoutKcpBirthDateOrTaxNumber.isVisible &&
                kcpBirthDateOrTaxNumberValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutKcpBirthDateOrTaxNumber.requestFocus()
                }
                binding.textInputLayoutKcpBirthDateOrTaxNumber.error =
                    localizedContext.getString(kcpBirthDateOrTaxNumberValidation.reason)
            }
            val kcpPasswordValidation = it.kcpCardPasswordState.validation
            if (binding.textInputLayoutKcpCardPassword.isVisible && kcpPasswordValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutKcpCardPassword.requestFocus()
                }
                binding.textInputLayoutKcpCardPassword.error = localizedContext.getString(kcpPasswordValidation.reason)
            }
            if (binding.addressFormInput.isVisible && !it.addressState.isValid) {
                binding.addressFormInput.highlightValidationErrors(isErrorFocused)
            }
        }
    }

    private fun notifyInputDataChanged() {
        component.notifyInputDataChanged()
    }

    private fun onCardNumberValidated(cardOutputData: CardOutputData) {
        val detectedCardTypes = cardOutputData.detectedCardTypes
        if (detectedCardTypes.isEmpty()) {
            binding.cardBrandLogoImageViewPrimary.apply {
                strokeWidth = 0f
                setImageResource(R.drawable.ic_card)
                alpha = 1f
            }
            binding.cardBrandLogoContainerSecondary.isVisible = false
            binding.editTextCardNumber.setAmexCardFormat(false)
            resetBrandSelectionInput()
        } else {
            binding.cardBrandLogoImageViewPrimary.strokeWidth = RoundCornerImageView.DEFAULT_STROKE_WIDTH
            imageLoader?.load(
                detectedCardTypes[0].cardType.txVariant,
                binding.cardBrandLogoImageViewPrimary,
                0,
                R.drawable.ic_card
            )
            setDualBrandedCardImages(detectedCardTypes, cardOutputData.cardNumberState.validation)

            // TODO: 29/01/2021 get this logic from OutputData
            val isAmex = detectedCardTypes.any { it.cardType == CardType.AMERICAN_EXPRESS }
            binding.editTextCardNumber.setAmexCardFormat(isAmex)
        }
    }

    private fun setDualBrandedCardImages(detectedCardTypes: List<DetectedCardType>, validation: Validation) {
        val cardNumberHasFocus = binding.textInputLayoutCardNumber.hasFocus()
        if (validation is Validation.Invalid && !cardNumberHasFocus) {
            setCardNumberError(validation.reason)
        } else {
            detectedCardTypes.getOrNull(1)?.takeIf { it.isReliable }?.let { detectedCardType ->
                binding.cardBrandLogoContainerSecondary.isVisible = true
                binding.cardBrandLogoImageViewSecondary.strokeWidth = RoundCornerImageView.DEFAULT_STROKE_WIDTH
                imageLoader?.load(
                    detectedCardType.cardType.txVariant,
                    binding.cardBrandLogoImageViewSecondary,
                    0,
                    R.drawable.ic_card
                )
                initCardBrandLogoViews(detectedCardTypes.indexOfFirst { it.isSelected })
                initBrandSelectionListeners()
            } ?: run {
                binding.cardBrandLogoImageViewPrimary.alpha = 1f
                binding.cardBrandLogoContainerSecondary.isVisible = false
                resetBrandSelectionInput()
            }
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
            component.inputData.cardNumber = binding.editTextCardNumber.rawValue
            notifyInputDataChanged()
            setCardErrorState(true)
        }
        binding.editTextCardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            setCardErrorState(hasFocus)
        }
    }

    private fun setCardErrorState(hasFocus: Boolean) {
        val outputData = component.outputData ?: return
        if (isStoredPaymentMethod(outputData)) return

        val cardNumberValidation = outputData.cardNumberState.validation
        val showErrorWhileEditing = (cardNumberValidation as? Validation.Invalid)?.showErrorWhileEditing ?: false
        val shouldNotShowError = hasFocus && !showErrorWhileEditing
        if (shouldNotShowError) {
            val shouldShowSecondaryLogo = outputData.isDualBranded
            setCardNumberError(null, shouldShowSecondaryLogo)
        } else if (cardNumberValidation is Validation.Invalid) {
            setCardNumberError(cardNumberValidation.reason)
        }
    }

    private fun setCardNumberError(@StringRes stringResId: Int?, shouldShowSecondaryLogo: Boolean = false) {
        if (stringResId == null) {
            binding.textInputLayoutCardNumber.isErrorEnabled = false
            binding.textInputLayoutCardNumber.error = null
            binding.cardBrandLogoContainerPrimary.isVisible = true
            binding.cardBrandLogoContainerSecondary.isVisible = shouldShowSecondaryLogo
        } else {
            binding.textInputLayoutCardNumber.isErrorEnabled = true
            binding.textInputLayoutCardNumber.error = localizedContext.getString(stringResId)
            binding.cardBrandLogoContainerPrimary.isVisible = false
            binding.cardBrandLogoContainerSecondary.isVisible = false
        }
    }

    private fun initCardBrandLogoViews(selectedIndex: Int) {
        when (selectedIndex) {
            PRIMARY_BRAND_INDEX -> selectPrimaryBrand()
            SECONDARY_BRAND_INDEX -> selectSecondaryBrand()
            else -> throw CheckoutException("Illegal brand index selected. Selected index must be either 0 or 1.")
        }
    }

    private fun initBrandSelectionListeners() {
        binding.cardBrandLogoContainerPrimary.setOnClickListener {
            component.inputData.selectedCardIndex = PRIMARY_BRAND_INDEX
            notifyInputDataChanged()
            selectPrimaryBrand()
        }

        binding.cardBrandLogoContainerSecondary.setOnClickListener {
            component.inputData.selectedCardIndex = SECONDARY_BRAND_INDEX
            notifyInputDataChanged()
            selectSecondaryBrand()
        }
    }

    private fun resetBrandSelectionInput() {
        binding.cardBrandLogoContainerPrimary.setOnClickListener(null)
        binding.cardBrandLogoContainerSecondary.setOnClickListener(null)
    }

    private fun selectPrimaryBrand() {
        binding.cardBrandLogoImageViewPrimary.alpha = SELECTED_BRAND_LOGO_ALPHA
        binding.cardBrandLogoImageViewSecondary.alpha = UNSELECTED_BRAND_LOGO_ALPHA
    }

    private fun selectSecondaryBrand() {
        binding.cardBrandLogoImageViewPrimary.alpha = UNSELECTED_BRAND_LOGO_ALPHA
        binding.cardBrandLogoImageViewSecondary.alpha = SELECTED_BRAND_LOGO_ALPHA
    }

    private fun initExpiryDateInput() {
        binding.editTextExpiryDate.setOnChangeListener {
            val date = binding.editTextExpiryDate.date
            component.inputData.expiryDate = date
            notifyInputDataChanged()
            binding.textInputLayoutExpiryDate.error = null
        }
        binding.editTextExpiryDate.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val expiryDateValidation = component.outputData?.expiryDateState?.validation
            if (hasFocus) {
                binding.textInputLayoutExpiryDate.error = null
            } else if (expiryDateValidation != null && expiryDateValidation is Validation.Invalid) {
                binding.textInputLayoutExpiryDate.error = localizedContext.getString(expiryDateValidation.reason)
            }
        }
    }

    private fun initSecurityCodeInput() {
        val securityCodeEditText = binding.textInputLayoutSecurityCode.editText as? SecurityCodeInput
        securityCodeEditText?.setOnChangeListener { editable: Editable ->
            component.inputData.securityCode = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSecurityCode.error = null
        }
        securityCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val securityCodeValidation = component.outputData?.securityCodeState?.validation
            if (hasFocus) {
                binding.textInputLayoutSecurityCode.error = null
            } else if (securityCodeValidation != null && securityCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSecurityCode.error = localizedContext.getString(securityCodeValidation.reason)
            }
        }
    }

    private fun initHolderNameInput() {
        val cardHolderEditText = binding.textInputLayoutCardHolder.editText as? AdyenTextInputEditText
        cardHolderEditText?.setOnChangeListener { editable: Editable ->
            component.inputData.holderName = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutCardHolder.error = null
        }
        cardHolderEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val holderNameValidation = component.outputData?.holderNameState?.validation
            if (hasFocus) {
                binding.textInputLayoutCardHolder.error = null
            } else if (holderNameValidation != null && holderNameValidation is Validation.Invalid) {
                binding.textInputLayoutCardHolder.error = localizedContext.getString(holderNameValidation.reason)
            }
        }
    }

    private fun initSocialSecurityNumberInput() {
        val socialSecurityNumberEditText =
            binding.textInputLayoutSocialSecurityNumber.editText as? AdyenTextInputEditText
        socialSecurityNumberEditText?.setOnChangeListener { editable ->
            component.inputData.socialSecurityNumber = editable.toString()
            notifyInputDataChanged()
            binding.textInputLayoutSocialSecurityNumber.error = null
        }
        socialSecurityNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val socialSecurityNumberValidation = component.outputData?.socialSecurityNumberState?.validation
            if (hasFocus) {
                binding.textInputLayoutSocialSecurityNumber.error = null
            } else if (socialSecurityNumberValidation != null && socialSecurityNumberValidation is Validation.Invalid) {
                binding.textInputLayoutSocialSecurityNumber.error =
                    localizedContext.getString(socialSecurityNumberValidation.reason)
            }
        }
    }

    private fun initKcpAuthenticationInput() {
        initKcpBirthDateOrTaxNumberInput()
        initKcpCardPasswordInput()
    }

    private fun initKcpBirthDateOrTaxNumberInput() {
        val kcpBirthDateOrRegistrationNumberEditText =
            binding.textInputLayoutKcpBirthDateOrTaxNumber.editText as? AdyenTextInputEditText
        kcpBirthDateOrRegistrationNumberEditText?.setOnChangeListener {
            component.inputData.kcpBirthDateOrTaxNumber = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutKcpBirthDateOrTaxNumber.error = null
        }

        kcpBirthDateOrRegistrationNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val kcpBirthDateOrTaxNumberValidation = component.outputData?.kcpBirthDateOrTaxNumberState?.validation
            if (hasFocus) {
                binding.textInputLayoutKcpBirthDateOrTaxNumber.error = null
            } else if (
                kcpBirthDateOrTaxNumberValidation != null &&
                kcpBirthDateOrTaxNumberValidation is Validation.Invalid
            ) {
                binding.textInputLayoutKcpBirthDateOrTaxNumber.error =
                    localizedContext.getString(kcpBirthDateOrTaxNumberValidation.reason)
            }
        }
    }

    private fun initKcpCardPasswordInput() {
        val kcpPasswordEditText = binding.textInputLayoutKcpCardPassword.editText as? AdyenTextInputEditText
        kcpPasswordEditText?.setOnChangeListener {
            component.inputData.kcpCardPassword = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutKcpCardPassword.error = null
        }

        kcpPasswordEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val kcpBirthDateOrRegistrationNumberValidation = component.outputData?.kcpCardPasswordState?.validation
            if (hasFocus) {
                binding.textInputLayoutKcpCardPassword.error = null
            } else if (kcpBirthDateOrRegistrationNumberValidation != null &&
                kcpBirthDateOrRegistrationNumberValidation is Validation.Invalid
            ) {
                binding.textInputLayoutKcpCardPassword.error =
                    localizedContext.getString(kcpBirthDateOrRegistrationNumberValidation.reason)
            }
        }
    }

    private fun initPostalCodeInput() {
        val postalCodeEditText = binding.textInputLayoutPostalCode.editText as? AdyenTextInputEditText
        postalCodeEditText?.setOnChangeListener {
            component.inputData.address.postalCode = it.toString()
            notifyInputDataChanged()
            binding.textInputLayoutPostalCode.error = null
        }

        postalCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val postalCodeValidation = component.outputData?.addressState?.postalCode?.validation
            if (hasFocus) {
                binding.textInputLayoutPostalCode.error = null
            } else if (postalCodeValidation != null && postalCodeValidation is Validation.Invalid) {
                binding.textInputLayoutPostalCode.error = localizedContext.getString(postalCodeValidation.reason)
            }
        }
    }

    private fun initAddressFormInput() {
        binding.addressFormInput.attachComponent(component)
    }

    private fun updateCountries(countryOptions: List<AddressListItem>) {
        binding.addressFormInput.updateCountries(countryOptions)
    }

    private fun updateStates(stateOptions: List<AddressListItem>) {
        binding.addressFormInput.updateStates(stateOptions)
    }

    private fun updateInstallments(cardOutputData: CardOutputData) {
        val installmentTextInputLayout = binding.textInputLayoutInstallments
        val installmentAutoCompleteTextView = binding.autoCompleteTextViewInstallments
        if (cardOutputData.installmentOptions.isNotEmpty()) {
            if (installmentListAdapter == null) {
                initInstallments()
            }
            if (cardOutputData.installmentState.value == null) {
                updateInstallmentSelection(cardOutputData.installmentOptions.first())
                val installmentOptionText = InstallmentUtils.getTextForInstallmentOption(
                    localizedContext,
                    cardOutputData.installmentOptions.first()
                )
                installmentAutoCompleteTextView.setText(installmentOptionText)
            }
            installmentListAdapter?.setItems(cardOutputData.installmentOptions)
            installmentTextInputLayout.isVisible = true
        } else {
            installmentTextInputLayout.isVisible = false
        }
    }

    private fun initInstallments() {
        installmentListAdapter = InstallmentListAdapter(context, localizedContext)
        installmentListAdapter?.let {
            binding.autoCompleteTextViewInstallments.apply {
                inputType = 0
                setAdapter(it)
                onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                    updateInstallmentSelection(installmentListAdapter?.getItem(position))
                }
            }
        }
    }

    private fun handleCvcUIState(cvcUIState: InputFieldUIState) {
        when (cvcUIState) {
            InputFieldUIState.REQUIRED -> {
                binding.textInputLayoutSecurityCode.isVisible = true
                binding.textInputLayoutSecurityCode.setLocalizedHintFromStyle(
                    R.style.AdyenCheckout_Card_SecurityCodeInput
                )
            }
            InputFieldUIState.OPTIONAL -> {
                binding.textInputLayoutSecurityCode.isVisible = true
                binding.textInputLayoutSecurityCode.hint = localizedContext.getString(
                    R.string.checkout_card_security_code_optional_hint
                )
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
                binding.textInputLayoutExpiryDate.setLocalizedHintFromStyle(
                    R.style.AdyenCheckout_Card_ExpiryDateInput
                )
            }
            InputFieldUIState.OPTIONAL -> {
                binding.textInputLayoutExpiryDate.isVisible = true
                binding.textInputLayoutExpiryDate.hint = localizedContext.getString(
                    R.string.checkout_card_expiry_date_optional_hint
                )
            }
            InputFieldUIState.HIDDEN -> {
                binding.textInputLayoutExpiryDate.isVisible = false
                val params = binding.textInputLayoutSecurityCode.layoutParams as LayoutParams
                params.marginStart = 0
                binding.textInputLayoutSecurityCode.layoutParams = params
            }
        }
    }

    private fun handleHolderNameUIState(holderNameUIState: InputFieldUIState) {
        binding.textInputLayoutCardHolder.isVisible = holderNameUIState != InputFieldUIState.HIDDEN
    }

    private fun setStorePaymentSwitchVisibility(showStorePaymentField: Boolean) {
        binding.switchStorePaymentMethod.isVisible = showStorePaymentField
    }

    private fun setSocialSecurityNumberVisibility(shouldShowSocialSecurityNumber: Boolean) {
        binding.textInputLayoutSocialSecurityNumber.isVisible = shouldShowSocialSecurityNumber
    }

    private fun setKcpAuthVisibility(shouldShowKCPAuth: Boolean) {
        binding.textInputLayoutKcpBirthDateOrTaxNumber.isVisible = shouldShowKCPAuth
        binding.textInputLayoutKcpCardPassword.isVisible = shouldShowKCPAuth
    }

    private fun setKcpHint(kcpBirthDateOrTaxNumberHint: Int?) {
        kcpBirthDateOrTaxNumberHint ?: return
        binding.textInputLayoutKcpBirthDateOrTaxNumber.hint = localizedContext.getString(kcpBirthDateOrTaxNumberHint)
    }

    private fun setAddressInputVisibility(addressFormUIState: AddressFormUIState) {
        when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> {
                binding.addressFormInput.isVisible = true
                binding.textInputLayoutPostalCode.isVisible = false
            }
            AddressFormUIState.POSTAL_CODE -> {
                binding.addressFormInput.isVisible = false
                binding.textInputLayoutPostalCode.isVisible = true
            }
            AddressFormUIState.NONE -> {
                binding.addressFormInput.isVisible = false
                binding.textInputLayoutPostalCode.isVisible = false
            }
        }
    }

    private fun setStoredCardInterface(outputData: CardOutputData) {
        binding.editTextCardNumber.isEnabled = false
        binding.editTextCardNumber.setText(
            localizedContext.getString(
                R.string.card_number_4digit,
                outputData.cardNumberState.value
            )
        )
        binding.editTextExpiryDate.isEnabled = false
        binding.editTextExpiryDate.date = outputData.expiryDateState.value
        binding.switchStorePaymentMethod.isVisible = false
        binding.textInputLayoutCardHolder.isVisible = false
        binding.textInputLayoutPostalCode.isVisible = false
        binding.addressFormInput.isVisible = false

        if (component.requiresInput()) {
            binding.textInputLayoutSecurityCode.editText?.requestFocus()
        }
    }

    private fun updateInstallmentSelection(installmentModel: InstallmentModel?) {
        installmentModel?.let {
            component.inputData.installmentOption = it
            notifyInputDataChanged()
        }
    }

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

    private fun setSupportedCardsList(cards: List<CardType>) {
        binding.recyclerViewCardList.isVisible = cards.isNotEmpty()
        if (cardListAdapter == null) {
            cardListAdapter = CardListAdapter(requireNotNull(imageLoader))
            binding.recyclerViewCardList.adapter = cardListAdapter
        }

        cardListAdapter?.submitList(cards)
    }

    private fun setFilteredCards(cards: List<CardType>) {
        cardListAdapter?.filteredCards = cards
    }

    private fun isStoredPaymentMethod(outputData: CardOutputData): Boolean {
        return outputData.componentMode == ComponentMode.STORED
    }
}
