/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/4/2025.
 */

package com.adyen.checkout.card.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.databinding.CardBrandItemBinding
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.ui.core.internal.ui.loadLogo

internal class CardBrandAdapter(
    private val onItemClicked: (CardBrandItem) -> Unit,
) : ListAdapter<CardBrandItem, CardBrandItemViewHolder>(CardBrandItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardBrandItemViewHolder {
        val binding = CardBrandItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardBrandItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardBrandItemViewHolder, position: Int) {
        val cardBrandItem = currentList[position]
        holder.bind(cardBrandItem, onItemClicked)
    }
}

internal class CardBrandItemViewHolder(
    private val binding: CardBrandItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(cardBrandItem: CardBrandItem, onItemClicked: (CardBrandItem) -> Unit) {
        binding.root.setOnClickListener {
            onItemClicked(cardBrandItem)
        }
        binding.imageViewCardBrandLogo.loadLogo(
            cardBrandItem.environment,
            cardBrandItem.brand.txVariant,
        )
        binding.textViewCardBrandName.text = cardBrandItem.name
        binding.radioButtonCardBrand.isChecked = cardBrandItem.isSelected
    }
}

internal object CardBrandItemDiffCallback : DiffUtil.ItemCallback<CardBrandItem>() {
    override fun areItemsTheSame(oldItem: CardBrandItem, newItem: CardBrandItem): Boolean {
        return oldItem.brand.txVariant == newItem.brand.txVariant
    }

    override fun areContentsTheSame(oldItem: CardBrandItem, newItem: CardBrandItem): Boolean {
        return oldItem == newItem
    }
}
