/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/11/2020.
 */

package com.adyen.checkout.dropin.ui.paymentmethods

sealed class StoredPaymentMethodModel : PaymentMethodListItem {
    abstract val id: String
    abstract val imageId: String
    abstract val isRemovable: Boolean

    override fun getViewType(): Int = PaymentMethodListItem.STORED_PAYMENT_METHOD
}

data class StoredCardModel(
    override val id: String,
    override val imageId: String,
    override val isRemovable: Boolean,
    val lastFour: String,
    val expiryMonth: String,
    val expiryYear: String
) : StoredPaymentMethodModel()

data class GenericStoredModel(
    override val id: String,
    override val imageId: String,
    override val isRemovable: Boolean,
    val name: String
) : StoredPaymentMethodModel()
