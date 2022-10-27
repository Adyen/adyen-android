/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2022.
 */

package com.adyen.checkout.components

sealed class ComponentResult {
    class ActionDetails(val data: ActionComponentData) : ComponentResult()
    class Error(val error: ComponentError) : ComponentResult()
}
