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
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import com.adyen.checkout.components.api.ImageLoader

class IssuerListSpinnerAdapter internal constructor(
    context: Context,
    private var issuerList: List<IssuerModel>,
    private val imageLoader: ImageLoader,
    private val paymentMethod: String,
    private val hideIssuerLogo: Boolean
) : BaseAdapter() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

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
        val (id, name) = getItem(position)
        // TODO: 15/03/2019 check if optimization with ViewHolder is possible
        val view: View = convertView ?: inflater.inflate(R.layout.spinner_list_with_image, parent, false)
        val issuerLogo: AppCompatImageView = view.findViewById(R.id.imageView_logo)
        val issuerName: AppCompatTextView = view.findViewById(R.id.textView_text)
        issuerName.text = name
        if (!hideIssuerLogo) {
            imageLoader.load(
                paymentMethod,
                id,
                issuerLogo,
                R.drawable.ic_placeholder_image,
                R.drawable.ic_placeholder_image
            )
        } else {
            issuerLogo.isVisible = false
        }
        return view
    }
}
