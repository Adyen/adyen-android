/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/12/2023.
 */

package com.adyen.checkout.voucher.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.adyen.checkout.voucher.databinding.FullVoucherInformationFieldBinding
import com.adyen.checkout.voucher.internal.ui.model.VoucherInformationField
import com.adyen.checkout.voucher.internal.ui.view.VoucherInformationFieldsAdapter.InformationFieldViewHolder

internal class VoucherInformationFieldsAdapter :
    ListAdapter<VoucherInformationField, InformationFieldViewHolder>(InformationFieldsDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InformationFieldViewHolder {
        val binding = FullVoucherInformationFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InformationFieldViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InformationFieldViewHolder, position: Int) {
        val informationField = currentList[position]
        holder.bindItem(informationField)
    }

    internal class InformationFieldViewHolder(
        private val binding: FullVoucherInformationFieldBinding
    ) : ViewHolder(binding.root) {
        fun bindItem(model: VoucherInformationField) {
            binding.textViewInformationLabel.text = binding.root.context.getString(model.labelResId)
            binding.textViewInformationValue.text = model.value
        }
    }

    object InformationFieldsDiffCallback : DiffUtil.ItemCallback<VoucherInformationField>() {
        override fun areItemsTheSame(oldItem: VoucherInformationField, newItem: VoucherInformationField): Boolean =
            oldItem == newItem

        override fun areContentsTheSame(oldItem: VoucherInformationField, newItem: VoucherInformationField): Boolean =
            areItemsTheSame(oldItem, newItem)
    }
}
