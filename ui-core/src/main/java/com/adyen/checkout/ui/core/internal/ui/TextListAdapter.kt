/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/3/2022.
 */

package com.adyen.checkout.ui.core.internal.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.ui.core.databinding.TextItemViewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class TextListAdapter<T : TextListItem>(private val context: Context) : BaseAdapter(), Filterable {

    private val itemList: MutableList<T> = mutableListOf()
    private val textListFilter = TextListFilter(itemList)

    fun setItems(itemList: List<T>) {
        this.itemList.clear()
        this.itemList.addAll(itemList)
        notifyDataSetChanged()
    }

    fun getItem(predicate: ((T) -> Boolean)): T? {
        return itemList.firstOrNull { predicate.invoke(it) }
    }

    override fun getCount() = itemList.size

    override fun getItem(position: Int) = itemList[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: TextViewHolder
        val binding: TextItemViewBinding
        if (convertView == null) {
            binding = TextItemViewBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = TextViewHolder(binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as TextViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getFilter(): Filter {
        return textListFilter
    }
}

internal class TextListFilter(
    private val itemList: List<TextListItem>
) : Filter() {

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        return FilterResults().apply {
            values = itemList
            count = itemList.size
        }
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        // do nothing
    }

    override fun convertResultToString(resultValue: Any?): CharSequence {
        return (resultValue as? TextListItem)?.text.orEmpty()
    }
}

internal class TextViewHolder(
    private val binding: TextItemViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(item: TextListItem) {
        binding.textViewTitle.text = item.text
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class TextListItem(val text: String)
