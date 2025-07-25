/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 27/5/2024.
 */

package com.adyen.checkout.upi.internal.ui.view

import android.content.Context
import android.os.Build
import androidx.autofill.HintConstants
import androidx.core.view.doOnAttach
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.ui.core.old.internal.util.hideError
import com.adyen.checkout.ui.core.old.internal.util.hideKeyboard
import com.adyen.checkout.ui.core.old.internal.util.isVisible
import com.adyen.checkout.ui.core.old.internal.util.showError
import com.adyen.checkout.ui.core.old.internal.util.showKeyboard
import com.adyen.checkout.upi.databinding.UpiAppManualAddressBinding
import com.adyen.checkout.upi.internal.ui.model.UPIIntentItem

internal class UPIIntentManualAddressViewHolder(
    private val binding: UpiAppManualAddressBinding,
    private val localizedContext: Context,
    private val onInputChangeListener: (String) -> Unit,
) : UPIIntentItemViewHolder(binding) {

    init {
        binding.editTextManualAddress.setOnChangeListener { editable ->
            onInputChangeListener.invoke(editable.toString())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.editTextManualAddress.setAutofillHints(HintConstants.AUTOFILL_HINT_UPI_VPA)
        }
    }

    override fun bind(item: UPIIntentItem, onClickListener: (UPIIntentItem) -> Unit) {
        (item as? UPIIntentItem.ManualInput) ?: run {
            adyenLog(AdyenLogLevel.DEBUG) { "Item type is not recognized, thus the item can not be bound" }
            return
        }

        itemView.setOnClickListener {
            onClickListener.invoke(item)
        }

        val errorMessage = item.errorMessageResource?.let { messageResource ->
            localizedContext.getString(messageResource)
        }
        bindItem(item.isSelected, errorMessage)
    }

    private fun bindItem(
        isChecked: Boolean,
        errorMessage: String?
    ) = with(binding) {
        radioButtonUpiManualAddress.isChecked = isChecked

        with(editTextManualAddress) {
            setOnFocusChangeListener { focusedView, hasFocus ->
                if (hasFocus) {
                    focusedView.showKeyboard()
                } else {
                    focusedView.hideKeyboard()
                }
            }
            doOnAttach { view ->
                textInputLayoutManualAddress.isVisible = isChecked

                if (isChecked) {
                    view.requestFocus()
                } else {
                    view.clearFocus()
                }
            }
        }

        errorMessage?.let { error ->
            textInputLayoutManualAddress.showError(error)
        } ?: run {
            textInputLayoutManualAddress.hideError()
        }
    }
}
