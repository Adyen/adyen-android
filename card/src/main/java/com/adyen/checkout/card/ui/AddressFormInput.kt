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
import android.widget.LinearLayout
import com.adyen.checkout.card.AddressInputModel
import com.adyen.checkout.card.R
import com.adyen.checkout.components.ui.view.AdyenTextInputEditText

class AddressFormInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): LinearLayout(context, attrs, defStyleAttr) {

    private var listener: OnAddressChangeListener? = null
    private val addressInput = AddressInputModel()

    init {
        orientation = VERTICAL
        layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        populateFormFields(AddressSpecification.US)
    }

    fun setOnAddressChangeListener(listener: OnAddressChangeListener) {
        this.listener = listener
    }

    fun removeOnAddressChangeListener() {
        this.listener = null
    }

    private fun populateFormFields(specification: AddressSpecification) {
        val layoutResId = when (specification) {
            AddressSpecification.BR -> R.layout.address_form_br
            AddressSpecification.CA -> R.layout.address_form_ca
            AddressSpecification.GB -> R.layout.address_form_gb
            AddressSpecification.US -> R.layout.address_form_us
            AddressSpecification.DEFAULT -> R.layout.address_form_default
        }

        LayoutInflater.from(context).inflate(R.layout.address_form_input, this, true)
        val formContainer = rootView.findViewById<LinearLayout>(R.id.linearLayout_formContainer)
        LayoutInflater.from(context).inflate(layoutResId, formContainer, true)
    }

    private fun initForm() {

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_street).setOnChangeListener {
            addressInput.street = it.toString()
            listener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_apartmentSuite).setOnChangeListener {
            addressInput.houseNumberOrName = it.toString()
            listener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_postalCode).setOnChangeListener {
            addressInput.postalCode = it.toString()
            listener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_city).setOnChangeListener {
            addressInput.city = it.toString()
            listener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.editText_provinceTerritory).setOnChangeListener {
            addressInput.stateOrProvince = it.toString()
            listener?.onChanged(addressInput)
        }

        rootView.findViewById<AdyenTextInputEditText>(R.id.autoCompleteTextView_state).setOnChangeListener {
            addressInput.stateOrProvince = it.toString()
            listener?.onChanged(addressInput)
        }
    }

    enum class AddressSpecification {
        BR, CA, GB, US, DEFAULT
    }

    interface OnAddressChangeListener {
        fun onChanged(address: AddressInputModel)
    }
}