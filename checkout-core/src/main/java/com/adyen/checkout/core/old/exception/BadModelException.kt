/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */
package com.adyen.checkout.core.old.exception

import com.adyen.checkout.core.old.internal.data.model.ModelObject
import com.adyen.checkout.core.old.internal.data.model.ModelUtils

/**+
 * Exception thrown when a [ModelObject] does not meet the requirement of having a SERIALIZER object.
 */
class BadModelException(
    clazz: Class<*>,
    e: Throwable?
) : CheckoutException(
    "ModelObject protocol requires a ModelObject.Serializer object called ${ModelUtils.SERIALIZER_FIELD_NAME} on " +
        "class ${clazz.simpleName}",
    e
)
