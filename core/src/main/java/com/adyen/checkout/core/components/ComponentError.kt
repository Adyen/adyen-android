/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 20/5/2019.
 */
package com.adyen.checkout.core.components

/**
 * Data about an error that happened inside a component.
 */
class ComponentError(
    // TODO - Error propagation
    /**
     * Can be used to try to identify the root cause of the issue.
     *
     * @return The exception that happened.
     */
    val exception: RuntimeException
) {

    /**
     * This message is not intended for user feedback, but for development feedback on what happened.
     *
     * @return A development driven error message from the Exception.
     */
    val errorMessage: String
        get() = exception.message.orEmpty()
}
