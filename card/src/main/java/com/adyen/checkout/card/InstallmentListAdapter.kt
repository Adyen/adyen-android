/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 26/10/2021.
 */

package com.adyen.checkout.card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.util.InstallmentUtils

// We need context to inflate the views and localizedContext to fetch the strings
class InstallmentListAdapter(
    private val context: Context,
    private val localizedContext: Context
) : BaseAdapter(), Filterable {

    private val installmentOptions: MutableList<InstallmentModel> = mutableListOf()
    private val installmentFilter = InstallmentFilter(localizedContext, installmentOptions)

    fun setItems(installmentOptions: List<InstallmentModel>) {
        this.installmentOptions.clear()
        this.installmentOptions.addAll(installmentOptions)
        notifyDataSetChanged()
    }

    override fun getCount() = installmentOptions.size

    override fun getItem(position: Int) = installmentOptions[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: InstallmentViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.installment_view, parent, false)
            viewHolder = InstallmentViewHolder(view, localizedContext)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as InstallmentViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getFilter(): Filter {
        return installmentFilter
    }
}

data class InstallmentModel(
    @StringRes val textResId: Int,
    val value: Int?,
    val option: InstallmentOption
)

class InstallmentFilter(
    private val context: Context,
    private val installmentOptions: List<InstallmentModel>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        return FilterResults().apply {
            values = installmentOptions
            count = installmentOptions.size
        }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        // do nothing
    }

    override fun convertResultToString(resultValue: Any?): CharSequence {
        return (resultValue as? InstallmentModel)?.let {
            InstallmentUtils.getTextForInstallmentOption(context, it)
        }.orEmpty()
    }
}

class InstallmentViewHolder(
    rootView: View,
    private val localizedContext: Context
) : RecyclerView.ViewHolder(rootView) {

    private val installmentTextView: TextView = rootView.findViewById(R.id.textView_installmentOption)

    fun bindItem(installmentModel: InstallmentModel) {
        installmentTextView.text = InstallmentUtils.getTextForInstallmentOption(localizedContext, installmentModel)
    }
}
