/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */

package com.adyen.checkout.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;

public interface ComponentAvailableCallback<ConfigurationT extends Configuration> {

    void onAvailabilityResult(boolean isAvailable, @NonNull PaymentMethod paymentMethod, @Nullable ConfigurationT config);
}
