/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */
package com.adyen.checkout.core.old.exception

import com.adyen.checkout.core.old.internal.data.model.ModelObject
import org.json.JSONException

/**
 * Exception thrown when an issue occurs during serialization of a [ModelObject].
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
class ModelSerializationException(modelClass: Class<*>, cause: JSONException?) :
    CheckoutException("Unexpected exception while serializing ${modelClass.simpleName}.", cause)
