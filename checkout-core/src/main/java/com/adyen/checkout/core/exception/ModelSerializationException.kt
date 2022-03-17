/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.exception

import org.json.JSONException

/**
 * Exception thrown when an issue occurs during serialization of a [ModelObject].
 */
class ModelSerializationException(modelClass: Class<*>, cause: JSONException?) :
    CheckoutException("Unexpected exception while serializing ${modelClass.simpleName}.", cause)
