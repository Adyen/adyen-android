/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 11/2/2025.
 */

package com.adyen.checkout.payto.internal.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.payto.databinding.PayIdTypeViewBinding
import com.adyen.checkout.payto.internal.ui.model.PayIdTypeModel

internal class PayIdTypeAdapter(
    private val context: Context,
    private val localizedContext: Context
) : BaseAdapter(), Filterable {

    private val payIdTypes: MutableList<PayIdTypeModel> = mutableListOf()
    private val payIdTypeFilter: PayIdTypeFilter = PayIdTypeFilter(localizedContext, payIdTypes)

    fun setItems(payIdTypes: List<PayIdTypeModel>) {
        this.payIdTypes.clear()
        this.payIdTypes.addAll(payIdTypes)
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: PayIdTypeViewHolder
        val binding: PayIdTypeViewBinding
        if (convertView == null) {
            binding = PayIdTypeViewBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = PayIdTypeViewHolder(binding, localizedContext)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as PayIdTypeViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getCount() = payIdTypes.size

    override fun getItem(position: Int): PayIdTypeModel = payIdTypes[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter {
        return payIdTypeFilter
    }

    internal class PayIdTypeViewHolder(
        private val binding: PayIdTypeViewBinding,
        private val localizedContext: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(payIdType: PayIdTypeModel) {
            with(binding) {
                textViewTitle.text = localizedContext.getString(payIdType.nameResId)
            }
        }
    }
}

private class PayIdTypeFilter(
    private val localizedContext: Context,
    private val payIdTypes: List<PayIdTypeModel>
) : Filter() {
    override fun performFiltering(constraint: CharSequence?): FilterResults {
        return FilterResults().apply {
            values = payIdTypes
            count = payIdTypes.size
        }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        // do nothing
    }

    override fun convertResultToString(resultValue: Any?): CharSequence {
        val payIdType = resultValue as? PayIdTypeModel

        return payIdType?.let {
            localizedContext.getString(it.nameResId)
        }.orEmpty()
    }
}
