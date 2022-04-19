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
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.AddressOutputData
import com.adyen.checkout.card.R
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddressFormInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var onAddressChangeListener: OnAddressChangeListener? = null
    private var countryAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)
    private var statesAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)

    private val autoCompleteTextViewCountry: AutoCompleteTextView?
        get() = rootView.findViewById(R.id.autoCompleteTextView_country)

    private val autoCompleteTextViewState: AutoCompleteTextView?
        get() = rootView.findViewById(R.id.autoCompleteTextView_state)

    private val editTextStreet: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_street)

    private val editTextHouseNumber: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_houseNumber)

    private val editTextPostalCode: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_postalCode)

    private val editTextCity: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_city)

    private val editTextProvinceTerritory: AdyenTextInputEditText?
        get() = rootView.findViewById(R.id.editText_provinceTerritory)

    private val textInputLayoutStreet: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_street)

    private val textInputLayoutHouseNumber: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_houseNumber)

    private val textInputLayoutPostalCode: TextInputLayout?
        get() = rootView.findViewById<LinearLayout>(R.id.linearLayout_formContainer)
            .findViewById(R.id.textInputLayout_postalCode)

    private val textInputLayoutCity: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_city)

    private val textInputLayoutProvinceTerritory: TextInputLayout?
        get() = rootView.findViewById(R.id.textInputLayout_provinceTerritory)

    private val addressInput = AddressInputModel()

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.address_form_input, this, true)

        autoCompleteTextViewCountry?.apply {
            inputType = 0
            setAdapter(countryAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectedCountryCode = countryAdapter.getItem(position).code
                addressInput.country = selectedCountryCode
                onAddressChangeListener?.onChanged(addressInput)
            }
        }
    }

    fun setOnAddressChangeListener(listener: OnAddressChangeListener) {
        this.onAddressChangeListener = listener
    }

    fun removeOnAddressChangeListener() {
        this.onAddressChangeListener = null
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
            autoCompleteTextViewCountry?.setText(it.name)
            populateFormFields(AddressSpecification.fromString(it.code))
            addressInput.country = it.code
        }
    }

    fun updateStates(stateList: List<AddressListItem>) {
        statesAdapter.setItems(stateList)
        stateList.firstOrNull { it.selected }?.let {
            autoCompleteTextViewState?.setText(it.name)
            addressInput.stateOrProvince = it.code
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
        initForm()
    }

    private fun initForm() {
        addressInput.reset()

        editTextStreet?.setOnChangeListener {
            addressInput.street = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        editTextHouseNumber?.setOnChangeListener {
            addressInput.houseNumberOrName = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        editTextPostalCode?.setOnChangeListener {
            addressInput.postalCode = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        editTextCity?.setOnChangeListener {
            addressInput.city = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        editTextProvinceTerritory?.setOnChangeListener {
            addressInput.stateOrProvince = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        autoCompleteTextViewState?.apply {
            inputType = 0
            setAdapter(statesAdapter)
            onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                addressInput.stateOrProvince = statesAdapter.getItem(position).code
                onAddressChangeListener?.onChanged(addressInput)
            }
        }
    }

    enum class AddressSpecification {
        BR, CA, GB, US, DEFAULT;

        companion object {
            fun fromString(countryCode: String?): AddressSpecification {
                return values().firstOrNull { it.name == countryCode } ?: DEFAULT
            }
        }
    }

    fun interface OnAddressChangeListener {
        fun onChanged(address: AddressInputModel)
    }
}
