/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/11/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.runtime.Composable
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.common.localization.internal.helper.resolveString

/**
 * Text that a view model decides on and a screen resolves, so the choice of copy can be tested without Compose.
 */
internal sealed interface UiText {

    @Composable
    fun resolve(): String

    data class Raw(val value: String) : UiText {
        @Composable
        override fun resolve() = value
    }

    data class Localized(
        val key: CheckoutLocalizationKey,
        val formatArgs: List<Any> = emptyList(),
    ) : UiText {
        @Suppress("SpreadOperator")
        @Composable
        override fun resolve() = resolveString(key, *formatArgs.toTypedArray())
    }
}
