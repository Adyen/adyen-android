/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.RestrictTo
import com.adyen.checkout.ui.core.databinding.CountryViewBinding
import com.adyen.checkout.ui.core.old.internal.ui.model.CountryModel

// We need context to inflate the views and localizedContext to fetch the strings
// Do not remove localizedContext! It's not used at the moment, however it is kept to prevent usage of context
// in case of the need to read strings.
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CountryAdapter(
    private val context: Context,
    @Suppress("UnusedPrivateMember")
    private val localizedContext: Context
) : BaseAdapter(), Filterable {

    private val countries: MutableList<CountryModel> = mutableListOf()
    private val countryFilter: CountryFilter = CountryFilter(countries)

    fun setItems(countries: List<CountryModel>) {
        this.countries.clear()
        this.countries.addAll(countries)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: CountryViewHolder
        val binding: CountryViewBinding
        if (convertView == null) {
            binding = CountryViewBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = CountryViewHolder(binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as CountryViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getCount() = countries.size

    override fun getItem(position: Int): CountryModel = countries[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter {
        return countryFilter
    }
}

private class CountryFilter(private val countries: List<CountryModel>) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        return FilterResults().apply {
            values = countries
            count = countries.size
        }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        // do nothing
    }

    override fun convertResultToString(resultValue: Any?): CharSequence {
        val country = resultValue as? CountryModel
        return country?.toShortString() ?: ""
    }
}
