/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/9/2020.
 */

package com.adyen.checkout.await.api;

import androidx.annotation.NonNull;

import com.adyen.checkout.await.model.StatusResponse;
import com.adyen.checkout.core.exception.NoConstructorException;

public final class StatusResponseUtils {

    public static final String RESULT_PENDING = "pending";
    public static final String RESULT_AUTHORIZED = "authorised";
    public static final String RESULT_REFUSED = "refused";
    public static final String RESULT_ERROR = "error";
    public static final String RESULT_CANCELED = "canceled";

    public static boolean isFinalResult(@NonNull StatusResponse statusResponse) {
        return !RESULT_PENDING.equals(statusResponse.getResultCode());
    }

    private StatusResponseUtils() {
        throw new NoConstructorException();
    }
}
