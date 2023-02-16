/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 17/11/2022.
 */

package com.adyen.checkout.example.ui.blik

import androidx.annotation.StringRes
import com.adyen.checkout.components.model.payments.response.Action as ActionResponse

sealed class BlikViewState {

    object Loading : BlikViewState()

    data class ShowComponent(val componentData: BlikComponentData) : BlikViewState()

    data class Action(val action: ActionResponse) : BlikViewState()

    class Error(@StringRes val stringId: Int, val arg: String? = null) : BlikViewState()
}
