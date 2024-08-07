/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/8/2024.
 */

package com.adyen.checkout.components.core

/**
 * Used to configure the method used to handle actions.
 */
enum class ActionHandlingMethod {
    /**
     * The action will be handled in a native way (e.g. using a SDK). **If** there is no way to handle the action
     * natively, then a fallback method will be used (e.g. a web flow).
     */
    PREFER_NATIVE,

    /**
     * The action will be handled with a web flow. **If** there is no way to handle the action with a web flow, then
     * native method will be used.
     */
    PREFER_WEB,
}
