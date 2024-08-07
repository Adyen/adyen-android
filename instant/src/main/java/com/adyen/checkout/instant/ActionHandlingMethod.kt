/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 28/11/2023.
 */

package com.adyen.checkout.instant

import com.adyen.checkout.components.core.ActionHandlingMethod

@Deprecated(
    "This class has been moved to a new package",
    ReplaceWith("TwintActionConfiguration", "com.adyen.checkout.components.core.ActionHandlingMethod"),
)
typealias ActionHandlingMethod = ActionHandlingMethod
