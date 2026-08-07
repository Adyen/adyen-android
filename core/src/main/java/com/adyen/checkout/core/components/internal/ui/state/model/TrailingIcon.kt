/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/12/2025.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.ui.CheckoutTextFieldTrailingIcon

/**
 * The trailing icon to be displayed at the end of a text field.
 *
 * [Empty] and [Error] are generic states handled by [CheckoutTextFieldTrailingIcon] for every field. Fields that need
 * their own icons declare additional states by extending this class.
 *
 * Implementations must be comparable by equality, as they are used as the target state of an animation. Prefer
 * `data object` or `data class`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class TrailingIcon {

    /**
     * No icon is displayed.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object Empty : TrailingIcon()

    /**
     * The generic error icon, displayed whenever the field is showing an error.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object Error : TrailingIcon()
}
