/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 20/11/2018.
 */

package com.adyen.checkout.threeds.internal;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.Base64Coder;
import com.adyen.checkout.threeds.ChallengeResult;
import com.adyen.threeds2.CompletionEvent;

import org.json.JSONException;
import org.json.JSONObject;

public final class ChallengeResultImpl implements ChallengeResult {

    private static final String KEY_TRANSACTION_STATUS = "transStatus";

    private static final String VALUE_TRANSACTION_STATUS = "Y";

    private final boolean mIsAuthenticated;

    private final String mPayload;

    @NonNull
    public static ChallengeResult from(@NonNull CompletionEvent completionEvent) throws JSONException {
        String transactionStatus = completionEvent.getTransactionStatus();

        boolean isAuthenticated = VALUE_TRANSACTION_STATUS.equals(transactionStatus);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_TRANSACTION_STATUS, transactionStatus);

        String payload = Base64Coder.encodeToString(jsonObject);

        return new ChallengeResultImpl(isAuthenticated, payload);
    }

    private ChallengeResultImpl(boolean isAuthenticated, @NonNull String payload) {
        mIsAuthenticated = isAuthenticated;
        mPayload = payload;
    }

    @Override
    public boolean isAuthenticated() {
        return mIsAuthenticated;
    }

    @NonNull
    @Override
    public String getPayload() {
        return mPayload;
    }
}
