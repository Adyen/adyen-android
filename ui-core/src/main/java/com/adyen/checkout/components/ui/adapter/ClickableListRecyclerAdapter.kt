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
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

abstract class ClickableListRecyclerAdapter<ViewHolderT : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<ViewHolderT>() {

    var onItemCLickedListener: OnItemCLickedListener? = null

    @CallSuper
    override fun onBindViewHolder(viewHolder: ViewHolderT, position: Int) {
        viewHolder.itemView.setOnClickListener {
            Logger.d(TAG, "click")
            onItemCLickedListener?.onItemClicked(viewHolder.bindingAdapterPosition)
        }
    }

    fun setItemCLickListener(itemCLickListener: OnItemCLickedListener) {
        onItemCLickedListener = itemCLickListener
    }

    fun interface OnItemCLickedListener {
        fun onItemClicked(position: Int)
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
