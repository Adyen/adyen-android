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
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.adapter.ClickableListRecyclerAdapter
import com.adyen.checkout.components.ui.databinding.RecyclerListWithImageBinding
import com.adyen.checkout.issuerlist.IssuerListRecyclerAdapter.IssuerViewHolder

internal class IssuerListRecyclerAdapter(
    private var issuerModelList: List<IssuerModel>,
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean
) : ClickableListRecyclerAdapter<IssuerViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IssuerViewHolder {
        val binding = RecyclerListWithImageBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
        return IssuerViewHolder(binding, hideIssuerLogo)
    }

    override fun onBindViewHolder(viewHolder: IssuerViewHolder, position: Int) {
        super.onBindViewHolder(viewHolder, position)
        viewHolder.bind(paymentMethod, issuerModelList[position], hideIssuerLogo, imageLoader)
    }

    override fun getItemCount(): Int {
        return issuerModelList.size
    }

    fun getIssuerAt(position: Int): IssuerModel {
        return issuerModelList[position]
    }

    internal inner class IssuerViewHolder(
        private val binding: RecyclerListWithImageBinding,
        hideIssuerLogo: Boolean
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.imageViewLogo.isVisible = !hideIssuerLogo
        }

        fun bind(paymentMethod: String, issuerModel: IssuerModel, hideIssuerLogo: Boolean, imageLoader: ImageLoader) {
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
}
