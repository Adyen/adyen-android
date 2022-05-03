/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 1/3/2022.
 */

package com.adyen.checkout.card.ui

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.model.AddressListItem
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

    private val autoCompleteTextViewCountry: AutoCompleteTextView?
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
        get() = rootView.findViewById<LinearLayout>(R.id.linearLayout_formContainer)
            .findViewById(R.id.editText_postalCode)

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
        get() = rootView.findViewById<LinearLayout>(R.id.linearLayout_formContainer)
            .findViewById(R.id.textInputLayout_postalCode)

    private val textInputLayoutCity: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_city)

    private val textInputLayoutProvinceTerritory: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_provinceTerritory)

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.address_form_input, this, true)

        autoCompleteTextViewCountry?.apply {
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

    fun highlightValidationErrors(addressOutputData: AddressOutputData) {
        var isErrorFocused = false
        val streetValidation = addressOutputData.street.validation
        if (streetValidation is Validation.Invalid) {
            isErrorFocused = true
            textInputLayoutStreet?.requestFocus()
            textInputLayoutStreet?.error = resources.getString(streetValidation.reason)
        }
        val houseNumberValidation = addressOutputData.houseNumberOrName.validation
        if (houseNumberValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutHouseNumber?.requestFocus()
            }
            textInputLayoutHouseNumber?.error = resources.getString(houseNumberValidation.reason)
        }
        val postalCodeValidation = addressOutputData.postalCode.validation
        if (postalCodeValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutPostalCode?.requestFocus()
            }
            textInputLayoutPostalCode?.error = resources.getString(postalCodeValidation.reason)
        }
        val cityValidation = addressOutputData.city.validation
        if (cityValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutCity?.requestFocus()
            }
            textInputLayoutCity?.error = resources.getString(cityValidation.reason)
        }
        val provinceTerritoryValidation = addressOutputData.stateOrProvince.validation
        if (provinceTerritoryValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutProvinceTerritory?.requestFocus()
            }
            textInputLayoutProvinceTerritory?.error = resources.getString(provinceTerritoryValidation.reason)
        }
    }

    fun updateCountries(countryList: List<AddressListItem>) {
        countryAdapter.setItems(countryList)
        countryList.firstOrNull { it.selected }?.let {
            val selectedSpecification = AddressSpecification.fromString(it.code)
            val formContainer = rootView.findViewById<LinearLayout>(R.id.linearLayout_formContainer)
            if (formContainer.childCount == 0) {
                autoCompleteTextViewCountry?.setText(it.name)
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

        val formContainer = rootView.findViewById<LinearLayout>(R.id.linearLayout_formContainer)
        formContainer.removeAllViews()
        LayoutInflater.from(context).inflate(layoutResId, formContainer, true)
        initForm(specification)
    }

    private fun initForm(addressSpecification: AddressSpecification) {
        initHeader()
        initCountryInput()
        initStreetInput(addressSpecification.streetHintResId)
        initHouseNumberInput()
        initApartmentSuiteInput()
        initPostalCodeInput(addressSpecification.postalCodeHintResId)
        initCityInput(addressSpecification.cityHintResId)
        initProvinceTerritoryInput(addressSpecification.stateHintResId)
        initStatesInput(addressSpecification.stateHintResId)
    }

    private fun initHeader() {
        textViewHeader.text = localizedContext.getString(R.string.checkout_address_form_billing_address_title)
    }

    private fun initCountryInput() {
        textInputLayoutCountry?.hint = localizedContext.getString(R.string.checkout_address_form_country_hint)
    }

    private fun initStreetInput(hintResId: Int) {
        textInputLayoutStreet?.hint = localizedContext.getString(hintResId)
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

    private fun initHouseNumberInput() {
        textInputLayoutHouseNumber?.hint = localizedContext.getString(R.string.checkout_address_form_house_number_hint)
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

    private fun initApartmentSuiteInput() {
        textInputLayoutApartmentSuite?.hint = localizedContext.getString(R.string.checkout_address_form_apartment_suite_hint)
        editTextApartmentSuite?.apply {
            setText(component.inputData.address.apartmentSuite)
            setOnChangeListener {
                component.inputData.address.apartmentSuite = it.toString()
                notifyInputDataChanged()
            }
        }
    }

    private fun initPostalCodeInput(hintResId: Int) {
        textInputLayoutPostalCode?.hint = localizedContext.getString(hintResId)
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

    private fun initCityInput(hintResId: Int) {
        textInputLayoutCity?.hint = localizedContext.getString(hintResId)
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

    private fun initProvinceTerritoryInput(hintResId: Int) {
        textInputLayoutProvinceTerritory?.hint = localizedContext.getString(hintResId)
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

    private fun initStatesInput(hintResId: Int) {
        autoCompleteTextViewState?.apply {
            hint = localizedContext.getString(hintResId)
            statesAdapter.getItem { it.code == component.inputData.address.stateOrProvince }
            inputType = 0
            setAdapter(statesAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                component.inputData.address.stateOrProvince = statesAdapter.getItem(position).code
                notifyInputDataChanged()
            }
        }
    }

    private fun notifyInputDataChanged() {
        component.inputDataChanged(component.inputData)
    }

    /**
     * Specification for address form alternatives depending on the country.
     */
    enum class AddressSpecification(
        val streetHintResId: Int,
        val postalCodeHintResId: Int,
        val cityHintResId: Int,
        val stateHintResId: Int
    ) {
        BR(
            R.string.checkout_address_form_street_hint,
            R.string.checkout_card_postal_code_hint,
            R.string.checkout_address_form_city_hint,
            R.string.checkout_address_form_states_hint
        ),
        CA(
            R.string.checkout_address_form_address_hint,
            R.string.checkout_card_postal_code_hint,
            R.string.checkout_address_form_city_hint,
            R.string.checkout_address_form_province_territory_hint
        ),
        GB(
            R.string.checkout_address_form_street_hint,
            R.string.checkout_card_postal_code_hint,
            R.string.checkout_address_form_city_town_hint,
            R.string.checkout_address_form_province_territory_hint
        ),
        US(
            R.string.checkout_address_form_address_hint,
            R.string.checkout_address_form_zip_code_hint,
            R.string.checkout_address_form_city_hint,
            R.string.checkout_address_form_states_hint
        ),
        DEFAULT(
            R.string.checkout_address_form_street_hint,
            R.string.checkout_card_postal_code_hint,
            R.string.checkout_address_form_city_hint,
            R.string.checkout_address_form_province_territory_hint
        );

        companion object {
            fun fromString(countryCode: String?): AddressSpecification {
                return values().firstOrNull { it.name == countryCode } ?: DEFAULT
            }
        }
    }
}
