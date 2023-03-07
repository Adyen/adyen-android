/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 20/2/2019.
 */
package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.ComponentDelegate

/**
 * A [Component] is a class that helps to retrieve or format data related to a part of the Checkout API payment.
 *
 * @param <ComponentResultT> The main parameter that notifies changes on this component.
 * @param <ConfigurationT> The Configuration object associated with this Component.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface Component {
    /**
     * The delegate from this component.
     */
    val delegate: ComponentDelegate
}
