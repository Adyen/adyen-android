/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/11/2018.
 */

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.internal.model.SurchargeConfigurationImpl;

/**
 * {@link Configuration} for a {@link PaymentMethod} that indicates the costs and total amount of a payment.
 */
@ProvidedBy(SurchargeConfigurationImpl.class)
public interface SurchargeConfiguration extends Configuration {
    /**
     * @return The currency code of the surcharge.
     */
    @NonNull
    String getSurchargeCurrencyCode();

    /**
     * @return The fixed cost of the surcharge, in minor units.
     */
    long getSurchargeFixedCost();

    /**
     * @return The variable cost of the surcharge in basis points.
     */
    int getSurchargeVariableCost();

    /**
     * @return The total cost of the surcharge, in minor units.
     */
    long getSurchargeTotalCost();


    /**
     * @return The final amount of the payment, including the surcharge, in minor units.
     */
    long getSurchargeFinalAmount();
}
