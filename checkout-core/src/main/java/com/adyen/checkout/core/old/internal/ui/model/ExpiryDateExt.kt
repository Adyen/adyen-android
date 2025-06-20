/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/10/2024.
 */

package com.adyen.checkout.core.old.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.ui.model.ExpiryDate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmField
val EMPTY_DATE: ExpiryDate = ExpiryDate(0, 0)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@JvmField
val INVALID_DATE: ExpiryDate = ExpiryDate(-1, -1)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun ExpiryDate.isEmptyDate() = expiryMonth == 0 && expiryYear == 0

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun ExpiryDate.isInvalidDate() = expiryMonth == -1 && expiryYear == -1
