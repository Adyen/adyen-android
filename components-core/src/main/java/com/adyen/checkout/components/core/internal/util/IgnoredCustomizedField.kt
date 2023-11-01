/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 1/11/2023.
 */

package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope
import kotlin.RequiresOptIn.Level

/**
 * Use [IgnoredCustomizedField] annotation to restrict direct use of properties.
 * When using this annotation, make sure to provide a customized alternative property to be used instead.
 */
@RequiresOptIn(
    level = Level.WARNING,
    message = "This field should not be directly used. Preferably use a customized alternative of this field."
)
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@RestrictTo(Scope.LIBRARY_GROUP)
annotation class IgnoredCustomizedField
