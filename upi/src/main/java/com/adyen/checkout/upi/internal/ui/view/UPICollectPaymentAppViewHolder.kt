/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 25/4/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.upi.databinding.UpiAppBinding
import com.adyen.checkout.upi.internal.ui.model.UPICollectItem

internal class UPICollectPaymentAppViewHolder(
    private val binding: UpiAppBinding,
    private val paymentMethod: String,
) : UPICollectItemViewHolder(binding) {

    override fun bind(item: UPICollectItem, isChecked: Boolean) {
        val app = (item as? UPICollectItem.PaymentApp) ?: run {
            adyenLog(AdyenLogLevel.DEBUG) { "Item type is not recognized, thus the item can not be bound" }
            return
        }

        bindItem(
            paymentMethod = paymentMethod,
            paymentApp = app,
            isChecked = isChecked,
        )
    }

    private fun bindItem(
        paymentMethod: String,
        paymentApp: UPICollectItem.PaymentApp,
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

    override fun setOnClickListener(onClickListener: (Int) -> Unit) {
        itemView.setOnClickListener {
            val position = getBindingAdapterPosition()
            onClickListener.invoke(position)
        }
    }
}
