/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 19/12/2023.
 */

package com.adyen.checkout.ui.core.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.internal.util.CountryUtils
import com.adyen.checkout.ui.core.databinding.AddressLookupOptionItemViewBinding
import java.util.Locale

internal class AddressLookupOptionsAdapter(
    private val shopperLocale: Locale,
    private val onItemClicked: (LookupAddress) -> Unit,
) :
    ListAdapter<LookupOption, AddressLookupOptionsAdapter.AddressLookupOptionViewHolder>(
        AddressLookupOptionDiffCallback,
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressLookupOptionViewHolder {
        val binding = AddressLookupOptionItemViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AddressLookupOptionViewHolder(binding, onItemClicked, shopperLocale)
    }

    override fun onBindViewHolder(holder: AddressLookupOptionViewHolder, position: Int) {
        holder.bindItem(currentList[position])
    }

    internal class AddressLookupOptionViewHolder(
        private val binding: AddressLookupOptionItemViewBinding,
        private val onItemClicked: (LookupAddress) -> Unit,
        private val shopperLocale: Locale,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindItem(lookupOption: LookupOption) {
            binding.root.setOnClickListener {
                onItemClicked(lookupOption.lookupAddress)
            }
            binding.progressBar.isVisible = lookupOption.isLoading
            binding.textViewAddressHeader.text = lookupOption.getDisplayTitle(shopperLocale)
            binding.textViewAddressDescription.text = lookupOption.getDisplaySubtitle(shopperLocale)
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
    private fun getDisplayName(locale: Locale): String {
        return listOf(
            lookupAddress.address.street,
            lookupAddress.address.houseNumberOrName,
            lookupAddress.address.apartmentSuite,
            lookupAddress.address.postalCode,
            lookupAddress.address.city,
            lookupAddress.address.stateOrProvince,
            CountryUtils.getCountryName(lookupAddress.address.country, locale),
        ).filter { !it.isNullOrBlank() }.joinToString(" ")
    }

    fun getDisplayTitle(locale: Locale) = lookupAddress.address.street.ifBlank {
        getDisplayName(locale)
    }

    fun getDisplaySubtitle(locale: Locale) = if (lookupAddress.address.street.isBlank()) {
        ""
    } else {
        getDisplayName(locale)
    }
}
