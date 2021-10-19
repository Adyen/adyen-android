package com.adyen.checkout.card

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class InstallmentListAdapter(private val context: Context): BaseAdapter() {

    private val installmentOptions: MutableList<InstallmentModel> = mutableListOf()

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
            viewHolder = InstallmentViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as InstallmentViewHolder
        }
        viewHolder.bindItem(getItem(position))
        return view
    }

}

data class InstallmentModel(
    val text: String,
    val value: Int?,
    val option: InstallmentOption
)