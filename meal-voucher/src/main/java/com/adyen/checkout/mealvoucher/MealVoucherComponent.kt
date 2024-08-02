/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/7/2024.
 */

package com.adyen.checkout.mealvoucher

import com.adyen.checkout.action.core.internal.DefaultActionHandlingComponent
import com.adyen.checkout.action.core.internal.ui.GenericActionDelegate
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ComponentEventHandler
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.giftcard.internal.ui.GiftCardDelegate
import com.adyen.checkout.mealvoucher.internal.provider.MealVoucherComponentProvider

class MealVoucherComponent internal constructor(
    giftCardDelegate: GiftCardDelegate,
    genericActionDelegate: GenericActionDelegate,
    actionHandlingComponent: DefaultActionHandlingComponent,
    internal val componentEventHandler: ComponentEventHandler<MealVoucherComponentState>,
) : GiftCardComponent(giftCardDelegate, genericActionDelegate, actionHandlingComponent, componentEventHandler) {
    companion object {
        @JvmField
        val PROVIDER = MealVoucherComponentProvider()

        // TODO update it to correct txVariants
        @JvmField
        val PAYMENT_METHOD_TYPES = listOf(
            PaymentMethodTypes.MEAL_VOUCHER_FR_GROUPEUP,
            PaymentMethodTypes.MEAL_VOUCHER_FR_NATIXIS,
            PaymentMethodTypes.MEAL_VOUCHER_FR_SODEXO,
        )
    }
}
