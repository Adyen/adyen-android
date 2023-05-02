/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/4/2023.
 */

package com.adyen.checkout.example.ui.giftcard

internal sealed class GiftCardViewState {

    object Loading : GiftCardViewState()

    object ShowComponent : GiftCardViewState()

    object Error : GiftCardViewState()
}
