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
import com.adyen.checkout.upi.internal.ui.model.UPICollectItem
import com.adyen.checkout.upi.internal.ui.view.UPIAppsAdapter.UPIViewType.VIEW_TYPE_GENERIC_APP
import com.adyen.checkout.upi.internal.ui.view.UPIAppsAdapter.UPIViewType.VIEW_TYPE_MANUAL_INPUT
import com.adyen.checkout.upi.internal.ui.view.UPIAppsAdapter.UPIViewType.VIEW_TYPE_PAYMENT_APP

// TODO: Check this adapter for LTR languages, like Arabic
internal class UPIAppsAdapter(
    private val context: Context,
    private val localizedContext: Context,
    private val paymentMethod: String,
    private val onCheckedListener: (UPICollectItem) -> Unit,
    private val onInputChangeListener: (String) -> Unit,
) : ListAdapter<UPICollectItem, UPICollectItemViewHolder>(UPIAppsDiffCallback) {

    private var lastCheckedPosition: Int = 0

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is UPICollectItem.PaymentApp -> VIEW_TYPE_PAYMENT_APP.id
        is UPICollectItem.GenericApp -> VIEW_TYPE_GENERIC_APP.id
        is UPICollectItem.ManualInput -> VIEW_TYPE_MANUAL_INPUT.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UPICollectItemViewHolder {
        val upiViewType = UPIViewType.entries[viewType]
        return when (upiViewType) {
            VIEW_TYPE_PAYMENT_APP -> {
                val binding = UpiAppBinding.inflate(LayoutInflater.from(context), parent, false)
                UPICollectPaymentAppViewHolder(binding, paymentMethod)
            }

            VIEW_TYPE_GENERIC_APP -> {
                val binding = UpiAppGenericBinding.inflate(LayoutInflater.from(context), parent, false)
                UPICollectGenericAppViewHolder(binding)
            }

            VIEW_TYPE_MANUAL_INPUT -> {
                val binding = UpiAppManualAddressBinding.inflate(LayoutInflater.from(context), parent, false)
                UPICollectManualAddressViewHolder(
                    binding,
                    localizedContext,
                    onInputChangeListener,
                )
            }
        }
    }

    override fun onBindViewHolder(holder: UPICollectItemViewHolder, position: Int) = with(holder) {
        bind(
            item = getItem(position),
            isChecked = position == lastCheckedPosition,
        )
        setOnClickListener(::onLastCheckedPositionChanged)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun onLastCheckedPositionChanged(newPosition: Int) {
        if (lastCheckedPosition != newPosition) {
            lastCheckedPosition = newPosition
        }
        notifyDataSetChanged()

        onCheckedListener.invoke(getItem(lastCheckedPosition))
    }

    object UPIAppsDiffCallback : DiffUtil.ItemCallback<UPICollectItem>() {
        override fun areItemsTheSame(oldItem: UPICollectItem, newItem: UPICollectItem): Boolean =
            oldItem.areItemsTheSame(newItem)

        override fun areContentsTheSame(oldItem: UPICollectItem, newItem: UPICollectItem): Boolean =
            oldItem.areContentsTheSame(newItem)

        override fun getChangePayload(oldItem: UPICollectItem, newItem: UPICollectItem) =
            oldItem.getChangePayload(newItem)
    }

    enum class UPIViewType(val id: Int) {
        VIEW_TYPE_PAYMENT_APP(0),
        VIEW_TYPE_GENERIC_APP(1),
        VIEW_TYPE_MANUAL_INPUT(2),
    }
}
