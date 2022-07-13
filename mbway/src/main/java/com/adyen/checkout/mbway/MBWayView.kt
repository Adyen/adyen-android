/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/12/2020.
 */

package com.adyen.checkout.mbway

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.MBWayPaymentMethod
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.view.AdyenLinearLayout
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.adyen.checkout.components.util.CountryUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.mbway.country.CountryAdapter
import com.adyen.checkout.mbway.country.CountryModel
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

private val TAG = LogUtil.getTag()

@Suppress("TooManyFunctions")
class MBWayView :
    AdyenLinearLayout<MBWayOutputData, MBWayConfiguration, PaymentComponentState<MBWayPaymentMethod>, MBWayComponent>,
    Observer<MBWayOutputData>,
    AdapterView.OnItemClickListener {

    private var mMobileNumberInput: TextInputLayout? = null
    private var mCountryAutoCompleteTextView: AutoCompleteTextView? = null

    private var mMobileNumberEditText: AdyenTextInputEditText? = null

    private var mCountryAdapter: CountryAdapter? = null
    private var selectedCountry: CountryModel? = null

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        init()
    }

    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(
        context,
        attributeSet,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        orientation = VERTICAL

        LayoutInflater.from(context).inflate(R.layout.mbway_view, this, true)

        val padding = resources.getDimension(R.dimen.standard_margin).toInt()
        setPadding(padding, padding, padding, 0)
    }

    override fun initLocalizedStrings(localizedContext: Context) {
        mMobileNumberInput?.setLocalizedHintFromStyle(R.style.AdyenCheckout_MBWay_MobileNumberInput)
    }

    override fun initView() {
        mMobileNumberInput = findViewById(R.id.textInputLayout_mobileNumber)
        mMobileNumberEditText = (mMobileNumberInput?.editText as? AdyenTextInputEditText)
        mCountryAutoCompleteTextView = findViewById(R.id.autoCompleteTextView_country)
        val mMobileNumberEditText = mMobileNumberEditText
        val mCountryAutoCompleteTextView = mCountryAutoCompleteTextView
        val mMobileNumberInput = mMobileNumberInput
        if (mMobileNumberEditText == null || mCountryAutoCompleteTextView == null || mMobileNumberInput == null) {
            throw CheckoutException("Could not find views inside layout.")
        }
        mMobileNumberEditText.setOnChangeListener {
            localNumberChanged()
            mMobileNumberInput.error = null
        }
        mMobileNumberEditText.onFocusChangeListener = OnFocusChangeListener { _, hasFocus: Boolean ->
            val outputData = component.outputData
            val mobilePhoneNumberValidation = outputData?.mobilePhoneNumberFieldState?.validation
            if (hasFocus) {
                mMobileNumberInput.error = null
            } else if (outputData != null && mobilePhoneNumberValidation is Validation.Invalid) {
                mMobileNumberInput.error = localizedContext.getString(mobilePhoneNumberValidation.reason)
            }
        }
        val countries = getCountries()
        val adapter = CountryAdapter(context, localizedContext)
        adapter.setItems(countries)
        mCountryAdapter = adapter
        // disable editing and hide cursor
        mCountryAutoCompleteTextView.inputType = 0
        mCountryAutoCompleteTextView.setAdapter(adapter)
        mCountryAutoCompleteTextView.onItemClickListener = this
        val firstCountry = countries.firstOrNull()
        if (firstCountry != null) {
            mCountryAutoCompleteTextView.setText(firstCountry.toShortString())
            countrySelected(firstCountry)
        }
    }

    private fun getCountries(): List<CountryModel> {
        val countriesInfo = CountryUtils.getCountries(component.getSupportedCountries())
        return countriesInfo.map {
            CountryModel(
                isoCode = it.isoCode,
                countryName = CountryUtils.getCountryName(it.isoCode, getShopperLocale()),
                callingCode = it.callingCode,
                emoji = it.emoji
            )
        }
    }

    private fun getShopperLocale(): Locale {
        return component.configuration.shopperLocale
    }

    private fun countrySelected(countryModel: CountryModel) {
        selectedCountry = countryModel
        countryCodeChanged()
    }

    override fun observeComponentChanges(lifecycleOwner: LifecycleOwner) {
        component.observeOutputData(lifecycleOwner, this)
    }

    override fun onComponentAttached() {
        // nothing to impl
    }

    override fun onChanged(mbWayOutputData: MBWayOutputData?) {
        Logger.v(TAG, "MBWayOutputData changed")
    }

    override val isConfirmationRequired: Boolean
        get() = true

    override fun highlightValidationErrors() {
        Logger.d(TAG, "highlightValidationErrors")
        val mobilePhoneNumberValidation = component.outputData?.mobilePhoneNumberFieldState?.validation
        if (mobilePhoneNumberValidation is Validation.Invalid) {
            mMobileNumberInput?.error = localizedContext.getString(mobilePhoneNumberValidation.reason)
        }
    }

    private fun localNumberChanged() {
        component.inputData.localPhoneNumber = mMobileNumberEditText?.rawValue.orEmpty()
        notifyInputDataChanged()
    }

    private fun countryCodeChanged() {
        component.inputData.countryCode = selectedCountry?.callingCode.orEmpty()
        notifyInputDataChanged()
    }

    private fun notifyInputDataChanged() {
        component.notifyInputDataChanged()
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val country = mCountryAdapter?.getCountries()?.get(position) ?: return
        countrySelected(country)
    }
}
