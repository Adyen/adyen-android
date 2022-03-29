/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/12/2020.
 */

package com.adyen.checkout.mbway.country

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.mbway.R

class CountryViewHolder(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
    private val flagTextView: TextView = rootView.findViewById(R.id.textView_flag)
    private val countryTextView: TextView = rootView.findViewById(R.id.textView_country)

    fun bindItem(country: CountryModel) {
        flagTextView.text = country.emoji
        countryTextView.text = rootView.context.getString(
            R.string.checkout_mbway_country_name_format,
            country.countryName,
            country.callingCode
        )
    }
}
