/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2023.
 */

package com.adyen.checkout.cashapppay.internal.ui

import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate

internal interface CashAppPayDelegate :
    PaymentComponentDelegate<CashAppPayComponentState>,
    ViewProvidingDelegate
