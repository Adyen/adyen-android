/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 16/11/2018.
 */

package com.adyen.checkout.threeds.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonDecodable;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("AbbreviationAsWordInName")
public final class FingerprintToken extends JsonDecodable {
    public static final Parcelable.Creator<FingerprintToken> CREATOR = new DefaultCreator<>(FingerprintToken.class);

    private static final String KEY_THREE_DS_SERVER_TRANSACTION_ID = "threeDSServerTransID";

    private static final String KEY_DIRECTORY_SERVER_ID = "directoryServerId";

    private static final String KEY_DIRECTORY_SERVER_PUBLIC_KEY = "directoryServerPublicKey";

    private final String mThreeDSServerTransID;

    private final String mDirectoryServerId;

    private final String mDirectoryServerPublicKey;

    public FingerprintToken(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mThreeDSServerTransID = jsonObject.getString(KEY_THREE_DS_SERVER_TRANSACTION_ID);
        mDirectoryServerId = jsonObject.getString(KEY_DIRECTORY_SERVER_ID);
        mDirectoryServerPublicKey = jsonObject.getString(KEY_DIRECTORY_SERVER_PUBLIC_KEY);
    }

    @NonNull
    public String getThreeDSServerTransID() {
        return mThreeDSServerTransID;
    }

    @NonNull
    public String getDirectoryServerId() {
        return mDirectoryServerId;
    }

    @NonNull
    public String getDirectoryServerPublicKey() {
        return mDirectoryServerPublicKey;
    }
}
