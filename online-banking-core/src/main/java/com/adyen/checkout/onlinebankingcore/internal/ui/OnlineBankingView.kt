/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 15/2/2023.
 */
package com.adyen.checkout.onlinebankingcore.internal.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import com.adyen.checkout.components.base.ComponentDelegate
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.onlinebankingcore.R
import com.adyen.checkout.onlinebankingcore.databinding.OnlineBankingViewBinding
import com.adyen.checkout.onlinebankingcore.internal.ui.model.OnlineBankingModel
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.SimpleTextListAdapter
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope

internal class OnlineBankingView @JvmOverloads constructor(
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

    private val binding: OnlineBankingViewBinding =
        OnlineBankingViewBinding.inflate(LayoutInflater.from(context), this)

    private val issuersAdapter: SimpleTextListAdapter<OnlineBankingModel> = SimpleTextListAdapter(context)

    private lateinit var localizedContext: Context

    private lateinit var onlineBankingDelegate: OnlineBankingDelegate<*>

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        if (delegate !is OnlineBankingDelegate<*>) throw IllegalArgumentException("Unsupported delegate type")
        onlineBankingDelegate = delegate

        this.localizedContext = localizedContext
        initLocalizedStrings(localizedContext)

        issuersAdapter.setItems(onlineBankingDelegate.getIssuers())

        binding.autoCompleteTextViewOnlineBanking.apply {
            inputType = 0
            setAdapter(issuersAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                Logger.d(TAG, "onItemSelected - ${issuersAdapter.getItem(position).name}")
                onlineBankingDelegate.updateInputData { selectedIssuer = issuersAdapter.getItem(position) }
                binding.textInputLayoutOnlineBanking.hideError()
            }
        }
        binding.textviewTermsAndConditions.setOnClickListener {
            onlineBankingDelegate.openTermsAndConditions(context)
        }
    }

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val output = onlineBankingDelegate.outputData
        val selectedIssuersValidation = output.selectedIssuerField.validation
        if (!selectedIssuersValidation.isValid()) {
            val errorReasonResId = (selectedIssuersValidation as Validation.Invalid).reason
            binding.textInputLayoutOnlineBanking.apply {
                requestFocus()
                showError(localizedContext.getString(errorReasonResId))
            }
        }
    }

    private fun initLocalizedStrings(localizedContext: Context) {
        binding.textInputLayoutOnlineBanking
            .setLocalizedHintFromStyle(
                R.style.AdyenCheckout_OnlineBanking_TermsAndConditionsInputLayout,
                localizedContext
            )
        binding.textviewTermsAndConditions.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_OnlineBanking_TermsAndConditionsTextView,
            localizedContext,
            formatHyperLink = true
        )
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        binding.autoCompleteTextViewOnlineBanking.isEnabled = enabled
        binding.textInputLayoutOnlineBanking.isEnabled = enabled
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
