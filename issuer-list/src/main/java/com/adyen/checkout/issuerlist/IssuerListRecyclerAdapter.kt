/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */
package com.adyen.checkout.issuerlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.databinding.RecyclerListWithImageBinding
import com.adyen.checkout.issuerlist.IssuerListRecyclerAdapter.IssuerViewHolder

internal class IssuerListRecyclerAdapter(
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean,
    private val onItemClicked: (IssuerModel) -> Unit,
) : ListAdapter<IssuerModel, IssuerViewHolder>(IssuerDiffCallBack) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IssuerViewHolder {
        val binding = RecyclerListWithImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return IssuerViewHolder(binding, hideIssuerLogo)
    }

    override fun onBindViewHolder(viewHolder: IssuerViewHolder, position: Int) {
        viewHolder.bind(paymentMethod, currentList[position], hideIssuerLogo, imageLoader, onItemClicked)
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
            imageLoader: ImageLoader,
            onItemClicked: (IssuerModel) -> Unit,
        ) {
            binding.root.setOnClickListener { onItemClicked(issuerModel) }

            binding.textViewTitle.text = issuerModel.name
            if (!hideIssuerLogo) {
                imageLoader.load(
                    paymentMethod,
                    issuerModel.id,
                    binding.imageViewLogo,
                    R.drawable.ic_placeholder_image,
                    R.drawable.ic_placeholder_image
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
