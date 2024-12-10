/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import androidx.annotation.StringRes

internal sealed class GooglePayViewState {

    data object Loading : GooglePayViewState()

    data object ShowComponent : GooglePayViewState()

    data class Error(@StringRes val message: Int) : GooglePayViewState()
}
