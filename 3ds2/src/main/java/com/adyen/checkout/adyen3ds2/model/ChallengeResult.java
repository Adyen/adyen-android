/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/5/2019.
 */

package com.adyen.checkout.adyen3ds2.model;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.encoding.Base64Encoder;
import com.adyen.threeds2.CompletionEvent;

import org.json.JSONException;
import org.json.JSONObject;

public final class ChallengeResult {

    private static final String KEY_TRANSACTION_STATUS = "transStatus";
    private static final String VALUE_TRANSACTION_STATUS = "Y";

    private final boolean mIsAuthenticated;
    private final String mPayload;

    /**
     * Constructs the object base in the result from te 3DS2 SDK.
     *
     * @param completionEvent The result from the 3DS2 SDK.
     * @return The filled object with the content needed for the details response.
     * @throws JSONException In case parsing fails.
     */
    @NonNull
    public static ChallengeResult from(@NonNull CompletionEvent completionEvent) throws JSONException {
        final String transactionStatus = completionEvent.getTransactionStatus();

        final boolean isAuthenticated = VALUE_TRANSACTION_STATUS.equals(transactionStatus);

        final JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_TRANSACTION_STATUS, transactionStatus);

        final String payload = Base64Encoder.encode(jsonObject.toString());

        return new ChallengeResult(isAuthenticated, payload);
    }

    private ChallengeResult(boolean isAuthenticated, @NonNull String payload) {
        mIsAuthenticated = isAuthenticated;
        mPayload = payload;
    }

    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }

    @NonNull
    public String getPayload() {
        return mPayload;
    }
}
