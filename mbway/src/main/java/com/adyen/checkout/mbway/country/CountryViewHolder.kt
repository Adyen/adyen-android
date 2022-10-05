/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/12/2020.
 */

package com.adyen.checkout.mbway.country

import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.mbway.R
import com.adyen.checkout.mbway.databinding.CountryViewBinding

class CountryViewHolder(private val binding: CountryViewBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(country: CountryModel) {
        with(binding) {
            textViewFlag.text = country.emoji
            textViewCountry.text = root.context.getString(
                R.string.checkout_mbway_country_name_format,
                country.countryName,
                country.callingCode
            )
        }
    }
}
