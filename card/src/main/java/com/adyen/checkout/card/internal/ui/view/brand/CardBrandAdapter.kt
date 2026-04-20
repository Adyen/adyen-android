/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view.brand

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.R
import com.adyen.checkout.card.databinding.BrandItemBinding
import com.adyen.checkout.card.internal.ui.model.CardBrandItem
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.ui.core.internal.ui.view.RoundCornerImageView

internal class CardBrandAdapter(
    val isSelectable: Boolean,
    private val onItemClicked: (CardBrandItem) -> Unit,
) : ListAdapter<CardBrandItem, CardBrandItemViewHolder>(CardBrandItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardBrandItemViewHolder {
        val binding = BrandItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CardBrandItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardBrandItemViewHolder, position: Int) {
        val item = currentList[position]
        if (isSelectable) {
            holder.bindSelectable(item, onItemClicked)
        } else {
            holder.bind(item)
        }
    }
}

internal class CardBrandItemViewHolder(
    private val binding: BrandItemBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(cardBrandItem: CardBrandItem) {
        binding.imageViewBrandItem.strokeWidth = RoundCornerImageView.DEFAULT_STROKE_WIDTH
        binding.imageViewBrandItem.loadLogo(
            cardBrandItem.environment,
            cardBrandItem.brand.txVariant,
        )
    }

    fun bindSelectable(cardBrandItem: CardBrandItem, onItemClicked: (CardBrandItem) -> Unit) {
        binding.imageViewBrandItem.loadLogo(
            cardBrandItem.environment,
            cardBrandItem.brand.txVariant,
        )
        binding.root.setOnClickListener { onItemClicked(cardBrandItem) }
        binding.brandItemContainer.setBackgroundResource(R.drawable.bg_brand_item_selector)
        binding.brandItemContainer.isSelected = cardBrandItem.isSelected
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
