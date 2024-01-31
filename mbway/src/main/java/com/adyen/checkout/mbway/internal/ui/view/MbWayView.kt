/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/9/2022.
 */

package com.adyen.checkout.mbway.internal.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.LinearLayout
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.mbway.R
import com.adyen.checkout.mbway.databinding.MbwayViewBinding
import com.adyen.checkout.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.mbway.internal.ui.model.FocussedView
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.CountryAdapter
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class MbWayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr),
    ComponentView {

    private val binding = MbwayViewBinding.inflate(LayoutInflater.from(context), this)

    private lateinit var localizedContext: Context

    private lateinit var delegate: MBWayDelegate

    private lateinit var countryAdapter: CountryAdapter

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is MBWayDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initMobileNumberInput()
        initCountryInput()

        delegate.viewStateFlow
            .onEach(::onViewState)
            .launchIn(coroutineScope)
    }

    private fun initMobileNumberInput() {
        binding.editTextMobileNumber.setOnChangeListener {
            delegate.onPhoneNumberChanged(it.toString())
        }

        binding.editTextMobileNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            if (hasFocus) {
                delegate.onViewFocussed(FocussedView.PHONE_NUMBER)
            }
        }
        // TODO: Remove after testing
        binding.editTextTest.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            if (hasFocus) {
                delegate.onViewFocussed(FocussedView.TEST)
            }
        }
    }

    private fun initCountryInput() {
        countryAdapter = CountryAdapter(context, localizedContext)
        binding.autoCompleteTextViewCountry.apply {
            // disable editing and hide cursor
            inputType = 0
            setAdapter(countryAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val country = countryAdapter.getItem(position)
                delegate.onCountrySelected(country)
            }
        }
    }

    private fun onViewState(viewState: MBWayViewState) {
        binding.autoCompleteTextViewCountry.setText(viewState.selectedCountry.toShortString())

        countryAdapter.setItems(viewState.countries)

        binding.editTextMobileNumber.apply {
            val newInput = viewState.phoneNumber
            if (text?.toString() != newInput) {
                setText(newInput)
                setSelection(newInput.length)
            }
        }

        if (viewState.phoneNumberError != null) {
            binding.textInputLayoutMobileNumber.showError(
                localizedContext.getString(viewState.phoneNumberError.messageRes),
            )
            if (viewState.phoneNumberError.requestFocus) {
                binding.editTextMobileNumber.requestFocus()
            }
        } else {
            binding.textInputLayoutMobileNumber.hideError()
        }
    }

    override fun highlightValidationErrors() {
        delegate.highlightValidationErrors()
    }

    override fun getView(): View = this

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
