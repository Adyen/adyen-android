/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.exception

import com.adyen.checkout.core.common.internal.model.ModelObject
import org.json.JSONException

/**
 * Exception thrown when an issue occurs during serialization of a [ModelObject].
 */
class ModelSerializationException(modelClass: Class<*>, cause: JSONException?) :
// TODO - Error Propagation
//    CheckoutException("Unexpected exception while serializing ${modelClass.simpleName}.", cause)
    RuntimeException("Unexpected exception while serializing ${modelClass.simpleName}.", cause)
