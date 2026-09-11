/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import androidx.annotation.RestrictTo

/**
 * The action key a text input shows on the keyboard.
 *
 * This is our own type rather than Compose's `ImeAction` so that the state layer stays free of Compose and can be
 * tested on the JVM. The view layer maps it to an `ImeAction`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class KeyboardAction {

    /**
     * Moves focus to the next text input.
     */
    NEXT,

    /**
     * Closes the keyboard. Shown on the last text input of a form. It does not submit.
     */
    DONE,
}
