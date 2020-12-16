/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
 */

package com.adyen.checkout.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelStoreOwner;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod;
import com.adyen.checkout.core.exception.CheckoutException;


public interface StoredPaymentComponentProvider<ComponentT extends PaymentComponent, ConfigurationT extends Configuration>
        extends PaymentComponentProvider<ComponentT, ConfigurationT> {

    @SuppressWarnings("LambdaLast")
    @NonNull
    ComponentT get(
            @NonNull ViewModelStoreOwner viewModelStoreOwner,
            @NonNull StoredPaymentMethod storedPaymentMethod,
            @NonNull ConfigurationT configuration
    ) throws CheckoutException;
}
