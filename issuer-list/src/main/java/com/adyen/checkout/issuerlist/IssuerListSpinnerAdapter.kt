/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */
package com.adyen.checkout.issuerlist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.adyen.checkout.components.api.ImageLoader
import com.adyen.checkout.components.ui.databinding.SpinnerListWithImageBinding

class IssuerListSpinnerAdapter internal constructor(
    private val context: Context,
    private var issuerList: List<IssuerModel>,
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean
) : BaseAdapter() {

    override fun getCount(): Int {
        return issuerList.size
    }

    override fun getItem(position: Int): IssuerModel {
        return issuerList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: IssuerListSpinnerViewHolder
        val binding: SpinnerListWithImageBinding
        if (convertView == null) {
            binding = SpinnerListWithImageBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            viewHolder = IssuerListSpinnerViewHolder(binding, imageLoader, paymentMethod, hideIssuerLogo)
        } else {
            view = convertView
            viewHolder = view.tag as IssuerListSpinnerViewHolder
        }
        viewHolder.bind(getItem(position))
        return view
    }
}

internal class IssuerListSpinnerViewHolder(
    private val binding: SpinnerListWithImageBinding,
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean
) : ViewHolder(binding.root) {

    fun bind(model: IssuerModel) {
        binding.textViewText.text = model.name
        if (!hideIssuerLogo) {
            imageLoader.load(
                paymentMethod,
                model.id,
                binding.imageViewLogo,
                R.drawable.ic_placeholder_image,
                R.drawable.ic_placeholder_image
            )
        } else {
            binding.imageViewLogo.isVisible = false
        }
    }

}
