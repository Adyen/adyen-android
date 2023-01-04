/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 4/1/2023.
 */

package com.adyen.checkout.example.ui.instant

sealed class InstantViewState {
    object Loading : InstantViewState()
    object Error : InstantViewState()
    object ShowComponent : InstantViewState()
}
