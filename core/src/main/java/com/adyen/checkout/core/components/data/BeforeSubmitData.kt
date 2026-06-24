/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 15/6/2026.
 */

package com.adyen.checkout.core.components.data

/**
 * Data that can be inspected or modified before the payment is submitted.
 *
 * This type intentionally exposes only shopper data that can be changed during the sessions `onBeforeSubmit` callback.
 * Use [copy] to return modified values.
 *
 * Null fields mean "keep the original value collected by the component". They do not clear the value.
 *
 * @property billingAddress The billing address of the shopper.
 * @property deliveryAddress The delivery address of the shopper.
 * @property shopperName The name of the shopper.
 * @property shopperEmail The email address of the shopper.
 */
data class BeforeSubmitData(
    val billingAddress: Address? = null,
    val deliveryAddress: Address? = null,
    val shopperName: ShopperName? = null,
    val shopperEmail: String? = null,
)
