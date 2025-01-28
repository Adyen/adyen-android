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
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.mbway.databinding.MbwayViewBinding
import com.adyen.checkout.mbway.internal.ui.MBWayDelegate
import com.adyen.checkout.mbway.internal.ui.model.MBWayFieldId
import com.adyen.checkout.mbway.internal.ui.model.MBWayViewState
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.CountryAdapter
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
import com.adyen.checkout.ui.core.internal.ui.view.AdyenTextInputEditText
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.showError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.adyen.checkout.ui.core.R as UICoreR

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

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is MBWayDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        initMobileNumberInput()
        initCountryInput()

        observeDelegate(delegate, coroutineScope)
    }

    private fun initCountryInput() {
        countryAdapter = CountryAdapter(context, localizedContext)
        binding.autoCompleteTextViewCountry.apply {
            // disable editing and hide cursor
            inputType = 0
            setAdapter(countryAdapter)
            setOnItemClickListener { _, _, position, _ ->
                val country = countryAdapter.getItem(position)
                onCountrySelected(country)
            }
        }
    }

    private fun initMobileNumberInput() {
        binding.editTextMobileNumber.setOnChangeListener {
            delegate.onFieldValueChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, it.toString())
        }
        binding.editTextMobileNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            delegate.onFieldFocusChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, hasFocus)
        }
    }

    private fun observeDelegate(delegate: MBWayDelegate, coroutineScope: CoroutineScope) {
        delegate.viewStateFlow
            .onEach { viewStateUpdated(it) }
            .launchIn(coroutineScope)
    }

    private fun viewStateUpdated(mbWayViewState: MBWayViewState) {
        updateCountries(mbWayViewState.countries)
        updateCountryInput(mbWayViewState.countryCodeFieldState)
        updateMobileNumberInput(mbWayViewState.phoneNumberFieldState)
    }

    private fun updateCountries(countries: List<CountryModel>) = countryAdapter.setItems(countries)

    private fun updateCountryInput(countryCodeFieldState: ComponentFieldViewState<CountryModel>) {
        val country = countryCodeFieldState.value
        binding.autoCompleteTextViewCountry.setText(country.toShortString())
    }

    private fun updateMobileNumberInput(phoneNumberFieldState: ComponentFieldViewState<String>) {
        binding.editTextMobileNumber.updateText(phoneNumberFieldState.value)

        if (phoneNumberFieldState.hasFocus) {
            binding.textInputLayoutMobileNumber.requestFocus()
        } else {
            binding.textInputLayoutMobileNumber.clearFocus()
        }

        phoneNumberFieldState.errorMessageId?.let { errorMessageId ->
            binding.textInputLayoutMobileNumber.showError(
                localizedContext.getString(errorMessageId),
            )
        } ?: run {
            binding.textInputLayoutMobileNumber.hideError()
        }
    }

    private fun AdyenTextInputEditText.updateText(newValue: String) {
        // Removing this condition will not cause cyclic triggers, but it is good to not make an update unless necessary
        if (text?.toString() != newValue) {
            setText(newValue)
            setSelection(length())
        }
    }

    override fun highlightValidationErrors() {
        // Not used
    }

    override fun getView(): View = this

    private fun onCountrySelected(countryModel: CountryModel) {
        delegate.onFieldValueChanged(MBWayFieldId.COUNTRY_CODE, countryModel.callingCode)
    }
}
