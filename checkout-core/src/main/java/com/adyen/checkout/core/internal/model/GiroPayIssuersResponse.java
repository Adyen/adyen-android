/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/11/2017.
 */

package com.adyen.checkout.core.internal.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class GiroPayIssuersResponse extends JsonObject {
    @NonNull
    public static final Creator<GiroPayIssuersResponse> CREATOR = new DefaultCreator<>(GiroPayIssuersResponse.class);

    private final List<GiroPayIssuerImpl> mGiroPayIssuers;

    private GiroPayIssuersResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mGiroPayIssuers = parseList("giroPayIssuers", GiroPayIssuerImpl.class);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GiroPayIssuersResponse that = (GiroPayIssuersResponse) o;

        return mGiroPayIssuers != null ? mGiroPayIssuers.equals(that.mGiroPayIssuers) : that.mGiroPayIssuers == null;
    }

    @Override
    public int hashCode() {
        return mGiroPayIssuers != null ? mGiroPayIssuers.hashCode() : 0;
    }

    @NonNull
    public List<GiroPayIssuerImpl> getGiroPayIssuers() {
        return new ArrayList<>(mGiroPayIssuers);
    }
}
