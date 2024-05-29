/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.adyen.checkout.upi.databinding.UpiAppBinding
import com.adyen.checkout.upi.databinding.UpiAppGenericBinding
import com.adyen.checkout.upi.databinding.UpiAppManualAddressBinding
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem
import com.adyen.checkout.upi.internal.ui.view.UPIAppsAdapter.UPIViewType.VIEW_TYPE_GENERIC_APP
import com.adyen.checkout.upi.internal.ui.view.UPIAppsAdapter.UPIViewType.VIEW_TYPE_MANUAL_INPUT
import com.adyen.checkout.upi.internal.ui.view.UPIAppsAdapter.UPIViewType.VIEW_TYPE_PAYMENT_APP

internal class UPIAppsAdapter(
    private val context: Context,
    private val localizedContext: Context,
    private val paymentMethod: String,
    private val onItemClickListener: (UPIIntentItem) -> Unit,
    private val onInputChangeListener: (String) -> Unit,
) : ListAdapter<UPIIntentItem, UPIIntentItemViewHolder>(UPIAppsDiffCallback) {

    private var selectedItem: UPIIntentItem? = null

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is UPIIntentItem.PaymentApp -> VIEW_TYPE_PAYMENT_APP.id
        is UPIIntentItem.GenericApp -> VIEW_TYPE_GENERIC_APP.id
        is UPIIntentItem.ManualInput -> VIEW_TYPE_MANUAL_INPUT.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UPIIntentItemViewHolder {
        val upiViewType = UPIViewType.entries[viewType]
        return when (upiViewType) {
            VIEW_TYPE_PAYMENT_APP -> {
                val binding = UpiAppBinding.inflate(LayoutInflater.from(context), parent, false)
                UPIIntentPaymentAppViewHolder(binding, paymentMethod)
            }

            VIEW_TYPE_GENERIC_APP -> {
                val binding = UpiAppGenericBinding.inflate(LayoutInflater.from(context), parent, false)
                UPIIntentGenericAppViewHolder(binding)
            }

            VIEW_TYPE_MANUAL_INPUT -> {
                val binding = UpiAppManualAddressBinding.inflate(LayoutInflater.from(context), parent, false)
                UPIIntentManualAddressViewHolder(
                    binding,
                    localizedContext,
                    onInputChangeListener,
                )
            }
        }
    }

    override fun onBindViewHolder(holder: UPIIntentItemViewHolder, position: Int) = with(holder) {
        val item = getItem(position)
        bind(
            item = item,
            isChecked = item == selectedItem,
        )
        setOnClickListener(::onItemClicked)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectedItem(item: UPIIntentItem?) {
        selectedItem = item
        notifyDataSetChanged()
    }

    private fun onItemClicked(itemPosition: Int) {
        onItemClickListener.invoke(getItem(itemPosition))
    }

    object UPIAppsDiffCallback : DiffUtil.ItemCallback<UPIIntentItem>() {
        override fun areItemsTheSame(oldItem: UPIIntentItem, newItem: UPIIntentItem): Boolean =
            oldItem.areItemsTheSame(newItem)

        override fun areContentsTheSame(oldItem: UPIIntentItem, newItem: UPIIntentItem): Boolean =
            oldItem.areContentsTheSame(newItem)

        override fun getChangePayload(oldItem: UPIIntentItem, newItem: UPIIntentItem) =
            oldItem.getChangePayload(newItem)
    }

    enum class UPIViewType(val id: Int) {
        VIEW_TYPE_PAYMENT_APP(0),
        VIEW_TYPE_GENERIC_APP(1),
        VIEW_TYPE_MANUAL_INPUT(2),
    }
}
