/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.ui.core.databinding.CountryViewBinding
import com.adyen.checkout.ui.core.old.internal.ui.model.CountryModel

internal class CountryViewHolder(private val binding: CountryViewBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(country: CountryModel) {
        with(binding) {
            textViewCountryCode.text = country.callingCode
            textViewCountry.text = country.countryName
        }
    }
}
