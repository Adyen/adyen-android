/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.components

/**
 * This provider should be used to get an instance of a [Component] that is bound your lifecycle.
 */
interface ComponentProvider<ComponentT : Component<*, *>>
