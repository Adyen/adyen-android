/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/10/2023.
 */

package com.adyen.checkout.twint.internal.ui

import androidx.annotation.RestrictTo
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.components.core.internal.ui.ActionDelegate
import com.adyen.checkout.components.core.internal.ui.DetailsEmittingDelegate
import com.adyen.checkout.components.core.internal.ui.StatusPollingDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface TwintActionDelegate :
    ActionDelegate,
    DetailsEmittingDelegate,
    StatusPollingDelegate,
    ViewProvidingDelegate {

    val payEventFlow: Flow<String>

    fun handleTwintResult(result: TwintPayResult)
}
