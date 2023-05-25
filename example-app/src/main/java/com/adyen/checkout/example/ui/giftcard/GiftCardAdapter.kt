/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/5/2023.
 */

package com.adyen.checkout.example.ui.giftcard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.example.databinding.ItemGiftCardBinding
import com.adyen.checkout.example.extensions.formatAmount
import java.util.Locale

class GiftCardAdapter : ListAdapter<GiftCardModel, GiftCardAdapter.GiftCardViewHolder>(GiftCardDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftCardViewHolder {
        val binding = ItemGiftCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GiftCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GiftCardViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    class GiftCardViewHolder(private val binding: ItemGiftCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(giftCardModel: GiftCardModel) {
            binding.textViewAmount.text = giftCardModel.amount.formatAmount(Locale.ENGLISH)
        }
    }

    object GiftCardDiffCallback : DiffUtil.ItemCallback<GiftCardModel>() {
        override fun areItemsTheSame(oldItem: GiftCardModel, newItem: GiftCardModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: GiftCardModel, newItem: GiftCardModel): Boolean =
            oldItem == newItem
    }
}

data class GiftCardModel(
    val id: String,
    val amount: Amount
)
