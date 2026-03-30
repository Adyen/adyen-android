/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.adyen.checkout.upi.databinding.UpiAppBinding
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem

internal class UPIAppsAdapter(
    private val context: Context,
    private val paymentMethod: String,
    private val onItemClickListener: (UPIIntentItem) -> Unit,
) : ListAdapter<UPIIntentItem, UPIIntentItemViewHolder>(UPIAppsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UPIIntentItemViewHolder {
        val binding = UpiAppBinding.inflate(LayoutInflater.from(context), parent, false)
        return UPIIntentPaymentAppViewHolder(binding, paymentMethod)
    }

    override fun onBindViewHolder(holder: UPIIntentItemViewHolder, position: Int) = with(holder) {
        bind(getItem(position), onItemClickListener)
    }

    object UPIAppsDiffCallback : DiffUtil.ItemCallback<UPIIntentItem>() {
        override fun areItemsTheSame(oldItem: UPIIntentItem, newItem: UPIIntentItem): Boolean =
            oldItem.areItemsTheSame(newItem)

        override fun areContentsTheSame(oldItem: UPIIntentItem, newItem: UPIIntentItem): Boolean =
            oldItem.areContentsTheSame(newItem)

        override fun getChangePayload(oldItem: UPIIntentItem, newItem: UPIIntentItem) =
            oldItem.getChangePayload(newItem)
    }
}
