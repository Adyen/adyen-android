/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 19/04/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.support.annotation.NonNull;

public interface CheckoutMethodPickerListener {
    void onCheckoutMethodSelected(@NonNull CheckoutMethod checkoutMethod);

    void onCheckoutMethodDelete(@NonNull CheckoutMethod checkoutMethod);

    boolean isCheckoutMethodDeletable(@NonNull CheckoutMethod checkoutMethod);

    void onClearSelection();
}
