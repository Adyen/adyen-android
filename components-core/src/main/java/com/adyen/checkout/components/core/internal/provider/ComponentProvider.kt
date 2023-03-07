/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.components.core.internal.provider

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.Component

/**
 * This provider should be used to get an instance of a [Component] that is bound your lifecycle.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentProvider<ComponentT : Component>
