/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 12/3/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.adyen.checkout.ui.core.internal.ui.loadLogo
import com.adyen.checkout.upi.databinding.UpiAppBinding
import com.adyen.checkout.upi.databinding.UpiAppManualAddressBinding
import com.adyen.checkout.upi.internal.ui.model.UPIApp

// TODO: Check this adapter for Arabic
// TODO: Make sure logos look nice on dark mode
internal class UPIAppsAdapter(
    private val context: Context,
    private val paymentMethod: String,
) : RecyclerView.Adapter<ViewHolder>() {

    private val upiApps: MutableList<UPIApp> = mutableListOf()

    private var lastCheckedPosition: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(upiApps: List<UPIApp>) {
        this.upiApps.clear()
        this.upiApps.addAll(upiApps)
        notifyDataSetChanged()
    }

    override fun getItemCount() = upiApps.size + 1

    override fun getItemViewType(position: Int): Int {
        val viewType = if (itemCount > 0 && position < itemCount - 1) {
            UPIViewType.VIEW_TYPE_APP
        } else {
            UPIViewType.VIEW_TYPE_MANUAL_ADDRESS
        }
        return viewType.id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val upiViewType = UPIViewType.entries[viewType]
        return when (upiViewType) {
            UPIViewType.VIEW_TYPE_APP -> {
                val binding = UpiAppBinding.inflate(LayoutInflater.from(context), parent, false)
                UPIAppViewHolder(binding)
            }

            UPIViewType.VIEW_TYPE_MANUAL_ADDRESS -> {
                val binding = UpiAppManualAddressBinding.inflate(LayoutInflater.from(context), parent, false)
                UPIManualAddressViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val upiViewType = UPIViewType.entries[holder.itemViewType]
        when (upiViewType) {
            UPIViewType.VIEW_TYPE_APP -> {
                val viewHolder = holder as UPIAppViewHolder
                onBindViewHolderForUPIApp(viewHolder, position)
            }

            UPIViewType.VIEW_TYPE_MANUAL_ADDRESS -> {
                val viewHolder = holder as UPIManualAddressViewHolder
                onBindViewHolderForUPIManualAddress(viewHolder, position)
            }
        }
    }

    private fun onBindViewHolderForUPIApp(viewHolder: UPIAppViewHolder, position: Int) = with(viewHolder) {
        binding.radioButtonUpiApp.isChecked = position == lastCheckedPosition
        binding.layoutUpiApp.setOnClickListener {
            binding.radioButtonUpiApp.isChecked = true
        }
        binding.radioButtonUpiApp.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // TODO: Fix flickering when item changes
                notifyItemChanged(lastCheckedPosition)
                lastCheckedPosition = position
            }
        }

        bindItem(paymentMethod = paymentMethod, model = upiApps[position])
    }

    private fun onBindViewHolderForUPIManualAddress(
        viewHolder: UPIManualAddressViewHolder,
        position: Int
    ) = with(viewHolder) {
        val isChecked = position == lastCheckedPosition

        // TODO: Can things move to bindItem()?
        binding.radioButtonUpiManualAddress.isChecked = isChecked
        binding.layoutUpiManualAddress.setOnClickListener {
            binding.radioButtonUpiManualAddress.isChecked = true
        }
        binding.radioButtonUpiManualAddress.setOnCheckedChangeListener { _, isChecked ->
            binding.textInputLayoutManualAddress.isVisible = isChecked

            if (isChecked) {
                // TODO: Fix flickering when item changes
                notifyItemChanged(lastCheckedPosition)
                lastCheckedPosition = position
//                binding.editTextManualAddress.requestFocus()
//                binding.editTextManualAddress.showKeyboard()
            } else {
//                binding.editTextManualAddress.clearFocus()
//                binding.editTextManualAddress.hideKeyboard()
            }
        }

        bindItem(isChecked)
    }

    internal class UPIAppViewHolder(
        val binding: UpiAppBinding,
    ) : ViewHolder(binding.root) {
        fun bindItem(paymentMethod: String, model: UPIApp) {
            binding.textViewUpiAppName.text = model.name

            binding.imageViewUpiLogo.loadLogo(
                environment = model.environment,
                txVariant = paymentMethod,
                txSubVariant = model.id,
            )
        }
    }

    internal class UPIManualAddressViewHolder(
        val binding: UpiAppManualAddressBinding,
    ) : ViewHolder(binding.root) {
        fun bindItem(isChecked: Boolean) {
            // TODO: Add a proper name which should be localized
            binding.textViewUpiAppName.text = "Test name for now"
            // TODO: If selected show the input layout
        }
    }

    object UPIAppsDiffCallback : DiffUtil.ItemCallback<UPIApp>() {
        override fun areItemsTheSame(oldItem: UPIApp, newItem: UPIApp): Boolean = oldItem == newItem

        override fun areContentsTheSame(oldItem: UPIApp, newItem: UPIApp): Boolean = areItemsTheSame(oldItem, newItem)
    }

    enum class UPIViewType(val id: Int) {
        VIEW_TYPE_APP(0),
        VIEW_TYPE_MANUAL_ADDRESS(1),
    }
}
