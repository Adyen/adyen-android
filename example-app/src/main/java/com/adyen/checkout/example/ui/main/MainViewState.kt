/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/4/2022.
 */

package com.adyen.checkout.example.ui.main

internal sealed class MainViewState {
    object Loading : MainViewState()
    data class Error(val message: String) : MainViewState()
    data class Result(val items: List<ComponentItem>) : MainViewState()
}
