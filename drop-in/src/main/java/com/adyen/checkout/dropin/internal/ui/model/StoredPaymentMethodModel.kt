/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/11/2020.
 */

package com.adyen.checkout.dropin.internal.ui.model

import android.content.Context
import com.adyen.checkout.components.core.internal.util.DateUtils
import com.adyen.checkout.core.Environment
import com.adyen.checkout.dropin.R
import com.adyen.checkout.dropin.internal.ui.PaymentMethodAdapter.StoredPaymentMethodItem

internal sealed class StoredPaymentMethodModel : PaymentMethodListItem {
    abstract val id: String
    abstract val imageId: String
    abstract val isRemovable: Boolean

    override fun getViewType(): Int = PaymentMethodListItem.STORED_PAYMENT_METHOD
}

internal data class StoredCardModel(
    override val id: String,
    override val imageId: String,
    override val isRemovable: Boolean,
    val lastFour: String,
    val expiryMonth: String,
    val expiryYear: String,
    // We need the environment to load the logo
    val environment: Environment,
) : StoredPaymentMethodModel()

internal data class StoredACHDirectDebitModel(
    override val id: String,
    override val imageId: String,
    override val isRemovable: Boolean,
    val lastFour: String,
    // We need the environment to load the logo
    val environment: Environment,
) : StoredPaymentMethodModel()

internal data class StoredPayByBankUSModel(
    override val id: String,
    override val imageId: String,
    override val isRemovable: Boolean,
    val name: String,
    val description: String?,
    // We need the environment to load the logo
    val environment: Environment,
) : StoredPaymentMethodModel()

internal data class GenericStoredModel(
    override val id: String,
    override val imageId: String,
    override val isRemovable: Boolean,
    val name: String,
    val description: String?,
    // We need the environment to load the logo
    val environment: Environment,
) : StoredPaymentMethodModel()

internal fun StoredPaymentMethodModel.mapToStoredPaymentMethodItem(context: Context): StoredPaymentMethodItem {
    return when (this) {
        is StoredCardModel -> {
            StoredPaymentMethodItem(
                title = context.getString(R.string.last_four_digits_format, lastFour),
                subtitle = DateUtils.parseDateToView(expiryMonth, expiryYear),
                imageId = imageId,
                environment = environment
            )
        }
        is GenericStoredModel -> {
            StoredPaymentMethodItem(
                title = name,
                subtitle = description,
                imageId = imageId,
                environment = environment
            )
        }
        is StoredPayByBankUSModel -> {
            StoredPaymentMethodItem(
                title = name,
                subtitle = description,
                imageId = imageId,
                environment = environment
            )
        }
        is StoredACHDirectDebitModel -> {
            StoredPaymentMethodItem(
                title = context.getString(R.string.last_four_digits_format, lastFour),
                subtitle = null,
                imageId = imageId,
                environment = environment
            )
        }
    }
}
