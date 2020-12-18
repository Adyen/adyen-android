/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 24/8/2020.
 */

package com.adyen.checkout.components.util;

import com.adyen.checkout.core.exception.NoConstructorException;

/**
 * Helper class with a list of all the currently supported Actions on Components and Drop-In.
 */
public final class  ActionTypes {

    public static final String AWAIT = "await";
    public static final String QR_CODE = "qrCode";
    public static final String REDIRECT = "redirect";
    public static final String SDK = "sdk";
    public static final String THREEDS2_CHALLENGE = "threeDS2Challenge";
    public static final String THREEDS2_FINGERPRINT = "threeDS2Fingerprint";
    public static final String THREEDS2 = "threeDS2";
    public static final String VOUCHER = "voucher";

    private ActionTypes() {
        throw new NoConstructorException();
    }
}
