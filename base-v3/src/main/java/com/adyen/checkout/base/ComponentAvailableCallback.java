/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public interface ComponentAvailableCallback<ConfigurationT extends Configuration> {

    void onAvailabilityResult(boolean isAvailable, @NonNull PaymentMethod paymentMethod, @NonNull ConfigurationT config);
}
