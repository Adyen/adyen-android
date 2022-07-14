/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/5/2019.
 */
package com.adyen.checkout.issuerlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.adapter.ClickableListRecyclerAdapter
import com.adyen.checkout.components.ui.view.RoundCornerImageView
import com.adyen.checkout.issuerlist.IssuerListRecyclerAdapter.IssuerViewHolder

internal class IssuerListRecyclerAdapter(
    private var issuerModelList: List<IssuerModel>,
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean
) : ClickableListRecyclerAdapter<IssuerViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): IssuerViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.recycler_list_with_image, viewGroup, false)
        return IssuerViewHolder(view, hideIssuerLogo)
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

    internal inner class IssuerViewHolder(itemView: View, hideIssuerLogo: Boolean) : RecyclerView.ViewHolder(itemView) {

        private val logoImage: RoundCornerImageView = itemView.findViewById(R.id.imageView_logo)
        private val text: TextView = itemView.findViewById(R.id.textView_text)

        init {
            logoImage.isVisible = !hideIssuerLogo
        }

        fun bind(paymentMethod: String, issuerModel: IssuerModel, hideIssuerLogo: Boolean, imageLoader: ImageLoader) {
            text.text = issuerModel.name
            if (!hideIssuerLogo) {
                imageLoader.load(
                    paymentMethod,
                    issuerModel.id,
                    logoImage,
                    R.drawable.ic_placeholder_image,
                    R.drawable.ic_placeholder_image
                )
            }
        }
    }
}
