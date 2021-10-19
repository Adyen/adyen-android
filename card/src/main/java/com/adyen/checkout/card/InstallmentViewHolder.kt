package com.adyen.checkout.card

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InstallmentViewHolder(private val rootView: View): RecyclerView.ViewHolder(rootView) {

    private val installmentTextView: TextView = rootView.findViewById(R.id.textView_installmentOption)

    fun bindItem(installmentModel: InstallmentModel) {
        installmentTextView.text = installmentModel.text
    }

}