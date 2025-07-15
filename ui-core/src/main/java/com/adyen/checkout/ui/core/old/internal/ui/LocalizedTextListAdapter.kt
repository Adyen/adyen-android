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
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.ui.core.databinding.TextItemViewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class LocalizedTextListAdapter<T : LocalizedTextListItem>(
    private val context: Context,
    private val localizedContext: Context,
) : BaseAdapter(), Filterable {

    private val itemList: MutableList<T> = mutableListOf()
    private val textListFilter = LocalizedTextListFilter(localizedContext, itemList)

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
        val viewHolder: LocalizedTextViewHolder
        val binding: TextItemViewBinding
        if (convertView == null) {
            binding = TextItemViewBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = LocalizedTextViewHolder(localizedContext, binding)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as LocalizedTextViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getFilter(): Filter {
        return textListFilter
    }
}

internal class LocalizedTextListFilter(
    private val localizedContext: Context,
    private val itemList: List<LocalizedTextListItem>,
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
        val textResId = (resultValue as? LocalizedTextListItem)?.textResId
        return textResId?.let {
            localizedContext.getString(it)
        } ?: ""
    }
}

internal class LocalizedTextViewHolder(
    private val localizedContext: Context,
    private val binding: TextItemViewBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bindItem(item: LocalizedTextListItem) {
        binding.textViewTitle.text = localizedContext.getString(item.textResId)
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
open class LocalizedTextListItem(@StringRes val textResId: Int)
