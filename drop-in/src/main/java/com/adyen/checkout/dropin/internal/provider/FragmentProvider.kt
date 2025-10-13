/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 8/12/2023.
 */

package com.adyen.checkout.dropin.internal.provider

import com.adyen.checkout.bacs.BacsDirectDebitComponent
import com.adyen.checkout.card.old.CardComponent
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.dropin.internal.ui.BacsDirectDebitDialogFragment
import com.adyen.checkout.dropin.internal.ui.CardComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.DropInBottomSheetDialogFragment
import com.adyen.checkout.dropin.internal.ui.GenericComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.GiftCardComponentDialogFragment
import com.adyen.checkout.dropin.internal.ui.GooglePayComponentDialogFragment
import com.adyen.checkout.dropin.internal.util.checkCompileOnly
import com.adyen.checkout.giftcard.GiftCardComponent
import com.adyen.checkout.googlepay.GooglePayComponent
import com.adyen.checkout.mealvoucherfr.MealVoucherFRComponent

internal fun getFragmentForStoredPaymentMethod(
    storedPaymentMethod: StoredPaymentMethod,
    fromPreselected: Boolean
): DropInBottomSheetDialogFragment {
    return when {
        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(storedPaymentMethod) } -> {
            CardComponentDialogFragment.newInstance(storedPaymentMethod, fromPreselected)
        }

        else -> {
            GenericComponentDialogFragment.newInstance(storedPaymentMethod, fromPreselected)
        }
    }
}

internal fun getFragmentForPaymentMethod(paymentMethod: PaymentMethod): DropInBottomSheetDialogFragment {
    return when {
        checkCompileOnly { CardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            CardComponentDialogFragment.newInstance(paymentMethod)
        }

        checkCompileOnly { BacsDirectDebitComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            BacsDirectDebitDialogFragment.newInstance(paymentMethod)
        }

        checkCompileOnly {
            GiftCardComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) ||
                MealVoucherFRComponent.PROVIDER.isPaymentMethodSupported(paymentMethod)
        } -> {
            GiftCardComponentDialogFragment.newInstance(paymentMethod)
        }

        checkCompileOnly { GooglePayComponent.PROVIDER.isPaymentMethodSupported(paymentMethod) } -> {
            GooglePayComponentDialogFragment.newInstance(paymentMethod)
        }

        else -> {
            GenericComponentDialogFragment.newInstance(paymentMethod)
        }
    }
}
