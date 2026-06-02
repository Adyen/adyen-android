/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/10/2025.
 */

package com.adyen.checkout.card.internal.helper

private val HIDDEN_BRANDS = setOf("accel", "pulse", "star", "nyce")

internal fun isHiddenCardType(brand: String) = brand in HIDDEN_BRANDS
