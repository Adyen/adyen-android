/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 22/3/2022.
 */

package com.adyen.checkout.components.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.ui.R

class SimpleTextListAdapter<T : SimpleTextListItem>(private val context: Context) : BaseAdapter(), Filterable {

    private val itemList: MutableList<T> = mutableListOf()
    private val simpleTextListFilter = SimpleTextListFilter(itemList)

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
        val viewHolder: SimpleTextViewHolder
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.simple_text_item_view, parent, false)
            viewHolder = SimpleTextViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as SimpleTextViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

    override fun getFilter(): Filter {
        return simpleTextListFilter
    }
}

class SimpleTextListFilter(
    private val itemList: List<SimpleTextListItem>
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
        return (resultValue as? SimpleTextListItem)?.text.orEmpty()
    }
}

class SimpleTextViewHolder(private val rootView: View) : RecyclerView.ViewHolder(rootView) {
    private val textView: TextView = rootView.findViewById(R.id.textView_text)

    fun bindItem(item: SimpleTextListItem) {
        textView.text = item.text
    }
}

open class SimpleTextListItem(val text: String)
