/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/3/2022.
 */

package com.adyen.checkout.card.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * AddressFormInput to be used in [CardComponent].
 */
@Suppress("TooManyFunctions")
class AddressFormInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var localizedContext: Context

    private lateinit var component: CardComponent

    private var countryAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)
    private var statesAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)

    private val textViewHeader: TextView
        get() = rootView.findViewById(R.id.textView_header)

    private val formContainer: LinearLayout
        get() = rootView.findViewById(R.id.linearLayout_formContainer)

    private val autoCompleteTextViewCountry: AutoCompleteTextView
        get() = rootView.findViewById(R.id.autoCompleteTextView_country)

    private val autoCompleteTextViewState: AutoCompleteTextView?
        get() = rootView.findViewById(R.id.autoCompleteTextView_state)

    private val editTextStreet: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_street)

    private val editTextHouseNumber: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_houseNumber)

    private val editTextApartmentSuite: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_apartmentSuite)

    private val editTextPostalCode: AdyenTextInputEditText?
        get() = formContainer.findViewById(R.id.editText_postalCode)

    private val editTextCity: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_city)

    private val editTextProvinceTerritory: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_provinceTerritory)

    private val textInputLayoutCountry: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_country)

    private val textInputLayoutStreet: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_street)

    private val textInputLayoutHouseNumber: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_houseNumber)

    private val textInputLayoutApartmentSuite: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_apartmentSuite)

    private val textInputLayoutPostalCode: TextInputLayout?
        get() = formContainer.findViewById(R.id.textInputLayout_postalCode)

    private val textInputLayoutCity: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_city)

    private val textInputLayoutProvinceTerritory: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_provinceTerritory)

    private val textInputLayoutState: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_state)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.address_form_input, this, true)

        autoCompleteTextViewCountry.apply {
            inputType = 0
            setAdapter(countryAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedCountryCode = countryAdapter.getItem(position).code
                if (component.inputData.address.country != selectedCountryCode) {
                    component.inputData.address.reset()
                    component.inputData.address.country = selectedCountryCode
                    notifyInputDataChanged()
                    populateFormFields(AddressSpecification.fromString(selectedCountryCode))
                }
            }
        }
    }

    fun attachComponent(component: CardComponent) {
        this.component = component
    }

    fun initLocalizedContext(localizedContext: Context) {
        this.localizedContext = localizedContext
    }

    fun highlightValidationErrors(isErrorFocusedPreviously: Boolean) {
        var isErrorFocused = isErrorFocusedPreviously
        val streetValidation = component.outputData?.addressState?.street?.validation
        if (streetValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutStreet?.requestFocus()
            }
            textInputLayoutStreet?.error = localizedContext.getString(streetValidation.reason)
        }
        val houseNumberValidation = component.outputData?.addressState?.houseNumberOrName?.validation
        if (houseNumberValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutHouseNumber?.requestFocus()
            }
            textInputLayoutHouseNumber?.error = localizedContext.getString(houseNumberValidation.reason)
        }
        val apartmentSuiteValidation = component.outputData?.addressState?.apartmentSuite?.validation
        if (apartmentSuiteValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutApartmentSuite?.requestFocus()
            }
            textInputLayoutApartmentSuite?.error = localizedContext.getString(apartmentSuiteValidation.reason)
        }
        val postalCodeValidation = component.outputData?.addressState?.postalCode?.validation
        if (postalCodeValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutPostalCode?.requestFocus()
            }
            textInputLayoutPostalCode?.error = localizedContext.getString(postalCodeValidation.reason)
        }
        val cityValidation = component.outputData?.addressState?.city?.validation
        if (cityValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutCity?.requestFocus()
            }
            textInputLayoutCity?.error = localizedContext.getString(cityValidation.reason)
        }
        val provinceTerritoryValidation = component.outputData?.addressState?.stateOrProvince?.validation
        if (provinceTerritoryValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutProvinceTerritory?.requestFocus()
            }
            textInputLayoutProvinceTerritory?.error = localizedContext.getString(provinceTerritoryValidation.reason)
        }
    }

    fun updateCountries(countryList: List<AddressListItem>) {
        countryAdapter.setItems(countryList)
        countryList.firstOrNull { it.selected }?.let {
            val selectedSpecification = AddressSpecification.fromString(it.code)
            if (formContainer.childCount == 0) {
                autoCompleteTextViewCountry.setText(it.name)
                populateFormFields(selectedSpecification)
            }
        }
    }

    fun updateStates(stateList: List<AddressListItem>) {
        statesAdapter.setItems(stateList)
        stateList.firstOrNull { it.selected }?.let {
            autoCompleteTextViewState?.setText(it.name)
            component.inputData.address.stateOrProvince = it.code
        }
    }

    private fun populateFormFields(
        specification: AddressSpecification
    ) {
        val layoutResId = when (specification) {
            AddressSpecification.BR -> R.layout.address_form_br
            AddressSpecification.CA -> R.layout.address_form_ca
            AddressSpecification.GB -> R.layout.address_form_gb
            AddressSpecification.US -> R.layout.address_form_us
            AddressSpecification.DEFAULT -> R.layout.address_form_default
        }

        val hadFocus = hasFocus()
        formContainer.removeAllViews()
        LayoutInflater.from(context).inflate(layoutResId, formContainer, true)
        initForm(specification)
        if (hadFocus) requestFocus()
    }

    private fun initForm(addressSpecification: AddressSpecification) {
        val isOptional = component.outputData?.addressState?.isOptional ?: false
        initHeader()
        initCountryInput(addressSpecification.country.styleResId)
        initStreetInput(addressSpecification.street.getStyleResId(isOptional))
        initHouseNumberInput(addressSpecification.houseNumber.getStyleResId(isOptional))
        initApartmentSuiteInput(addressSpecification.apartmentSuite.getStyleResId(isOptional))
        initPostalCodeInput(addressSpecification.postalCode.getStyleResId(isOptional))
        initCityInput(addressSpecification.city.getStyleResId(isOptional))
        initProvinceTerritoryInput(addressSpecification.stateProvince.getStyleResId(isOptional))
        initStatesInput(addressSpecification.stateProvince.getStyleResId(isOptional))
    }

    private fun initHeader() {
        textViewHeader.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_AddressForm_HeaderTextAppearance,
            localizedContext
        )
    }

    private fun initCountryInput(styleResId: Int) {
        textInputLayoutCountry?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
    }

    private fun initStreetInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutStreet?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextStreet?.apply {
            setText(component.inputData.address.street)
            setOnChangeListener {
                component.inputData.address.street = it.toString()
                notifyInputDataChanged()
                textInputLayoutStreet?.error = null
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = component.outputData?.addressState?.street?.validation
                if (hasFocus) {
                    textInputLayoutStreet?.error = null
                } else if (validation != null && validation is Validation.Invalid) {
                    textInputLayoutStreet?.error = localizedContext.getString(validation.reason)
                }
            }
        }
    }

    private fun initHouseNumberInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutHouseNumber?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextHouseNumber?.apply {
            setText(component.inputData.address.houseNumberOrName)
            setOnChangeListener {
                component.inputData.address.houseNumberOrName = it.toString()
                notifyInputDataChanged()
                textInputLayoutHouseNumber?.error = null
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = component.outputData?.addressState?.houseNumberOrName?.validation
                if (hasFocus) {
                    textInputLayoutHouseNumber?.error = null
                } else if (validation != null && validation is Validation.Invalid) {
                    textInputLayoutHouseNumber?.error = localizedContext.getString(validation.reason)
                }
            }
        }
    }

    private fun initApartmentSuiteInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutApartmentSuite?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextApartmentSuite?.apply {
            setText(component.inputData.address.apartmentSuite)
            setOnChangeListener {
                component.inputData.address.apartmentSuite = it.toString()
                notifyInputDataChanged()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = component.outputData?.addressState?.apartmentSuite?.validation
                if (hasFocus) {
                    textInputLayoutApartmentSuite?.error = null
                } else if (validation != null && validation is Validation.Invalid) {
                    textInputLayoutApartmentSuite?.error = localizedContext.getString(validation.reason)
                }
            }
        }
    }

    private fun initPostalCodeInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutPostalCode?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextPostalCode?.apply {
            setText(component.inputData.address.postalCode)
            setOnChangeListener {
                component.inputData.address.postalCode = it.toString()
                notifyInputDataChanged()
                textInputLayoutPostalCode?.error = null
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = component.outputData?.addressState?.postalCode?.validation
                if (hasFocus) {
                    textInputLayoutPostalCode?.error = null
                } else if (validation != null && validation is Validation.Invalid) {
                    textInputLayoutPostalCode?.error = localizedContext.getString(validation.reason)
                }
            }
        }
    }

    private fun initCityInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutCity?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextCity?.apply {
            setText(component.inputData.address.city)
            setOnChangeListener {
                component.inputData.address.city = it.toString()
                notifyInputDataChanged()
                textInputLayoutCity?.error = null
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = component.outputData?.addressState?.city?.validation
                if (hasFocus) {
                    textInputLayoutCity?.error = null
                } else if (validation != null && validation is Validation.Invalid) {
                    textInputLayoutCity?.error = localizedContext.getString(validation.reason)
                }
            }
        }
    }

    private fun initProvinceTerritoryInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutProvinceTerritory?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextProvinceTerritory?.apply {
            setText(component.inputData.address.stateOrProvince)
            setOnChangeListener {
                component.inputData.address.stateOrProvince = it.toString()
                notifyInputDataChanged()
                textInputLayoutProvinceTerritory?.error = null
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = component.outputData?.addressState?.stateOrProvince?.validation
                if (hasFocus) {
                    textInputLayoutProvinceTerritory?.error = null
                } else if (validation != null && validation is Validation.Invalid) {
                    textInputLayoutProvinceTerritory?.error = localizedContext.getString(validation.reason)
                }
            }
        }
    }

    private fun initStatesInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutState?.setLocalizedHintFromStyle(it, localizedContext) }
        autoCompleteTextViewState?.apply {
            setText(statesAdapter.getItem { it.selected }?.name)
            inputType = 0
            setAdapter(statesAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                component.inputData.address.stateOrProvince = statesAdapter.getItem(position).code
                notifyInputDataChanged()
            }
        }
    }

    fun updateAddressHint(isOptional: Boolean) {
        val spec = AddressSpecification.fromString(component.inputData.address.country)

        val streetStyleResId = spec.street.getStyleResId(isOptional)
        streetStyleResId?.let {
            textInputLayoutStreet?.setLocalizedHintFromStyle(it, localizedContext)
        }

        val houseNumberStyleResId = spec.houseNumber.getStyleResId(isOptional)
        houseNumberStyleResId?.let {
            textInputLayoutHouseNumber?.setLocalizedHintFromStyle(it, localizedContext)
        }

        val apartmentSuiteStyleResId = spec.apartmentSuite.getStyleResId(isOptional)
        apartmentSuiteStyleResId?.let {
            textInputLayoutApartmentSuite?.setLocalizedHintFromStyle(it, localizedContext)
        }

        val postalCodeStyleResId = spec.postalCode.getStyleResId(isOptional)
        postalCodeStyleResId?.let {
            textInputLayoutPostalCode?.setLocalizedHintFromStyle(it, localizedContext)
        }

        val cityStyleResId = spec.city.getStyleResId(isOptional)
        cityStyleResId?.let {
            textInputLayoutCity?.setLocalizedHintFromStyle(it, localizedContext)
        }

        val provinceTerritoryStyleResId = spec.stateProvince.getStyleResId(isOptional)
        provinceTerritoryStyleResId?.let {
            textInputLayoutProvinceTerritory?.setLocalizedHintFromStyle(it, localizedContext)
        }

        val statesStyleResId = spec.stateProvince.getStyleResId(isOptional)
        statesStyleResId?.let { textInputLayoutState?.setLocalizedHintFromStyle(it, localizedContext) }
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(component.inputData)
    }
}
