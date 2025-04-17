/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/4/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.databinding.CardBrandItemBinding
import com.adyen.checkout.card.internal.ui.model.CardBrandItem

internal class CardBrandAdapter : ListAdapter<CardBrandItem, CardBrandItemViewHolder>(CardBrandItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardBrandItemViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: CardBrandItemViewHolder, position: Int) {
        TODO("Not yet implemented")
    }
}

internal class CardBrandItemViewHolder(binding: CardBrandItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind() {
        TODO("Not yet implemented")
    }
}

internal object CardBrandItemDiffCallback : DiffUtil.ItemCallback<CardBrandItem>() {
    override fun areItemsTheSame(oldItem: CardBrandItem, newItem: CardBrandItem): Boolean {
        TODO("Not yet implemented")
    }

    override fun areContentsTheSame(oldItem: CardBrandItem, newItem: CardBrandItem): Boolean {
        TODO("Not yet implemented")
    }
}
