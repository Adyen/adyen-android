/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2022.
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
import com.adyen.checkout.card.CardDelegate
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.extensions.hideError
import com.adyen.checkout.components.extensions.setLocalizedHintFromStyle
import com.adyen.checkout.components.extensions.setLocalizedTextFromStyle
import com.adyen.checkout.components.extensions.showError
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.google.android.material.textfield.TextInputLayout

// TODO: I have replaced all usage of inputData with outputData, but we have to make sure this works the same.

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

    private lateinit var delegate: CardDelegate

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
                // TODO: Check if works
                if (delegate.outputData.addressState.country.value != selectedCountryCode) {
                    delegate.updateInputData {
                        address.reset()
                        address.country = selectedCountryCode
                    }
                    populateFormFields(AddressSpecification.fromString(selectedCountryCode))
                }
            }
        }
    }

    internal fun attachDelegate(delegate: CardDelegate) {
        this.delegate = delegate
    }

    internal fun initLocalizedContext(localizedContext: Context) {
        this.localizedContext = localizedContext
    }

    internal fun highlightValidationErrors(isErrorFocusedPreviously: Boolean) {
        var isErrorFocused = isErrorFocusedPreviously
        val streetValidation = delegate.outputData.addressState.street.validation
        if (streetValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutStreet?.requestFocus()
            }
            textInputLayoutStreet?.showError(localizedContext.getString(streetValidation.reason))
        }
        val houseNumberValidation = delegate.outputData.addressState.houseNumberOrName.validation
        if (houseNumberValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutHouseNumber?.requestFocus()
            }
            textInputLayoutHouseNumber?.showError(localizedContext.getString(houseNumberValidation.reason))
        }
        val apartmentSuiteValidation = delegate.outputData.addressState.apartmentSuite.validation
        if (apartmentSuiteValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutApartmentSuite?.requestFocus()
            }
            textInputLayoutApartmentSuite?.showError(localizedContext.getString(apartmentSuiteValidation.reason))
        }
        val postalCodeValidation = delegate.outputData.addressState.postalCode.validation
        if (postalCodeValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutPostalCode?.requestFocus()
            }
            textInputLayoutPostalCode?.showError(localizedContext.getString(postalCodeValidation.reason))
        }
        val cityValidation = delegate.outputData.addressState.city.validation
        if (cityValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutCity?.requestFocus()
            }
            textInputLayoutCity?.showError(localizedContext.getString(cityValidation.reason))
        }
        val provinceTerritoryValidation = delegate.outputData.addressState.stateOrProvince.validation
        if (provinceTerritoryValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                @Suppress("UNUSED_VALUE")
                isErrorFocused = true
                textInputLayoutProvinceTerritory?.requestFocus()
            }
            textInputLayoutProvinceTerritory?.showError(localizedContext.getString(provinceTerritoryValidation.reason))
        }
    }

    internal fun updateCountries(countryList: List<AddressListItem>) {
        countryAdapter.setItems(countryList)
        countryList.firstOrNull { it.selected }?.let {
            val selectedSpecification = AddressSpecification.fromString(it.code)
            if (formContainer.childCount == 0) {
                autoCompleteTextViewCountry.setText(it.name)
                populateFormFields(selectedSpecification)
            }
        }
    }

    internal fun updateStates(stateList: List<AddressListItem>) {
        statesAdapter.setItems(stateList)
        stateList.firstOrNull { it.selected }?.let {
            autoCompleteTextViewState?.setText(it.name)
            delegate.updateInputData { address.stateOrProvince = it.code }
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
        initHeader()
        initCountryInput(addressSpecification.country.styleResId)
        initStreetInput(addressSpecification.street.styleResId)
        initHouseNumberInput(addressSpecification.houseNumber.styleResId)
        initApartmentSuiteInput(addressSpecification.apartmentSuite.styleResId)
        initPostalCodeInput(addressSpecification.postalCode.styleResId)
        initCityInput(addressSpecification.city.styleResId)
        initProvinceTerritoryInput(addressSpecification.stateProvince.styleResId)
        initStatesInput(addressSpecification.stateProvince.styleResId)
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

    private fun initStreetInput(styleResId: Int) {
        textInputLayoutStreet?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        editTextStreet?.apply {
            setText(delegate.outputData.addressState.street.value)
            setOnChangeListener {
                delegate.updateInputData { address.street = it.toString() }
                textInputLayoutStreet?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.outputData.addressState.street.validation
                if (hasFocus) {
                    textInputLayoutStreet?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutStreet?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initHouseNumberInput(styleResId: Int) {
        textInputLayoutHouseNumber?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        editTextHouseNumber?.apply {
            setText(delegate.outputData.addressState.houseNumberOrName.value)
            setOnChangeListener {
                delegate.updateInputData { address.houseNumberOrName = it.toString() }
                textInputLayoutHouseNumber?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.outputData.addressState.houseNumberOrName.validation
                if (hasFocus) {
                    textInputLayoutHouseNumber?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutHouseNumber?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initApartmentSuiteInput(styleResId: Int) {
        textInputLayoutApartmentSuite?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        editTextApartmentSuite?.apply {
            setText(delegate.outputData.addressState.apartmentSuite.value)
            setOnChangeListener {
                delegate.updateInputData { address.apartmentSuite = it.toString() }
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.outputData.addressState.apartmentSuite.validation
                if (hasFocus) {
                    textInputLayoutApartmentSuite?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutApartmentSuite?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initPostalCodeInput(styleResId: Int) {
        textInputLayoutPostalCode?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        editTextPostalCode?.apply {
            setText(delegate.outputData.addressState.postalCode.value)
            setOnChangeListener {
                delegate.updateInputData { address.postalCode = it.toString() }
                textInputLayoutPostalCode?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.outputData.addressState.postalCode.validation
                if (hasFocus) {
                    textInputLayoutPostalCode?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutPostalCode?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initCityInput(styleResId: Int) {
        textInputLayoutCity?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        editTextCity?.apply {
            setText(delegate.outputData.addressState.city.value)
            setOnChangeListener {
                delegate.updateInputData { address.city = it.toString() }
                textInputLayoutCity?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.outputData.addressState.city.validation
                if (hasFocus) {
                    textInputLayoutCity?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutCity?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initProvinceTerritoryInput(styleResId: Int) {
        textInputLayoutProvinceTerritory?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        editTextProvinceTerritory?.apply {
            setText(delegate.outputData.addressState.stateOrProvince.value)
            setOnChangeListener {
                delegate.updateInputData { address.stateOrProvince = it.toString() }
                textInputLayoutProvinceTerritory?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.outputData.addressState.stateOrProvince.validation
                if (hasFocus) {
                    textInputLayoutProvinceTerritory?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutProvinceTerritory?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initStatesInput(styleResId: Int) {
        textInputLayoutState?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext
        )
        autoCompleteTextViewState?.apply {
            setText(statesAdapter.getItem { it.selected }?.name)
            inputType = 0
            setAdapter(statesAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                delegate.updateInputData { address.stateOrProvince = statesAdapter.getItem(position).code }
            }
        }
    }

    /**
     * Specification for address form alternatives depending on the country.
     */
    @Suppress("LongParameterList")
    enum class AddressSpecification(
        internal val street: AddressFieldSpec,
        internal val houseNumber: AddressFieldSpec,
        internal val apartmentSuite: AddressFieldSpec,
        internal val postalCode: AddressFieldSpec,
        internal val city: AddressFieldSpec,
        internal val stateProvince: AddressFieldSpec,
        internal val country: AddressFieldSpec
    ) {
        BR(
            street = AddressFieldSpec(true, R.style.AdyenCheckout_Card_StreetInput),
            houseNumber = AddressFieldSpec(true, R.style.AdyenCheckout_Card_HouseNumberInput),
            apartmentSuite = AddressFieldSpec(false, R.style.AdyenCheckout_Card_ApartmentSuiteInput),
            postalCode = AddressFieldSpec(true, R.style.AdyenCheckout_Card_PostalCodeInput),
            city = AddressFieldSpec(true, R.style.AdyenCheckout_Card_CityInput),
            stateProvince = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput),
            country = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput)
        ),
        CA(
            street = AddressFieldSpec(true, R.style.AdyenCheckout_Card_AddressInput),
            houseNumber = AddressFieldSpec(false, 0),
            apartmentSuite = AddressFieldSpec(false, R.style.AdyenCheckout_Card_ApartmentSuiteInput),
            postalCode = AddressFieldSpec(true, R.style.AdyenCheckout_Card_PostalCodeInput),
            city = AddressFieldSpec(true, R.style.AdyenCheckout_Card_CityInput),
            stateProvince = AddressFieldSpec(true, R.style.AdyenCheckout_Card_ProvinceTerritoryInput),
            country = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput)
        ),
        GB(
            street = AddressFieldSpec(true, R.style.AdyenCheckout_Card_StreetInput),
            houseNumber = AddressFieldSpec(true, R.style.AdyenCheckout_Card_HouseNumberInput),
            apartmentSuite = AddressFieldSpec(false, 0),
            postalCode = AddressFieldSpec(true, R.style.AdyenCheckout_Card_PostalCodeInput),
            city = AddressFieldSpec(true, R.style.AdyenCheckout_Card_CityTownInput),
            stateProvince = AddressFieldSpec(false, 0),
            country = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput)
        ),
        US(
            street = AddressFieldSpec(true, R.style.AdyenCheckout_Card_AddressInput),
            houseNumber = AddressFieldSpec(false, 0),
            apartmentSuite = AddressFieldSpec(false, R.style.AdyenCheckout_Card_ApartmentSuiteInput),
            postalCode = AddressFieldSpec(true, R.style.AdyenCheckout_Card_ZipCodeInput),
            city = AddressFieldSpec(true, R.style.AdyenCheckout_Card_CityInput),
            stateProvince = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_StatesInput),
            country = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput)
        ),
        DEFAULT(
            street = AddressFieldSpec(true, R.style.AdyenCheckout_Card_StreetInput),
            houseNumber = AddressFieldSpec(true, R.style.AdyenCheckout_Card_HouseNumberInput),
            apartmentSuite = AddressFieldSpec(false, R.style.AdyenCheckout_Card_ApartmentSuiteInput),
            postalCode = AddressFieldSpec(true, R.style.AdyenCheckout_Card_PostalCodeInput),
            city = AddressFieldSpec(true, R.style.AdyenCheckout_Card_CityInput),
            stateProvince = AddressFieldSpec(true, R.style.AdyenCheckout_Card_ProvinceTerritoryInput),
            country = AddressFieldSpec(true, R.style.AdyenCheckout_DropdownTextInputLayout_CountryInput)
        );

        companion object {
            fun fromString(countryCode: String?): AddressSpecification {
                return values().firstOrNull { it.name == countryCode } ?: DEFAULT
            }
        }
    }

    internal data class AddressFieldSpec(
        val isRequired: Boolean,
        val styleResId: Int
    )
}
