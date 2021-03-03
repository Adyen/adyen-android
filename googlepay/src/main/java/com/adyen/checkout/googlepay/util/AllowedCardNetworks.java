/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */

package com.adyen.checkout.googlepay.util;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.ArrayList;
import java.util.List;

public final class AllowedCardNetworks {

    public static final String AMEX = "AMEX";
    public static final String DISCOVER = "DISCOVER";
    public static final String INTERAC = "INTERAC";
    public static final String JCB = "JCB";
    public static final String MASTERCARD = "MASTERCARD";
    public static final String VISA = "VISA";

    /**
     * A list of the allowed credit card networks accepted on Google Pay.
     *
     * @return The list of all allowed card networks.
     */
    @NonNull
    public static List<String> getAllAllowedCardNetworks() {
        final ArrayList<String> allowedCardNetworks = new ArrayList<>();
        allowedCardNetworks.add(AMEX);
        allowedCardNetworks.add(DISCOVER);
        allowedCardNetworks.add(INTERAC);
        allowedCardNetworks.add(JCB);
        allowedCardNetworks.add(MASTERCARD);
        allowedCardNetworks.add(VISA);
        return allowedCardNetworks;
    }

    private AllowedCardNetworks() {
        throw new NoConstructorException();
    }
}
