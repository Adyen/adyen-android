/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2022.
 */
package com.adyen.checkout.card.internal.ui.view

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
import android.widget.LinearLayout
import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.adyen.checkout.card.CardBrand
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardType
import com.adyen.checkout.card.R
import com.adyen.checkout.card.databinding.CardViewBinding
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.CardDelegate
import com.adyen.checkout.card.internal.ui.model.CardListItem
import com.adyen.checkout.card.internal.ui.model.CardOutputData
import com.adyen.checkout.card.internal.ui.model.ExpiryDate
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.util.InstallmentUtils
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.BuildUtils
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.ui.view.RoundCornerImageView
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.isVisible
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * CardView for [CardComponent].
 */
@Suppress("TooManyFunctions", "LargeClass")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) :
    LinearLayout(
        context,
        attrs,
        defStyleAttr
    ),
    ComponentView {

    private val binding: CardViewBinding = CardViewBinding.inflate(LayoutInflater.from(context), this)

    private var installmentListAdapter: InstallmentListAdapter? = null
    private var cardListAdapter: CardListAdapter? = null

    private lateinit var localizedContext: Context

    private lateinit var cardDelegate: CardDelegate

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

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is CardDelegate) { "Unsupported delegate type" }
        cardDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        observeDelegate(delegate, coroutineScope)

        initCardNumberInput()
        initExpiryDateInput()
        initSecurityCodeInput()
        initHolderNameInput()
        initSocialSecurityNumberInput()
        initKcpAuthenticationInput()
        initPostalCodeInput()
        initAddressFormInput(coroutineScope)
        initAddressLookup()

        binding.switchStorePaymentMethod.setOnCheckedChangeListener { _, isChecked ->
            delegate.updateInputData { isStorePaymentMethodSwitchChecked = isChecked }
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutCardNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_CardNumberInput,
            localizedContext
        )
        binding.textInputLayoutExpiryDate.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_ExpiryDateInput,
            localizedContext
        )
        binding.textInputLayoutSecurityCode.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_SecurityCodeInput,
            localizedContext
        )
        binding.textInputLayoutCardHolder.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_HolderNameInput,
            localizedContext
        )
        binding.textInputLayoutPostalCode.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_PostalCodeInput,
            localizedContext
        )
        binding.textInputLayoutSocialSecurityNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_SocialSecurityNumberInput,
            localizedContext
        )
        binding.textInputLayoutKcpBirthDateOrTaxNumber.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_KcpBirthDateOrTaxNumber,
            localizedContext
        )
        binding.textInputLayoutKcpCardPassword.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_Card_KcpCardPassword,
            localizedContext
        )
        binding.textInputLayoutInstallments.setLocalizedHintFromStyle(
            R.style.AdyenCheckout_DropdownTextInputLayout_Installments,
            localizedContext
        )
        binding.switchStorePaymentMethod.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_Card_StorePaymentSwitch,
            localizedContext
        )
        binding.addressFormInput.initLocalizedContext(localizedContext)
    }

    private fun observeDelegate(delegate: CardDelegate, coroutineScope: CoroutineScope) {
        delegate.outputDataFlow
            .onEach { outputDataChanged(it) }
            .launchIn(coroutineScope)
    }

    private fun outputDataChanged(cardOutputData: CardOutputData) {
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
        updateAddressHint(cardOutputData.addressUIState, cardOutputData.addressState.isOptional)
        setCardList(cardOutputData.cardBrands, cardOutputData.isCardListVisible)
        updateAddressLookupInputText(cardOutputData.addressState)
    }

    @Suppress("ComplexMethod", "LongMethod")
    override fun highlightValidationErrors() {
        cardDelegate.outputData.let {
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
                binding.textInputLayoutExpiryDate.showError(localizedContext.getString(expiryDateValidation.reason))
            }
            val securityCodeValidation = it.securityCodeState.validation
            if (securityCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutSecurityCode.requestFocus()
                }
                binding.textInputLayoutSecurityCode.showError(localizedContext.getString(securityCodeValidation.reason))
            }
            val holderNameValidation = it.holderNameState.validation
            if (binding.textInputLayoutCardHolder.isVisible && holderNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutCardHolder.requestFocus()
                }
                binding.textInputLayoutCardHolder.showError(localizedContext.getString(holderNameValidation.reason))
            }
            val postalCodeValidation = it.addressState.postalCode.validation
            if (binding.textInputLayoutPostalCode.isVisible && postalCodeValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutPostalCode.requestFocus()
                }
                binding.textInputLayoutPostalCode.showError(localizedContext.getString(postalCodeValidation.reason))
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
            val kcpBirthDateOrTaxNumberValidation = it.kcpBirthDateOrTaxNumberState.validation
            if (binding.textInputLayoutKcpBirthDateOrTaxNumber.isVisible &&
                kcpBirthDateOrTaxNumberValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutKcpBirthDateOrTaxNumber.requestFocus()
                }
                binding.textInputLayoutKcpBirthDateOrTaxNumber.showError(
                    localizedContext.getString(kcpBirthDateOrTaxNumberValidation.reason)
                )
            }
            val kcpPasswordValidation = it.kcpCardPasswordState.validation
            if (binding.textInputLayoutKcpCardPassword.isVisible && kcpPasswordValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutKcpCardPassword.requestFocus()
                }
                binding.textInputLayoutKcpCardPassword.showError(
                    localizedContext.getString(kcpPasswordValidation.reason)
                )
            }
            if (binding.addressFormInput.isVisible && !it.addressState.isValid) {
                binding.addressFormInput.highlightValidationErrors(isErrorFocused)
            }
        }
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
            val firtDetectedCardType = detectedCardTypes.first()
            binding.cardBrandLogoImageViewPrimary.strokeWidth = RoundCornerImageView.DEFAULT_STROKE_WIDTH
            binding.cardBrandLogoImageViewPrimary.loadLogo(
                environment = cardDelegate.componentParams.environment,
                txVariant = detectedCardTypes[0].cardBrand.txVariant,
                placeholder = R.drawable.ic_card,
                errorFallback = R.drawable.ic_card,
            )
            setDualBrandedCardImages(detectedCardTypes, cardOutputData.cardNumberState.validation)

            // TODO: 29/01/2021 get this logic from OutputData
            val isAmex = detectedCardTypes.any { it.cardBrand == CardBrand(cardType = CardType.AMERICAN_EXPRESS) }
            binding.editTextCardNumber.setAmexCardFormat(isAmex)

            if (detectedCardTypes.size == 1 &&
                firtDetectedCardType.panLength == binding.editTextCardNumber.rawValue.length
            ) {
                val cardNumberValidation = cardOutputData.cardNumberState.validation
                if (cardNumberValidation is Validation.Invalid) {
                    setCardNumberError(cardNumberValidation.reason)
                } else {
                    goToNextInputIfFocus(binding.editTextCardNumber)
                }
            }
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
                binding.cardBrandLogoImageViewSecondary.loadLogo(
                    environment = cardDelegate.componentParams.environment,
                    txVariant = detectedCardType.cardBrand.txVariant,
                    placeholder = R.drawable.ic_card,
                    errorFallback = R.drawable.ic_card,
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
            setCardErrorState(true)
            cardDelegate.updateInputData { cardNumber = binding.editTextCardNumber.rawValue }
        }
        binding.editTextCardNumber.onFocusChangeListener = OnFocusChangeListener { _: View?, hasFocus: Boolean ->
            setCardErrorState(hasFocus)
        }
    }

    private fun setCardErrorState(hasFocus: Boolean) {
        val outputData = cardDelegate.outputData

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
            binding.textInputLayoutCardNumber.hideError()
            binding.cardBrandLogoContainerPrimary.isVisible = true
            binding.cardBrandLogoContainerSecondary.isVisible = shouldShowSecondaryLogo
        } else {
            binding.textInputLayoutCardNumber.showError(localizedContext.getString(stringResId))
            binding.cardBrandLogoContainerPrimary.isVisible = false
            binding.cardBrandLogoContainerSecondary.isVisible = false
        }
    }

    private fun initCardBrandLogoViews(selectedIndex: Int) {
        when (selectedIndex) {
            UNSELECTED_BRAND_INDEX -> deselectBrands()
            PRIMARY_BRAND_INDEX -> selectPrimaryBrand()
            SECONDARY_BRAND_INDEX -> selectSecondaryBrand()
            else -> throw CheckoutException("Illegal brand index selected. Selected index must be either 0 or 1.")
        }
    }

    private fun initBrandSelectionListeners() {
        binding.cardBrandLogoContainerPrimary.setOnClickListener {
            cardDelegate.updateInputData { selectedCardIndex = PRIMARY_BRAND_INDEX }
            selectPrimaryBrand()
        }

        binding.cardBrandLogoContainerSecondary.setOnClickListener {
            cardDelegate.updateInputData { selectedCardIndex = SECONDARY_BRAND_INDEX }
            selectSecondaryBrand()
        }
    }

    private fun resetBrandSelectionInput() {
        binding.cardBrandLogoContainerPrimary.setOnClickListener(null)
        binding.cardBrandLogoContainerSecondary.setOnClickListener(null)
    }

    private fun deselectBrands() {
        binding.cardBrandLogoImageViewPrimary.alpha = UNSELECTED_BRAND_LOGO_ALPHA
        binding.cardBrandLogoImageViewSecondary.alpha = UNSELECTED_BRAND_LOGO_ALPHA
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
            cardDelegate.updateInputData { expiryDate = date }
            binding.textInputLayoutExpiryDate.hideError()
        }
        binding.editTextExpiryDate.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val expiryDateValidation = cardDelegate.outputData.expiryDateState.validation
            if (hasFocus) {
                binding.textInputLayoutExpiryDate.hideError()
            } else if (expiryDateValidation is Validation.Invalid) {
                binding.textInputLayoutExpiryDate.showError(localizedContext.getString(expiryDateValidation.reason))
            }
        }
    }

    private fun initSecurityCodeInput() {
        val securityCodeEditText = binding.textInputLayoutSecurityCode.editText as? SecurityCodeInput
        securityCodeEditText?.setOnChangeListener { editable: Editable ->
            cardDelegate.updateInputData { securityCode = editable.toString() }
            binding.textInputLayoutSecurityCode.hideError()
        }
        securityCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val securityCodeValidation = cardDelegate.outputData.securityCodeState.validation
            if (hasFocus) {
                binding.textInputLayoutSecurityCode.hideError()
            } else if (securityCodeValidation is Validation.Invalid) {
                binding.textInputLayoutSecurityCode.showError(localizedContext.getString(securityCodeValidation.reason))
            }
        }
    }

    private fun initHolderNameInput() {
        val cardHolderEditText = binding.textInputLayoutCardHolder.editText as? AdyenTextInputEditText
        cardHolderEditText?.setOnChangeListener { editable: Editable ->
            cardDelegate.updateInputData { holderName = editable.toString() }
            binding.textInputLayoutCardHolder.hideError()
        }
        cardHolderEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val holderNameValidation = cardDelegate.outputData.holderNameState.validation
            if (hasFocus) {
                binding.textInputLayoutCardHolder.hideError()
            } else if (holderNameValidation is Validation.Invalid) {
                binding.textInputLayoutCardHolder.showError(localizedContext.getString(holderNameValidation.reason))
            }
        }
    }

    private fun initSocialSecurityNumberInput() {
        val socialSecurityNumberEditText =
            binding.textInputLayoutSocialSecurityNumber.editText as? AdyenTextInputEditText
        socialSecurityNumberEditText?.setOnChangeListener { editable ->
            cardDelegate.updateInputData { socialSecurityNumber = editable.toString() }
            binding.textInputLayoutSocialSecurityNumber.hideError()
        }
        socialSecurityNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val socialSecurityNumberValidation = cardDelegate.outputData.socialSecurityNumberState.validation
            if (hasFocus) {
                binding.textInputLayoutSocialSecurityNumber.hideError()
            } else if (socialSecurityNumberValidation is Validation.Invalid) {
                binding.textInputLayoutSocialSecurityNumber.showError(
                    localizedContext.getString(socialSecurityNumberValidation.reason)
                )
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
            cardDelegate.updateInputData { kcpBirthDateOrTaxNumber = it.toString() }
            binding.textInputLayoutKcpBirthDateOrTaxNumber.hideError()
        }

        kcpBirthDateOrRegistrationNumberEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val kcpBirthDateOrTaxNumberValidation = cardDelegate.outputData.kcpBirthDateOrTaxNumberState.validation
            if (hasFocus) {
                binding.textInputLayoutKcpBirthDateOrTaxNumber.hideError()
            } else if (kcpBirthDateOrTaxNumberValidation is Validation.Invalid) {
                binding.textInputLayoutKcpBirthDateOrTaxNumber.showError(
                    localizedContext.getString(kcpBirthDateOrTaxNumberValidation.reason)
                )
            }
        }
    }

    private fun initKcpCardPasswordInput() {
        val kcpPasswordEditText = binding.textInputLayoutKcpCardPassword.editText as? AdyenTextInputEditText
        kcpPasswordEditText?.setOnChangeListener {
            cardDelegate.updateInputData { kcpCardPassword = it.toString() }
            binding.textInputLayoutKcpCardPassword.hideError()
        }

        kcpPasswordEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val kcpBirthDateOrRegistrationNumberValidation = cardDelegate.outputData.kcpCardPasswordState.validation
            if (hasFocus) {
                binding.textInputLayoutKcpCardPassword.hideError()
            } else if (kcpBirthDateOrRegistrationNumberValidation is Validation.Invalid) {
                binding.textInputLayoutKcpCardPassword.showError(
                    localizedContext.getString(kcpBirthDateOrRegistrationNumberValidation.reason)
                )
            }
        }
    }

    private fun initPostalCodeInput() {
        val postalCodeEditText = binding.textInputLayoutPostalCode.editText as? AdyenTextInputEditText
        postalCodeEditText?.setOnChangeListener {
            cardDelegate.updateInputData { address.postalCode = it.toString() }
            binding.textInputLayoutPostalCode.hideError()
        }

        postalCodeEditText?.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val postalCodeValidation = cardDelegate.outputData.addressState.postalCode.validation
            if (hasFocus) {
                binding.textInputLayoutPostalCode.hideError()
            } else if (postalCodeValidation is Validation.Invalid) {
                binding.textInputLayoutPostalCode.showError(localizedContext.getString(postalCodeValidation.reason))
            }
        }
    }

    private fun initAddressFormInput(coroutineScope: CoroutineScope) {
        binding.addressFormInput.attachDelegate(cardDelegate, coroutineScope)
    }

    private fun initAddressLookup() {
        binding.autoCompleteTextViewAddressLookup.setOnClickListener {
            cardDelegate.startAddressLookup()
        }
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
                    R.style.AdyenCheckout_Card_SecurityCodeInput,
                    localizedContext
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
                // We don't expect the hidden status to change back to isVisible, so we don't worry about putting the
                // margin back.
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
                    R.style.AdyenCheckout_Card_ExpiryDateInput,
                    localizedContext
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
                binding.textInputLayoutPostalCode.isVisible = false
                binding.textInputLayoutAddressLookup.isVisible = false
                binding.addressFormInput.isVisible = true
            }

            AddressFormUIState.POSTAL_CODE -> {
                binding.addressFormInput.isVisible = false
                binding.textInputLayoutAddressLookup.isVisible = false
                binding.textInputLayoutPostalCode.isVisible = true
            }

            AddressFormUIState.NONE -> {
                binding.addressFormInput.isVisible = false
                binding.textInputLayoutPostalCode.isVisible = false
                binding.textInputLayoutAddressLookup.isVisible = false
            }

            AddressFormUIState.LOOKUP -> {
                binding.addressFormInput.isVisible = false
                binding.textInputLayoutPostalCode.isVisible = false
                binding.textInputLayoutAddressLookup.isVisible = true
            }
        }
    }

    private fun updateAddressLookupInputText(addressOutputData: AddressOutputData) {
        binding.autoCompleteTextViewAddressLookup.setText(addressOutputData.toString())
    }

    private fun updateAddressHint(addressFormUIState: AddressFormUIState, isOptional: Boolean) {
        when (addressFormUIState) {
            AddressFormUIState.FULL_ADDRESS -> binding.addressFormInput.updateAddressHint(isOptional)
            AddressFormUIState.POSTAL_CODE -> {
                val postalCodeStyleResId = if (isOptional) {
                    R.style.AdyenCheckout_PostalCodeInput_Optional
                } else {
                    R.style.AdyenCheckout_PostalCodeInput
                }
                binding.textInputLayoutPostalCode.setLocalizedHintFromStyle(postalCodeStyleResId, localizedContext)
            }

            else -> {
                // no ops
            }
        }
    }

    private fun updateInstallmentSelection(installmentModel: InstallmentModel?) {
        installmentModel?.let {
            cardDelegate.updateInputData { installmentOption = it }
        }
    }

    private fun getActivity(context: Context): Activity? {
        return when (context) {
            is Activity -> context
            is ContextWrapper -> getActivity(context.baseContext)
            else -> null
        }
    }

    private fun setCardList(cards: List<CardListItem>, isCardListVisible: Boolean) {
        binding.recyclerViewCardList.isVisible = isCardListVisible
        if (isCardListVisible) {
            if (cardListAdapter == null) {
                cardListAdapter = CardListAdapter()
                binding.recyclerViewCardList.adapter = cardListAdapter
            }
            cardListAdapter?.submitList(cards)
        }
    }

    override fun getView(): View = this

    companion object {
        private const val UNSELECTED_BRAND_LOGO_ALPHA = 0.2f
        private const val SELECTED_BRAND_LOGO_ALPHA = 1f
        private const val UNSELECTED_BRAND_INDEX = -1
        private const val PRIMARY_BRAND_INDEX = 0
        private const val SECONDARY_BRAND_INDEX = 1
    }
}
