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
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.CardListAdapter.ImageViewHolder
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.view.RoundCornerImageView

class CardListAdapter(private val mImageLoader: ImageLoader, private val mSupportedCards: List<CardType>) : RecyclerView.Adapter<ImageViewHolder>() {
    private var filteredCards: List<CardType> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ImageViewHolder {
        val imageView = LayoutInflater.from(parent.context).inflate(R.layout.brand_logo, parent, false) as RoundCornerImageView
        return ImageViewHolder(imageView)
    }

    override fun onBindViewHolder(imageViewHolder: ImageViewHolder, i: Int) {
        val card = mSupportedCards[i]
        imageViewHolder.mCardLogo.alpha =
            if (filteredCards.isEmpty() || filteredCards.contains(card)) ACTIVE else NOT_ACTIVE
        mImageLoader.load(card.txVariant, imageViewHolder.mCardLogo)
    }

    override fun getItemCount(): Int {
        return mSupportedCards.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredCard(filteredCards: List<CardType>) {
        this.filteredCards = filteredCards
        notifyDataSetChanged()
        // TODO refactor this to ListAdapter
    }

    inner class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var mCardLogo: RoundCornerImageView = view as RoundCornerImageView
    }

    companion object {
        private const val ACTIVE = 1f
        private const val NOT_ACTIVE = 0.2f
    }
}
