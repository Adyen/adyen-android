package com.adyen.checkout.cashapppay.internal.ui

import com.adyen.checkout.cashapppay.CashAppPayComponentState
import com.adyen.checkout.components.core.internal.ui.PaymentComponentDelegate
import com.adyen.checkout.ui.core.internal.ui.ViewProvidingDelegate

internal interface CashAppPayDelegate :
    PaymentComponentDelegate<CashAppPayComponentState>,
    ViewProvidingDelegate
