/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateFactory
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class BlikComponentStateFactory : ComponentStateFactory<BlikComponentState> {

    override fun createInitialState() = BlikComponentState(
        blikCode = TextInputComponentState(isFocused = true, description = CheckoutLocalizationKey.BLIK_CODE_HINT),
        isLoading = false,
    )
}
