/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */
package com.adyen.checkout.components.ui.adapter

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger

abstract class ClickableListRecyclerAdapter<ViewHolderT : RecyclerView.ViewHolder> : RecyclerView.Adapter<ViewHolderT>() {

    var onItemCLickedListener: OnItemCLickedListener? = null

    @CallSuper
    override fun onBindViewHolder(viewHolderT: ViewHolderT, position: Int) {
        viewHolderT.itemView.setOnClickListener {
            Logger.d(TAG, "click")
            onItemCLickedListener?.onItemClicked(viewHolderT.bindingAdapterPosition)
        }
    }

    fun setItemCLickListener(itemCLickListener: OnItemCLickedListener) {
        onItemCLickedListener = itemCLickListener
    }

    fun interface OnItemCLickedListener {
        fun onItemClicked(position: Int)
    }

    companion object {
        val TAG = getTag()
    }
}
