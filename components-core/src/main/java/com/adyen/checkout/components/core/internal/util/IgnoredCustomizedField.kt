/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 1/11/2023.
 */

package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo

/**
 * Preferably another field should be used instead, which is already customized.
 * In other cases [IgnoredCustomizedField] annotation can be used to suppress warning.
 */
// TODO: Explain what is this annotation in the comment section
// TODO: Make sure it is well configured. More here - https://kotlinlang.org/docs/opt-in-requirements.html#create-opt-in-requirement-annotations
// TODO: We could pass a custom message to be shown for the opt in
@RequiresOptIn(message = "This field should not be used. Preferably use a customized alternative of this field.")
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
annotation class IgnoredCustomizedField
