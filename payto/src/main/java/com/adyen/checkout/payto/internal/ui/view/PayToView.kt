/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/2/2025.
 */

package com.adyen.checkout.payto.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.payto.R
import com.adyen.checkout.payto.databinding.PaytoViewBinding
import com.adyen.checkout.payto.internal.ui.PayToDelegate
import com.adyen.checkout.payto.internal.ui.model.PayIdType
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel
import com.adyen.checkout.payto.internal.ui.model.PayToMode
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.LocalizedTextListAdapter
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.isVisible
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

@Suppress("TooManyFunctions")
internal class PayToView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr), ComponentView {

    private val binding = PaytoViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: PayToDelegate

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is PayToDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initLocalizedStrings(localizedContext)
        initModeSelector()
        initPayIdTypeSelector()
        initPayIdPhoneNumberInput()
        initPayIdEmailAddressInput()
        initPayIdAbnNumberInput()
        initPayIdOrganizationIdInput()
        initBsbAccountNumberInput()
        initBsbStateBranchInput()
        initFirstNameInput()
        initLastNameInput()
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textViewModeSelection.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_ModeSelectionTextView,
            localizedContext,
        )
        binding.buttonTogglePayId.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_ToggleButton,
            localizedContext,
        )
        binding.textViewPayIdDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_DescriptionTextView,
            localizedContext,
        )
        binding.editTextPayIdPhoneNumber.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_PhoneNumberEditText,
            localizedContext,
        )
        binding.editTextPayIdEmailAddress.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_EmailAddressEditText,
            localizedContext,
        )
        binding.editTextPayIdAbnNumber.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_AbnNumberEditText,
            localizedContext,
        )
        binding.editTextPayIdOrganizationId.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_OrganizationIdEditText,
            localizedContext,
        )
        binding.buttonToggleBsb.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_BSB_ToggleButton,
            localizedContext,
        )
        binding.textViewBsbDescription.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_BSB_DescriptionTextView,
            localizedContext,
        )
        binding.editTextBsbAccountNumber.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_BSB_AccountNumberEditText,
            localizedContext,
        )
        binding.editTextBsbStateBranch.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_BSB_StateBranchEditText,
            localizedContext,
        )
        binding.editTextFirstName.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_FirstNameEditText,
            localizedContext,
        )
        binding.editTextLastName.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_LastNameEditText,
            localizedContext,
        )
    }

    private fun initModeSelector() {
        binding.toggleButtonChoice.addOnButtonCheckedListener { _, checkedId, isChecked ->
            when (checkedId) {
                R.id.button_toggle_payId -> togglePayIdViews(isChecked)
                R.id.button_toggle_bsb -> toggleBsbViews(isChecked)
            }
        }
        binding.toggleButtonChoice.check(R.id.button_toggle_payId)
    }

    private fun togglePayIdViews(isChecked: Boolean) {
        binding.layoutPayIdContent.isVisible = isChecked
        binding.layoutBsbContent.isVisible = !isChecked

        if (isChecked) {
            delegate.updateInputData { mode = PayToMode.PAY_ID }
        }
    }

    private fun toggleBsbViews(isChecked: Boolean) {
        binding.layoutPayIdContent.isVisible = !isChecked
        binding.layoutBsbContent.isVisible = isChecked

        if (isChecked) {
            delegate.updateInputData { mode = PayToMode.BSB }
        }
    }

    private fun initPayIdTypeSelector() {
        val payIdTypes = delegate.getPayIdTypes()
        val payIdTypeAdapter = LocalizedTextListAdapter<PayIdTypeModel>(context, localizedContext)
        payIdTypeAdapter.setItems(payIdTypes)

        binding.autoCompleteTextViewPayIdType.apply {
            inputType = 0
            setAdapter(payIdTypeAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val item = payIdTypeAdapter.getItem(position)
                adyenLog(AdyenLogLevel.DEBUG) { "onItemSelected - ${item.type}" }
                onPayIdTypeSelected(item)
            }
        }

        payIdTypes.firstOrNull()?.let { payIdTypeModel ->
            val name = localizedContext.getString(payIdTypeModel.nameResId)
            binding.autoCompleteTextViewPayIdType.setText(name)
            onPayIdTypeSelected(payIdTypeModel)
        }
    }

    private fun onPayIdTypeSelected(payIdTypeModel: PayIdTypeModel) {
        togglePayIdTypeViews(payIdTypeModel)
        delegate.updateInputData { this.payIdTypeModel = payIdTypeModel }
    }

    private fun togglePayIdTypeViews(payIdTypeModel: PayIdTypeModel) = with(binding) {
        textInputLayoutPayIdPhoneNumber.isVisible = payIdTypeModel.type == PayIdType.PHONE
        textInputLayoutPayIdEmailAddress.isVisible = payIdTypeModel.type == PayIdType.EMAIL
        textInputLayoutPayIdAbnNumber.isVisible = payIdTypeModel.type == PayIdType.ABN
        textInputLayoutPayIdOrganizationId.isVisible = payIdTypeModel.type == PayIdType.ORGANIZATION_ID
    }

    private fun initPayIdPhoneNumberInput() {
        binding.editTextPayIdPhoneNumber.setOnChangeListener {
            delegate.updateInputData { phoneNumber = binding.editTextPayIdPhoneNumber.rawValue }
            binding.textInputLayoutPayIdPhoneNumber.hideError()
        }
        binding.editTextPayIdPhoneNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.phoneNumberFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutPayIdPhoneNumber.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutPayIdPhoneNumber.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initPayIdEmailAddressInput() {
        binding.editTextPayIdEmailAddress.setOnChangeListener {
            delegate.updateInputData { emailAddress = binding.editTextPayIdEmailAddress.rawValue }
            binding.textInputLayoutPayIdEmailAddress.hideError()
        }
        binding.editTextPayIdEmailAddress.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.emailAddressFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutPayIdEmailAddress.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutPayIdEmailAddress.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initPayIdAbnNumberInput() {
        binding.editTextPayIdAbnNumber.setOnChangeListener {
            delegate.updateInputData { abnNumber = binding.editTextPayIdAbnNumber.rawValue }
            binding.textInputLayoutPayIdAbnNumber.hideError()
        }
        binding.editTextPayIdAbnNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.abnNumberFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutPayIdAbnNumber.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutPayIdAbnNumber.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initPayIdOrganizationIdInput() {
        binding.editTextPayIdOrganizationId.setOnChangeListener {
            delegate.updateInputData { organizationId = binding.editTextPayIdOrganizationId.rawValue }
            binding.textInputLayoutPayIdOrganizationId.hideError()
        }
        binding.editTextPayIdOrganizationId.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.organizationIdFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutPayIdOrganizationId.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutPayIdOrganizationId.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initBsbAccountNumberInput() {
        binding.editTextBsbAccountNumber.setOnChangeListener {
            delegate.updateInputData { bsbAccountNumber = binding.editTextBsbAccountNumber.rawValue }
            binding.textInputLayoutBsbAccountNumber.hideError()
        }
        binding.editTextBsbAccountNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.bsbAccountNumberFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutBsbAccountNumber.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutBsbAccountNumber.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initBsbStateBranchInput() {
        binding.editTextBsbStateBranch.setOnChangeListener {
            delegate.updateInputData { bsbStateBranch = binding.editTextBsbStateBranch.rawValue }
            binding.textInputLayoutBsbStateBranch.hideError()
        }
        binding.editTextBsbStateBranch.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.bsbStateBranchFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutBsbStateBranch.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutBsbStateBranch.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initFirstNameInput() {
        binding.editTextFirstName.setOnChangeListener {
            delegate.updateInputData { firstName = binding.editTextFirstName.rawValue }
            binding.textInputLayoutFirstName.hideError()
        }
        binding.editTextFirstName.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.firstNameFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutFirstName.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutFirstName.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    private fun initLastNameInput() {
        binding.editTextLastName.setOnChangeListener {
            delegate.updateInputData { lastName = binding.editTextLastName.rawValue }
            binding.textInputLayoutLastName.hideError()
        }
        binding.editTextLastName.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            val validation = delegate.outputData.lastNameFieldState.validation
            if (hasFocus) {
                binding.textInputLayoutLastName.hideError()
            } else if (validation is Validation.Invalid) {
                binding.textInputLayoutLastName.showError(localizedContext.getString(validation.reason))
            }
        }
    }

    @Suppress("ComplexMethod", "LongMethod")
    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        delegate.outputData.let {
            var isErrorFocused = false

            val phoneNumberValidation = it.phoneNumberFieldState.validation
            if (isPayIdContentVisible() &&
                binding.textInputLayoutPayIdPhoneNumber.isVisible &&
                phoneNumberValidation is Validation.Invalid
            ) {
                isErrorFocused = true
                binding.textInputLayoutPayIdPhoneNumber.requestFocus()
                binding.textInputLayoutPayIdPhoneNumber.showError(
                    localizedContext.getString(phoneNumberValidation.reason),
                )
            }

            val emailAddressValidation = it.emailAddressFieldState.validation
            if (isPayIdContentVisible() &&
                binding.textInputLayoutPayIdEmailAddress.isVisible &&
                emailAddressValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutPayIdEmailAddress.requestFocus()
                }
                binding.textInputLayoutPayIdEmailAddress.showError(
                    localizedContext.getString(emailAddressValidation.reason),
                )
            }

            val abnNumberValidation = it.abnNumberFieldState.validation
            if (isPayIdContentVisible() &&
                binding.textInputLayoutPayIdAbnNumber.isVisible &&
                abnNumberValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutPayIdAbnNumber.requestFocus()
                }
                binding.textInputLayoutPayIdAbnNumber.showError(localizedContext.getString(abnNumberValidation.reason))
            }

            val organizationIdValidation = it.organizationIdFieldState.validation
            if (isPayIdContentVisible() &&
                binding.textInputLayoutPayIdOrganizationId.isVisible &&
                organizationIdValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutPayIdOrganizationId.requestFocus()
                }
                binding.textInputLayoutPayIdOrganizationId.showError(
                    localizedContext.getString(organizationIdValidation.reason),
                )
            }

            val bsbAccountNumberValidation = it.bsbAccountNumberFieldState.validation
            if (isBsbContentVisible() &&
                binding.textInputLayoutBsbAccountNumber.isVisible &&
                bsbAccountNumberValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutBsbAccountNumber.requestFocus()
                }
                binding.textInputLayoutBsbAccountNumber.showError(
                    localizedContext.getString(bsbAccountNumberValidation.reason),
                )
            }

            val bsbStateBranchValidation = it.bsbStateBranchFieldState.validation
            if (isBsbContentVisible() &&
                binding.textInputLayoutBsbStateBranch.isVisible &&
                bsbStateBranchValidation is Validation.Invalid
            ) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutBsbStateBranch.requestFocus()
                }
                binding.textInputLayoutBsbStateBranch.showError(
                    localizedContext.getString(bsbStateBranchValidation.reason),
                )
            }

            val firstNameValidation = it.firstNameFieldState.validation
            if (binding.textInputLayoutFirstName.isVisible && firstNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    isErrorFocused = true
                    binding.textInputLayoutFirstName.requestFocus()
                }
                binding.textInputLayoutFirstName.showError(localizedContext.getString(firstNameValidation.reason))
            }

            val lastNameValidation = it.lastNameFieldState.validation
            if (binding.textInputLayoutLastName.isVisible && lastNameValidation is Validation.Invalid) {
                if (!isErrorFocused) {
                    binding.textInputLayoutLastName.requestFocus()
                }
                binding.textInputLayoutLastName.showError(localizedContext.getString(lastNameValidation.reason))
            }
        }
    }

    private fun isPayIdContentVisible() = binding.layoutPayIdContent.isVisible

    private fun isBsbContentVisible() = binding.layoutBsbContent.isVisible

    override fun getView(): View = this
}
