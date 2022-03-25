/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */
package com.adyen.checkout.card

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.CardListAdapter.ImageViewHolder
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.RoundCornerImageView

internal class CardListAdapter(
    private val imageLoader: ImageLoader,
) : ListAdapter<CardType, ImageViewHolder>(CardDiffCallback) {

    var filteredCards: List<CardType> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context)
            .inflate(R.layout.brand_logo, parent, false) as RoundCornerImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val card = currentList[position]
        val alpha = if (filteredCards.isEmpty() || filteredCards.contains(card)) ACTIVE else NOT_ACTIVE
        holder.bind(card, alpha, imageLoader)
    }

    internal class ImageViewHolder(
        private val imageView: RoundCornerImageView
    ) : RecyclerView.ViewHolder(imageView) {

        fun bind(card: CardType, alpha: Float, imageLoader: ImageLoader) {
            imageView.alpha = alpha
            imageLoader.load(card.txVariant, imageView)
        }
    }

    companion object {
        private const val ACTIVE = 1f
        private const val NOT_ACTIVE = 0.2f
    }

    object CardDiffCallback : DiffUtil.ItemCallback<CardType>() {
        override fun areItemsTheSame(oldItem: CardType, newItem: CardType): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: CardType, newItem: CardType): Boolean =
            areItemsTheSame(oldItem, newItem)
    }
}
