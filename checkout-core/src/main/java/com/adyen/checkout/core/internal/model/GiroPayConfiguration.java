/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 04/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.Configuration;

import org.json.JSONException;
import org.json.JSONObject;

@ProvidedBy(GiroPayConfiguration.class)
public final class GiroPayConfiguration extends JsonObject implements Configuration {
    @NonNull
    public static final Parcelable.Creator<GiroPayConfiguration> CREATOR = new DefaultCreator<>(GiroPayConfiguration.class);

    private final String mIssuersUrl;

    private GiroPayConfiguration(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mIssuersUrl = jsonObject.getString("giroPayIssuersUrl");
    }

    @NonNull
    public String getIssuersUrl() {
        return mIssuersUrl;
    }
}
