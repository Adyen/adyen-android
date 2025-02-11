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
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.payto.R
import com.adyen.checkout.payto.databinding.PaytoViewBinding
import com.adyen.checkout.payto.internal.ui.PayToDelegate
import com.adyen.checkout.payto.internal.ui.model.PayIdType
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel
import com.adyen.checkout.payto.internal.ui.model.PayToMode
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import kotlinx.coroutines.CoroutineScope
import com.adyen.checkout.ui.core.R as UICoreR

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
        binding.editTextPayIdEmail.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_EmailEditText,
            localizedContext,
        )
        binding.editTextPayIdAbn.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_PayTo_PayId_AbnEditText,
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
        val payIdTypeAdapter = PayIdTypeAdapter(context, localizedContext)
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
        textInputLayoutPayIdPhoneNumber.isVisible = payIdTypeModel.type == PayIdType.MOBILE
        textInputLayoutPayIdEmail.isVisible = payIdTypeModel.type == PayIdType.EMAIL
        textInputLayoutPayIdAbn.isVisible = payIdTypeModel.type == PayIdType.ABN
        textInputLayoutPayIdOrganizationId.isVisible = payIdTypeModel.type == PayIdType.ORGANIZATION_ID
    }

    override fun highlightValidationErrors() {
        adyenLog(AdyenLogLevel.DEBUG) { "highlightValidationErrors" }
        // TODO Do validation
    }

    override fun getView(): View = this
}
