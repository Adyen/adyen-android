/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.common.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.ui.text.input.ImeAction
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction

/**
 * Maps the keyboard action the form decided on to the one Compose understands.
 *
 * The two are kept apart so that the state layer can be tested without Compose, which is why this translation lives
 * here rather than on [KeyboardAction] itself.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun KeyboardAction.toImeAction(): ImeAction = when (this) {
    KeyboardAction.NEXT -> ImeAction.Next
    KeyboardAction.DONE -> ImeAction.Done
}
