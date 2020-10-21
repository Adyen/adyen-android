/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/7/2019.
 */

package com.adyen.checkout.googlepay.util;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

import java.util.ArrayList;
import java.util.List;

public final class AllowedAuthMethods {

    public static final String PAN_ONLY = "PAN_ONLY";
    public static final String CRYPTOGRAM_3DS = "CRYPTOGRAM_3DS";

    /**
     * The the Google Pay authentication methods accepted by Adyen.
     *
     * @return A list of the allowed authentication methods.
     */
    @NonNull
    public static List<String> getAllAllowedAuthMethods() {
        final ArrayList<String> allowedAuthMethods = new ArrayList<>();
        allowedAuthMethods.add(PAN_ONLY);
        allowedAuthMethods.add(CRYPTOGRAM_3DS);
        return allowedAuthMethods;
    }

    private AllowedAuthMethods() {
        throw new NoConstructorException();
    }
}
