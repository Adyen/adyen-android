/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/10/2022.
 */

package com.adyen.checkout.paybybank

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.databinding.RecyclerListWithImageBinding
import com.adyen.checkout.issuerlist.IssuerModel
import com.adyen.checkout.issuerlist.R

internal class PayByBankRecyclerAdapter(
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val onItemClicked: (IssuerModel) -> Unit,
) : ListAdapter<IssuerModel, PayByBankRecyclerAdapter.PayByBankViewHolder>(IssuerDiffCallBack) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PayByBankViewHolder {
        val binding = RecyclerListWithImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return PayByBankViewHolder(binding)
    }

    override fun onBindViewHolder(viewHolder: PayByBankViewHolder, position: Int) {
        viewHolder.bind(paymentMethod, currentList[position], imageLoader, onItemClicked)
    }

    class PayByBankViewHolder(
        private val binding: RecyclerListWithImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            paymentMethod: String,
            issuerModel: IssuerModel,
            imageLoader: ImageLoader,
            onItemClicked: (IssuerModel) -> Unit,
        ) {
            binding.root.setOnClickListener { onItemClicked(issuerModel) }

            binding.textViewTitle.text = issuerModel.name
            imageLoader.load(
                paymentMethod,
                issuerModel.id,
                binding.imageViewLogo,
                R.drawable.ic_placeholder_image,
                R.drawable.ic_placeholder_image
            )
        }
    }

    object IssuerDiffCallBack : DiffUtil.ItemCallback<IssuerModel>() {
        override fun areItemsTheSame(oldItem: IssuerModel, newItem: IssuerModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: IssuerModel, newItem: IssuerModel): Boolean =
            oldItem == newItem
    }
}
