/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/9/2024.
 */

package com.adyen.checkout.mealvoucherfr

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.mealvoucherfr.internal.provider.MealVoucherFRComponentProvider

class MealVoucherFRComponent internal constructor(
    giftCardDelegate: GiftCardDelegate,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<MealVoucherFRComponentState>,
) : GiftCardComponent(giftCardDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler) {
    companion object {
        @JvmField
        val PROVIDER = MealVoucherFRComponentProvider()

        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(
            PaymentMethodTypes.MEAL_VOUCHER_FR_GROUPEUP,
            PaymentMethodTypes.MEAL_VOUCHER_FR_NATIXIS,
            PaymentMethodTypes.MEAL_VOUCHER_FR_SODEXO,
        )
    }
}
