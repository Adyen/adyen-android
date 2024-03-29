/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/12/2022.
 */

package com.adyen.checkout.example.ui.main

internal sealed class MainEvent {
    data class NavigateTo(val destination: MainNavigation) : MainEvent()
    data class Toast(val message: String) : MainEvent()
}
