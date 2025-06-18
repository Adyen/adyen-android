/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.upi.databinding.UpiAppBinding
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem

internal class UPIIntentPaymentAppViewHolder(
    private val binding: UpiAppBinding,
    private val paymentMethod: String,
) : UPIIntentItemViewHolder(binding) {

    override fun bind(item: UPIIntentItem, onClickListener: (UPIIntentItem) -> Unit) {
        val app = (item as? UPIIntentItem.PaymentApp) ?: run {
            adyenLog(AdyenLogLevel.DEBUG) { "Item type is not recognized, thus the item can not be bound" }
            return
        }

        itemView.setOnClickListener {
            onClickListener.invoke(item)
        }

        bindItem(
            paymentMethod = paymentMethod,
            paymentApp = app,
            isChecked = item.isSelected,
        )
    }

    private fun bindItem(
        paymentMethod: String,
        paymentApp: UPIIntentItem.PaymentApp,
        isChecked: Boolean,
    ) = with(binding) {
        radioButtonUpiApp.isChecked = isChecked
        textViewUpiAppName.text = paymentApp.name
        imageViewUpiLogo.loadLogo(
            environment = paymentApp.environment,
            txVariant = paymentMethod,
            txSubVariant = paymentApp.id,
        )
    }
}
