/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 21/2/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.view

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.OnFocusChangeListener
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.autofill.HintConstants
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.internal.ui.AddressDelegate
import com.adyen.checkout.ui.core.internal.ui.AddressSpecification
import com.adyen.checkout.ui.core.internal.ui.SimpleTextListAdapter
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.util.hideError
import com.adyen.checkout.ui.core.internal.util.setLocalizedHintFromStyle
import com.adyen.checkout.ui.core.internal.util.setLocalizedTextFromStyle
import com.adyen.checkout.ui.core.internal.util.showError
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * AddressFormInput to be used in other modules.
 */
@Suppress("TooManyFunctions")
class AddressFormInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var localizedContext: Context

    private lateinit var delegate: AddressDelegate

    private var currentSpec: AddressSpecification? = null

    private var countryAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)
    private var statesAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)

    private val textViewHeader: TextView?
        get() = rootView.findViewById(R.id.textView_header)

    private val formContainer: LinearLayout?
        get() = rootView.findViewById(R.id.linearLayout_formContainer)

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
        get() = formContainer?.findViewById(R.id.editText_postalCode)

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
        get() = formContainer?.findViewById(R.id.textInputLayout_postalCode)

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

        autoCompleteTextViewCountry?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_COUNTRY)
            }
            inputType = 0
            setAdapter(countryAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedCountryCode = countryAdapter.getItem(position).code
                if (delegate.addressOutputData.country.value != selectedCountryCode) {
                    delegate.updateAddressInputData {
                        // Only reset state/province, so filled in data is retained.
                        stateOrProvince = ""
                        country = selectedCountryCode
                    }
                    populateFormFields(AddressSpecification.fromString(selectedCountryCode))
                }
                textInputLayoutCountry?.hideError()
            }
        }
    }

    fun attachDelegate(delegate: AddressDelegate, coroutineScope: CoroutineScope) {
        this.delegate = delegate
        subscribeCountryAndStateList(coroutineScope)
    }

    private fun subscribeCountryAndStateList(coroutineScope: CoroutineScope) {
        delegate.addressOutputDataFlow.onEach { addressOutputData ->
            updateCountries(addressOutputData.countryOptions)
            updateStates(addressOutputData.stateOptions)
        }.launchIn(coroutineScope)
    }

    fun initLocalizedContext(localizedContext: Context) {
        this.localizedContext = localizedContext
    }

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    fun highlightValidationErrors(isErrorFocusedPreviously: Boolean) {
        var isErrorFocused = isErrorFocusedPreviously
        val countryValidation = delegate.addressOutputData.country.validation
        if (countryValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutCountry?.requestFocus()
            }
            textInputLayoutCountry?.showError(localizedContext.getString(countryValidation.reason))
        }
        val streetValidation = delegate.addressOutputData.street.validation
        if (streetValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutStreet?.requestFocus()
            }
            textInputLayoutStreet?.showError(localizedContext.getString(streetValidation.reason))
        }
        val houseNumberValidation = delegate.addressOutputData.houseNumberOrName.validation
        if (houseNumberValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutHouseNumber?.requestFocus()
            }
            textInputLayoutHouseNumber?.showError(localizedContext.getString(houseNumberValidation.reason))
        }
        val apartmentSuiteValidation = delegate.addressOutputData.apartmentSuite.validation
        if (apartmentSuiteValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutApartmentSuite?.requestFocus()
            }
            textInputLayoutApartmentSuite?.showError(localizedContext.getString(apartmentSuiteValidation.reason))
        }
        val postalCodeValidation = delegate.addressOutputData.postalCode.validation
        if (postalCodeValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutPostalCode?.requestFocus()
            }
            textInputLayoutPostalCode?.showError(localizedContext.getString(postalCodeValidation.reason))
        }
        val cityValidation = delegate.addressOutputData.city.validation
        if (cityValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                isErrorFocused = true
                textInputLayoutCity?.requestFocus()
            }
            textInputLayoutCity?.showError(localizedContext.getString(cityValidation.reason))
        }
        val provinceTerritoryValidation = delegate.addressOutputData.stateOrProvince.validation
        if (provinceTerritoryValidation is Validation.Invalid) {
            if (!isErrorFocused) {
                @Suppress("UNUSED_VALUE")
                isErrorFocused = true
                textInputLayoutProvinceTerritory?.requestFocus()
                textInputLayoutState?.requestFocus()
            }
            textInputLayoutProvinceTerritory?.showError(localizedContext.getString(provinceTerritoryValidation.reason))
            textInputLayoutState?.showError(localizedContext.getString(provinceTerritoryValidation.reason))
        }
    }

    private fun updateCountries(countryList: List<AddressListItem>) {
        val currentSelected = countryAdapter.getItem { it.selected }
        countryAdapter.setItems(countryList)
        val selectedCountry = countryList.firstOrNull { it.selected }
        val selectedSpecification = AddressSpecification.fromString(selectedCountry?.code)
        if (selectedSpecification != currentSpec || currentSelected != selectedCountry) {
            currentSpec = selectedSpecification
            autoCompleteTextViewCountry?.setText(selectedCountry?.name)
            populateFormFields(selectedSpecification)
        } else {
            updateInputFields(delegate.addressOutputData)
        }
    }

    private fun updateStates(stateList: List<AddressListItem>) {
        statesAdapter.setItems(stateList)
        stateList.firstOrNull { it.selected }?.let {
            autoCompleteTextViewState?.setText(it.name)
            val didStateChange = delegate.addressOutputData.stateOrProvince.value != it.code
            if (didStateChange) {
                delegate.updateAddressInputData { stateOrProvince = it.code }
            }
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
        formContainer?.removeAllViews()
        LayoutInflater.from(context).inflate(layoutResId, formContainer, true)
        updateInputFields(delegate.addressOutputData)
        initForm(specification)
        if (hadFocus) requestFocus()
    }

    private fun initForm(addressSpecification: AddressSpecification) {
        initHeader()
        initCountryInput(addressSpecification.country.styleResId)
        initStreetInput(
            styleResId = addressSpecification.street.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
        initHouseNumberInput(
            styleResId = addressSpecification.houseNumber.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
        initApartmentSuiteInput(
            styleResId = addressSpecification.apartmentSuite.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
        initPostalCodeInput(
            styleResId = addressSpecification.postalCode.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
        initCityInput(
            styleResId = addressSpecification.city.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
        initProvinceTerritoryInput(
            styleResId = addressSpecification.stateProvince.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
        initStatesInput(
            styleResId = addressSpecification.stateProvince.getStyleResId(
                isOptional = delegate.addressOutputData.isOptional,
            ),
        )
    }

    private fun initHeader() {
        textViewHeader?.setLocalizedTextFromStyle(
            R.style.AdyenCheckout_AddressForm_HeaderTextAppearance,
            localizedContext,
        )
    }

    private fun initCountryInput(styleResId: Int) {
        textInputLayoutCountry?.setLocalizedHintFromStyle(
            styleResId,
            localizedContext,
        )
        val text = delegate.addressOutputData.countryOptions.firstOrNull { it.selected }?.name
        autoCompleteTextViewCountry?.setText(text)
    }

    private fun initStreetInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutStreet?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextStreet?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_STREET_ADDRESS)
            }

            setOnChangeListener {
                delegate.updateAddressInputData { street = it.toString() }
                textInputLayoutStreet?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.addressOutputData.street.validation
                if (hasFocus) {
                    textInputLayoutStreet?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutStreet?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initHouseNumberInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutHouseNumber?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextHouseNumber?.apply {

            setOnChangeListener {
                delegate.updateAddressInputData { houseNumberOrName = it.toString() }
                textInputLayoutHouseNumber?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.addressOutputData.houseNumberOrName.validation
                if (hasFocus) {
                    textInputLayoutHouseNumber?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutHouseNumber?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initApartmentSuiteInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutApartmentSuite?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextApartmentSuite?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_APT_NUMBER)
            }

            setOnChangeListener {
                delegate.updateAddressInputData { apartmentSuite = it.toString() }
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.addressOutputData.apartmentSuite.validation
                if (hasFocus) {
                    textInputLayoutApartmentSuite?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutApartmentSuite?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initPostalCodeInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutPostalCode?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextPostalCode?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_CODE)
            }

            setOnChangeListener {
                delegate.updateAddressInputData { postalCode = it.toString() }
                textInputLayoutPostalCode?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.addressOutputData.postalCode.validation
                if (hasFocus) {
                    textInputLayoutPostalCode?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutPostalCode?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initCityInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutCity?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextCity?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_LOCALITY)
            }

            setOnChangeListener {
                delegate.updateAddressInputData { city = it.toString() }
                textInputLayoutCity?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.addressOutputData.city.validation
                if (hasFocus) {
                    textInputLayoutCity?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutCity?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initProvinceTerritoryInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutProvinceTerritory?.setLocalizedHintFromStyle(it, localizedContext) }
        editTextProvinceTerritory?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_REGION)
            }

            setOnChangeListener {
                delegate.updateAddressInputData { stateOrProvince = it.toString() }
                textInputLayoutProvinceTerritory?.hideError()
            }
            onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
                val validation = delegate.addressOutputData.stateOrProvince.validation
                if (hasFocus) {
                    textInputLayoutProvinceTerritory?.hideError()
                } else if (validation is Validation.Invalid) {
                    textInputLayoutProvinceTerritory?.showError(localizedContext.getString(validation.reason))
                }
            }
        }
    }

    private fun initStatesInput(styleResId: Int?) {
        styleResId?.let { textInputLayoutState?.setLocalizedHintFromStyle(it, localizedContext) }
        autoCompleteTextViewState?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setAutofillHints(HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_REGION)
            }
            setText(statesAdapter.getItem { it.selected }?.name)
            inputType = 0
            setAdapter(statesAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                delegate.updateAddressInputData { stateOrProvince = statesAdapter.getItem(position).code }
                textInputLayoutState?.hideError()
            }
        }
    }

    private fun updateInputFields(outputData: AddressOutputData) {
        editTextStreet?.setTextIfChanged(outputData.street.value)
        editTextHouseNumber?.setTextIfChanged(outputData.houseNumberOrName.value)
        editTextApartmentSuite?.setTextIfChanged(outputData.apartmentSuite.value)
        editTextPostalCode?.setTextIfChanged(outputData.postalCode.value)
        editTextCity?.setTextIfChanged(outputData.city.value)
        editTextProvinceTerritory?.setTextIfChanged(outputData.stateOrProvince.value)
    }

    private fun TextView.setTextIfChanged(newText: String?) {
        if (newText != text.toString()) {
            text = newText
        }
    }

    fun updateAddressHint(isOptional: Boolean) {
        val spec = AddressSpecification.fromString(delegate.addressOutputData.country.value)

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
}
