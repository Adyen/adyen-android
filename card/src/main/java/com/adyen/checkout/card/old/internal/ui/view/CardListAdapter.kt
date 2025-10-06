/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */
package com.adyen.checkout.card.old.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.databinding.BrandLogoBinding
import com.adyen.checkout.card.old.internal.ui.model.CardListItem
import com.adyen.checkout.ui.core.old.internal.ui.loadLogo

internal class CardListAdapter : ListAdapter<CardListItem, CardListAdapter.ImageViewHolder>(CardDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = BrandLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val card = currentList[position]
        val alpha = if (card.isDetected) VIEW_ALPHA_DETECTED else VIEW_ALPHA_NON_DETECTED
        holder.bind(card, alpha)
    }

    internal class ImageViewHolder(
        private val binding: BrandLogoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: CardListItem, alpha: Float) {
            binding.imageViewBrandLogo.alpha = alpha
            binding.imageViewBrandLogo.loadLogo(
                environment = card.environment,
                txVariant = card.cardBrand.txVariant,
            )
        }
    }

    companion object {
        private const val VIEW_ALPHA_DETECTED = 1f
        private const val VIEW_ALPHA_NON_DETECTED = 0.2f
    }

    object CardDiffCallback : DiffUtil.ItemCallback<CardListItem>() {
        override fun areItemsTheSame(oldItem: CardListItem, newItem: CardListItem): Boolean =
            oldItem.cardBrand.txVariant == newItem.cardBrand.txVariant

        override fun areContentsTheSame(oldItem: CardListItem, newItem: CardListItem): Boolean =
            oldItem == newItem
    }
}
