/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/5/2019.
 */

package com.adyen.checkout.adyen3ds2.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass", "AbbreviationAsWordInName"})
public class FingerprintToken extends ModelObject {
    @NonNull
    public static final Creator<FingerprintToken> CREATOR = new Creator<>(FingerprintToken.class);

    private static final String DIRECTORY_SERVER_ID = "directoryServerId";
    private static final String DIRECTORY_SERVER_PUBLIC_KEY = "directoryServerPublicKey";
    private static final String THREEDS_SERVER_TRANS_ID = "threeDSServerTransID";

    @NonNull
    public static final Serializer<FingerprintToken> SERIALIZER = new Serializer<FingerprintToken>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull FingerprintToken modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(DIRECTORY_SERVER_ID, modelObject.getDirectoryServerId());
                jsonObject.putOpt(DIRECTORY_SERVER_PUBLIC_KEY, modelObject.getDirectoryServerPublicKey());
                jsonObject.putOpt(THREEDS_SERVER_TRANS_ID, modelObject.getThreeDSServerTransID());

            } catch (JSONException e) {
                throw new ModelSerializationException(FingerprintToken.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public FingerprintToken deserialize(@NonNull JSONObject jsonObject) {
            final FingerprintToken fingerprintToken = new FingerprintToken();
            fingerprintToken.setDirectoryServerId(jsonObject.optString(DIRECTORY_SERVER_ID, null));
            fingerprintToken.setDirectoryServerPublicKey(jsonObject.optString(DIRECTORY_SERVER_PUBLIC_KEY, null));
            fingerprintToken.setThreeDSServerTransID(jsonObject.optString(THREEDS_SERVER_TRANS_ID, null));
            return fingerprintToken;
        }
    };

    private String directoryServerId;
    private String directoryServerPublicKey;
    private String threeDSServerTransID;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getDirectoryServerId() {
        return directoryServerId;
    }

    public void setDirectoryServerId(@Nullable String directoryServerId) {
        this.directoryServerId = directoryServerId;
    }

    @Nullable
    public String getDirectoryServerPublicKey() {
        return directoryServerPublicKey;
    }

    public void setDirectoryServerPublicKey(@Nullable String directoryServerPublicKey) {
        this.directoryServerPublicKey = directoryServerPublicKey;
    }

    @Nullable
    public String getThreeDSServerTransID() {
        return threeDSServerTransID;
    }

    public void setThreeDSServerTransID(@Nullable String threeDSServerTransID) {
        this.threeDSServerTransID = threeDSServerTransID;
    }
}
