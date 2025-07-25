/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.ui.core.databinding.AddressLookupOptionItemViewBinding

internal class AddressLookupOptionsAdapter(
    private val onItemClicked: (LookupAddress) -> Unit
) :
    ListAdapter<LookupOption, AddressLookupOptionsAdapter.AddressLookupOptionViewHolder>(
        AddressLookupOptionDiffCallback,
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
        private val onItemClicked: (LookupAddress) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(lookupOption: LookupOption) {
            binding.root.setOnClickListener {
                onItemClicked(lookupOption.lookupAddress)
            }
            binding.progressBar.isVisible = lookupOption.isLoading
            binding.textViewAddressHeader.text = lookupOption.title
            binding.textViewAddressDescription.text = lookupOption.subtitle
        }
    }

    object AddressLookupOptionDiffCallback : DiffUtil.ItemCallback<LookupOption>() {
        override fun areItemsTheSame(oldItem: LookupOption, newItem: LookupOption): Boolean =
            oldItem.lookupAddress.id == newItem.lookupAddress.id

        override fun areContentsTheSame(oldItem: LookupOption, newItem: LookupOption): Boolean =
            oldItem == newItem
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class LookupOption(
    val lookupAddress: LookupAddress,
    val isLoading: Boolean = false
) {

    val title: String = formatTitle()

    private fun formatTitle(): String = with(lookupAddress.address) {
        arrayOf(
            street,
            houseNumberOrName,
            apartmentSuite.orEmpty(),
        )
            .filter { it.isNotBlank() }
            .joinToString(" ")
    }

    val subtitle = formatSubtitle()

    private fun formatSubtitle() = with(lookupAddress.address) {
        arrayOf(
            postalCode,
            city,
            stateOrProvince,
            country,
        )
            .filter { it.isNotBlank() }
            .joinToString(", ")
    }
}
