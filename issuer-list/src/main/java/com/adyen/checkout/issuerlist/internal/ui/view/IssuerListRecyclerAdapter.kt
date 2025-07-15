/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */
package com.adyen.checkout.issuerlist.internal.ui.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.issuerlist.internal.ui.model.IssuerModel
import com.adyen.checkout.issuerlist.internal.ui.view.IssuerListRecyclerAdapter.IssuerViewHolder
import com.adyen.checkout.ui.core.databinding.RecyclerListWithImageBinding
import com.adyen.checkout.ui.core.old.internal.ui.loadLogo

internal class IssuerListRecyclerAdapter(
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean,
    private val onItemClicked: (IssuerModel) -> Unit,
) : ListAdapter<IssuerModel, IssuerViewHolder>(IssuerDiffCallBack) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IssuerViewHolder {
        val binding = RecyclerListWithImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return IssuerViewHolder(binding, hideIssuerLogo)
    }

    override fun onBindViewHolder(viewHolder: IssuerViewHolder, position: Int) {
        viewHolder.bind(paymentMethod, currentList[position], hideIssuerLogo, onItemClicked)
    }

    class IssuerViewHolder(
        private val binding: RecyclerListWithImageBinding,
        hideIssuerLogo: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageViewLogo.isVisible = !hideIssuerLogo
        }

        fun bind(
            paymentMethod: String,
            issuerModel: IssuerModel,
            hideIssuerLogo: Boolean,
            onItemClicked: (IssuerModel) -> Unit,
        ) {
            binding.root.setOnClickListener { onItemClicked(issuerModel) }

            binding.textViewTitle.text = issuerModel.name
            if (!hideIssuerLogo) {
                binding.imageViewLogo.loadLogo(
                    environment = issuerModel.environment,
                    txVariant = paymentMethod,
                    txSubVariant = issuerModel.id,
                )
            }
        }
    }

    object IssuerDiffCallBack : DiffUtil.ItemCallback<IssuerModel>() {
        override fun areItemsTheSame(oldItem: IssuerModel, newItem: IssuerModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: IssuerModel, newItem: IssuerModel): Boolean =
            oldItem == newItem
    }
}
