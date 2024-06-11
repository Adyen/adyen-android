/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.upi.databinding.UpiAppGenericBinding
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem

internal class UPIIntentGenericAppViewHolder(
    private val binding: UpiAppGenericBinding,
) : UPIIntentItemViewHolder(binding) {

    override fun bind(item: UPIIntentItem) {
        (item as? UPIIntentItem.GenericApp) ?: run {
            adyenLog(AdyenLogLevel.DEBUG) { "Item type is not recognized, thus the item can not be bound" }
            return
        }

        bindItem(item.isSelected)
    }

    private fun bindItem(isChecked: Boolean) = with(binding) {
        radioButtonUpiApp.isChecked = isChecked
    }

    override fun setOnClickListener(onClickListener: (Int) -> Unit) {
        itemView.setOnClickListener {
            val position = getBindingAdapterPosition()
            onClickListener.invoke(position)
        }
    }
}
