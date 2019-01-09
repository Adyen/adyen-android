/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 05/04/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.ui.internal.common.fragment.CheckoutDetailsFragment;

public interface CheckoutHandler extends CheckoutSessionProvider {

    void presentDetailsActivity(@NonNull Intent intent);

    void presentDetailsFragment(@NonNull CheckoutDetailsFragment fragment);

    void handleWithPaymentMethodHandler(@NonNull PaymentMethodHandler paymentMethodHandler);
}
