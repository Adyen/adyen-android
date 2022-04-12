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
import com.adyen.checkout.card.R
import com.adyen.checkout.card.api.model.AddressItem
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.ui.adapter.SimpleTextListAdapter
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText

class AddressFormInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var onAddressChangeListener: OnAddressChangeListener? = null
    private var countryAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)
    private var statesAdapter: SimpleTextListAdapter<AddressListItem> = SimpleTextListAdapter(context)

    private lateinit var autoCompleteTextViewCountry: AutoCompleteTextView

    private val addressInput = AddressInputModel()

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.address_form_input, this, true)

        autoCompleteTextViewCountry = rootView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_country).apply {
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

    fun updateCountries(countryList: List<AddressListItem>) {
        countryAdapter.setItems(countryList)
        autoCompleteTextViewCountry.setText(countryList.firstOrNull { it.selected }?.name)
        populateFormFields(AddressSpecification.fromString(countryList.firstOrNull { it.selected }?.code))
    }

    fun updateStates(stateList: List<AddressListItem>) {
        statesAdapter.setItems(stateList)
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

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_street).setOnChangeListener {
            addressInput.street = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_apartmentSuite)
            ?.setOnChangeListener {
                addressInput.houseNumberOrName = it.toString()
                onAddressChangeListener?.onChanged(addressInput)
            }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_postalCode)
            ?.setOnChangeListener {
                addressInput.postalCode = it.toString()
                onAddressChangeListener?.onChanged(addressInput)
            }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_city)?.setOnChangeListener {
            addressInput.city = it.toString()
            onAddressChangeListener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_provinceTerritory)
            ?.setOnChangeListener {
                addressInput.stateOrProvince = it.toString()
                onAddressChangeListener?.onChanged(addressInput)
            }

        rootView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView_state)?.apply {
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
