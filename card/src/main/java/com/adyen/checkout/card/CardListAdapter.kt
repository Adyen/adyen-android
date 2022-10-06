/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */
package com.adyen.checkout.card

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.CardListAdapter.ImageViewHolder
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.databinding.BrandLogoBinding
import com.adyen.checkout.components.api.ImageLoader

internal class CardListAdapter(
    private val imageLoader: ImageLoader,
) : ListAdapter<CardListItem, ImageViewHolder>(CardDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = BrandLogoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val card = currentList[position]
        val alpha = if (card.isDetected) ACTIVE else NOT_ACTIVE
        holder.bind(card, alpha, imageLoader)
    }

    internal class ImageViewHolder(
        private val binding: BrandLogoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(card: CardListItem, alpha: Float, imageLoader: ImageLoader) {
            binding.imageViewBrandLogo.alpha = alpha
            imageLoader.load(card.cardType.txVariant, binding.imageViewBrandLogo)
        }
    }

    companion object {
        private const val ACTIVE = 1f
        private const val NOT_ACTIVE = 0.2f
    }

    object CardDiffCallback : DiffUtil.ItemCallback<CardListItem>() {
        override fun areItemsTheSame(oldItem: CardListItem, newItem: CardListItem): Boolean =
            oldItem.cardType.txVariant == newItem.cardType.txVariant

        override fun areContentsTheSame(oldItem: CardListItem, newItem: CardListItem): Boolean =
            oldItem == newItem
    }
}

internal data class CardListItem(val cardType: CardType, val isDetected: Boolean)
