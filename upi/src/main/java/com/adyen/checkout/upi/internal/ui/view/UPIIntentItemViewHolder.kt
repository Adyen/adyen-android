/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem

internal abstract class UPIIntentItemViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {

    abstract fun bind(item: UPIIntentItem, onClickListener: (UPIIntentItem) -> Unit)
}
