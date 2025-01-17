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
import com.adyen.checkout.ui.core.internal.ui.ComponentView
import com.adyen.checkout.ui.core.internal.ui.CountryAdapter
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel
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

    init {
        orientation = VERTICAL

        val padding = resources.getDimension(UICoreR.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initView(delegate: ComponentDelegate, coroutineScope: CoroutineScope, localizedContext: Context) {
        require(delegate is MBWayDelegate) { "Unsupported delegate type" }
        this.delegate = delegate
        this.localizedContext = localizedContext

        observeDelegate(delegate, coroutineScope)

        initMobileNumberInput()
        initCountryInput()
    }


    private fun observeDelegate(delegate: MBWayDelegate, coroutineScope: CoroutineScope) {
        delegate.viewStateFlow
            .onEach { updateMobileNumberInput(it.phoneNumberFieldState) }
            .launchIn(coroutineScope)
    }

    private fun initMobileNumberInput() {
        binding.editTextMobileNumber.setOnChangeListener {
            delegate.onFieldValueChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, it.toString())
        }
        binding.editTextMobileNumber.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            delegate.onFieldFocusChanged(MBWayFieldId.LOCAL_PHONE_NUMBER, hasFocus)
        }
    }

    private fun updateMobileNumberInput(phoneNumberFieldState: ComponentFieldViewState<String>) {
        phoneNumberFieldState.errorMessageId?.let { errorMessageId ->
            binding.textInputLayoutMobileNumber.showError(
                localizedContext.getString(errorMessageId),
            )
        } ?: run {
            binding.textInputLayoutMobileNumber.hideError()
        }
    }

    private fun initCountryInput() {
        val countries = delegate.getSupportedCountries()
        val adapter = CountryAdapter(context, localizedContext)
        adapter.setItems(countries)
        binding.autoCompleteTextViewCountry.apply {
            // disable editing and hide cursor
            inputType = 0
            setAdapter(adapter)
            setOnItemClickListener { _, _, position, _ ->
                val country = adapter.getItem(position)
                onCountrySelected(country)
            }
        }
        delegate.getInitiallySelectedCountry()?.let {
            binding.autoCompleteTextViewCountry.setText(it.toShortString())
            onCountrySelected(it)
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
