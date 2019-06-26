/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 13/5/2019.
 */

package com.adyen.checkout.base.util;

import com.adyen.checkout.core.exeption.NoConstructorException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ResultCode {

    // Indicates the payment authorisation was successfully completed.
    public static final String AUTHORIZED = "Authorised";
    // Indicates the payment was refused. The reason is given in the refusalReason field. This is a final state.
    public static final String REFUSED = "Refused";
    // Indicates the shopper should be redirected to an external web page or app to complete the authorisation.
    public static final String REDIRECT = "RedirectShopper";
    // Indicates the payment has successfully been received by Adyen, and will be processed. This is the initial state for all payments.
    public static final String RECEIVED = "Received";
    // Indicates the payment has been cancelled (either by the shopper or the merchant) before processing was completed. Final state.
    public static final String CANCELED = "Cancelled";
    // Indicates that it is not possible to obtain the final status of the payment. For more information on handling a pending payment,
    // refer to Payments with pending status.
    public static final String PENDING = "Pending";
    // Indicates an error occurred during processing of the payment. The reason is given in the refusalReason field. This is a final state.
    public static final String ERROR = "Error";

    // 3DS2 related results
    // You are required to perform the 3D Secure 2 device fingerprinting.
    public static final String IDENTIFY_SHOPPER = "IdentifyShopper";
    // This means that the issuer would like to perform additional checks in order to verify that the shopper is indeed the cardholder.
    public static final String CHALLENGE_SHOPPER = "ChallengeShopper";

    // present to shopper -  WeChatPay QR Code



    public static final List<String> POSSIBLE_RESULTS;

    static {
        final String[] responses = {AUTHORIZED, REFUSED, REDIRECT, RECEIVED, CANCELED, PENDING, ERROR, IDENTIFY_SHOPPER, CHALLENGE_SHOPPER};
        POSSIBLE_RESULTS = Collections.unmodifiableList(Arrays.asList(responses));
    }

    private ResultCode() {
        throw new NoConstructorException();
    }
}
