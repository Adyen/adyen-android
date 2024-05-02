/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/4/2024.
 */

package com.adyen.checkout.upi.internal.ui.model

import androidx.annotation.StringRes
import com.adyen.checkout.core.Environment

internal sealed class UPICollectItem {

    abstract fun areItemsTheSame(newItem: UPICollectItem): Boolean
    abstract fun areContentsTheSame(newItem: UPICollectItem): Boolean
    abstract fun getChangePayload(newItem: UPICollectItem): Any?

    data class PaymentApp(
        val id: String,
        val name: String,
        val environment: Environment,
    ) : UPICollectItem() {
        override fun areItemsTheSame(newItem: UPICollectItem) =
            newItem is PaymentApp &&
                id == newItem.id

        override fun areContentsTheSame(newItem: UPICollectItem) =
            newItem is PaymentApp &&
                name == newItem.name &&
                environment == newItem.environment

        override fun getChangePayload(newItem: UPICollectItem) = null
    }

    data object GenericApp : UPICollectItem() {
        override fun areItemsTheSame(newItem: UPICollectItem) = true
        override fun areContentsTheSame(newItem: UPICollectItem) = true
        override fun getChangePayload(newItem: UPICollectItem) = null
    }

    data class ManualInput(
        @StringRes val errorMessageResource: Int?
    ) : UPICollectItem() {
        override fun areItemsTheSame(newItem: UPICollectItem) = true
        override fun areContentsTheSame(newItem: UPICollectItem) =
            newItem is ManualInput &&
                errorMessageResource == newItem.errorMessageResource

        // This has been implemented to avoid creating a new ViewHolder when the input field has validation error
        override fun getChangePayload(newItem: UPICollectItem) =
            newItem is ManualInput &&
                errorMessageResource == newItem.errorMessageResource
    }
}
