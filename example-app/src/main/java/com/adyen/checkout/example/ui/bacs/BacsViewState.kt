/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/1/2023.
 */

package com.adyen.checkout.example.ui.bacs

import androidx.annotation.StringRes

internal sealed class BacsViewState {

    object Loading : BacsViewState()

    object ShowComponent : BacsViewState()

    data class Error(@StringRes val message: Int, val arg: String? = null) : BacsViewState()
}
