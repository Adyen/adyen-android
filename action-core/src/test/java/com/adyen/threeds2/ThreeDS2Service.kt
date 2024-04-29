/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/4/2024.
 */

package com.adyen.threeds2

// Fake ThreeDS2Service that overrides the static instance of the actual library, because it crashes unit tests
@Suppress("unused")
object ThreeDS2Service {
    val INSTANCE = this
}
