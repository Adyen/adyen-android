/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */

package com.adyen.checkout.components;

import androidx.annotation.RestrictTo;

/**
 * This provider should be used to get an instance of a {@link Component} that is bound your lifecycle.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface ComponentProvider<ComponentT extends Component> {
    // Maker interface
}
