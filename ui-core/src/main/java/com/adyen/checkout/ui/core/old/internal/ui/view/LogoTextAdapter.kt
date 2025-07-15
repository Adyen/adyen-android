/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.ui.core.old.internal.ui.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.ui.core.databinding.LogoViewHolderBinding
import com.adyen.checkout.ui.core.databinding.TextViewHolderBinding
import com.adyen.checkout.ui.core.old.internal.ui.loadLogo
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem.LogoItem
import com.adyen.checkout.ui.core.old.internal.ui.model.LogoTextItem.TextItem

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class LogoTextAdapter(private val localizedContext: Context) :
    ListAdapter<LogoTextItem, RecyclerView.ViewHolder>(LogoTextItemDiffCallback) {

    override fun getItemViewType(position: Int): Int {
        return currentList[position].getViewType().type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            LogoTextItem.LogoTextItemViewType.Logo.type -> {
                LogoViewHolder(
                    LogoViewHolderBinding.inflate(inflater, parent, false),
                )
            }

            LogoTextItem.LogoTextItemViewType.Text.type -> {
                TextViewHolder(
                    TextViewHolderBinding.inflate(inflater, parent, false),
                    localizedContext,
                )
            }

            else -> throw CheckoutException("Unexpected viewType on onCreateViewHolder - $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = currentList[position]
        when (holder) {
            is LogoViewHolder -> holder.bind(item as LogoItem)
            is TextViewHolder -> holder.bind(item as TextItem)
        }
    }

    internal class LogoViewHolder(
        private val binding: LogoViewHolderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LogoItem) {
            binding.imageViewBrandLogo.loadLogo(
                environment = item.environment,
                txVariant = item.logoPath,
            )
        }
    }

    internal class TextViewHolder(
        private val binding: TextViewHolderBinding,
        private val localizedContext: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TextItem) {
            binding.textView.text = localizedContext.getString(item.textResId)
        }
    }

    internal object LogoTextItemDiffCallback : DiffUtil.ItemCallback<LogoTextItem>() {

        override fun areItemsTheSame(oldItem: LogoTextItem, newItem: LogoTextItem): Boolean {
            return when (oldItem) {
                is LogoItem -> {
                    oldItem.logoPath == (newItem as? LogoItem)?.logoPath
                }

                is TextItem -> {
                    oldItem.textResId == (newItem as? TextItem)?.textResId
                }
            }
        }

        override fun areContentsTheSame(oldItem: LogoTextItem, newItem: LogoTextItem): Boolean {
            return oldItem == newItem
        }
    }
}
