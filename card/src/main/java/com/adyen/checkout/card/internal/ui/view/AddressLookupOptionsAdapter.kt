/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 29/11/2023.
 */

package com.adyen.checkout.card.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.card.databinding.AddressLookupOptionItemViewBinding
import com.adyen.checkout.card.internal.data.model.LookupAddress

internal class AddressLookupOptionsAdapter(
    private val onItemClicked: (LookupAddress) -> Boolean
) :
    ListAdapter<LookupAddress, AddressLookupOptionsAdapter.AddressLookupOptionViewHolder>(
        AddressLookupOptionDiffCallback
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressLookupOptionViewHolder {
        val binding = AddressLookupOptionItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressLookupOptionViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: AddressLookupOptionViewHolder, position: Int) {
        holder.bindItem(currentList[position])
    }

    internal class AddressLookupOptionViewHolder(
        private val binding: AddressLookupOptionItemViewBinding,
        private val onItemClicked: (LookupAddress) -> Boolean
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(lookupAddress: LookupAddress) {
            binding.root.setOnClickListener {
                onItemClicked(lookupAddress)
            }
            binding.progressBar.isVisible = lookupAddress.isLoading
            binding.textViewAddressHeader.text = lookupAddress.title
            binding.textViewAddressDescription.text = lookupAddress.subtitle
        }
    }

    object AddressLookupOptionDiffCallback : DiffUtil.ItemCallback<LookupAddress>() {
        override fun areItemsTheSame(oldItem: LookupAddress, newItem: LookupAddress): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: LookupAddress, newItem: LookupAddress): Boolean =
            oldItem == newItem
    }
}
