/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/3/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public interface PaymentComponentProvider<ComponentT extends PaymentComponent, ConfigurationT extends Configuration> {

    @NonNull
    ComponentT get(@NonNull FragmentActivity activity, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT config);

    @NonNull
    ComponentT get(@NonNull Fragment fragment, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT config);
}
