/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.exception

import com.adyen.checkout.core.model.ModelUtils

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
