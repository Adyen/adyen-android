/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.old.internal.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.databinding.InstallmentViewBinding
import com.adyen.checkout.card.old.internal.ui.model.InstallmentOption
import com.adyen.checkout.card.old.internal.util.InstallmentUtils
import com.adyen.checkout.components.core.Amount
import java.util.Locale

// We need context to inflate the views and localizedContext to fetch the strings
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
internal class InstallmentListAdapter(
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
        val binding: InstallmentViewBinding
        if (convertView == null) {
            binding = InstallmentViewBinding.inflate(LayoutInflater.from(context), parent, false)
            viewHolder = InstallmentViewHolder(binding, localizedContext)
            view = binding.root
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

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class InstallmentModel(
    val numberOfInstallments: Int?,
    val option: InstallmentOption,
    val amount: Amount?,
    val shopperLocale: Locale,
    val showAmount: Boolean
)

internal class InstallmentFilter(
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

internal class InstallmentViewHolder(
    private val binding: InstallmentViewBinding,
    private val localizedContext: Context
) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(installmentModel: InstallmentModel) {
        binding.textViewInstallmentOption.text =
            InstallmentUtils.getTextForInstallmentOption(localizedContext, installmentModel)
    }
}
