/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 10/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.model.GiroPayIssuer;

import org.json.JSONException;
import org.json.JSONObject;

public final class GiroPayIssuerImpl extends JsonObject implements GiroPayIssuer {
    @NonNull
    public static final Parcelable.Creator<GiroPayIssuerImpl> CREATOR = new DefaultCreator<>(GiroPayIssuerImpl.class);

    private static final String KEY_BANK_NAME = "bankName";

    private static final String KEY_BIC = "bic";

    private static final String KEY_BLZ = "blz";

    private String mBankName;

    private String mBic;

    private String mBlz;

    private GiroPayIssuerImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mBankName = jsonObject.getString(KEY_BANK_NAME);
        mBic = jsonObject.getString(KEY_BIC);
        mBlz = jsonObject.getString(KEY_BLZ);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GiroPayIssuerImpl that = (GiroPayIssuerImpl) o;

        if (mBankName != null ? !mBankName.equals(that.mBankName) : that.mBankName != null) {
            return false;
        }
        if (mBic != null ? !mBic.equals(that.mBic) : that.mBic != null) {
            return false;
        }
        return mBlz != null ? mBlz.equals(that.mBlz) : that.mBlz == null;
    }

    @Override
    public int hashCode() {
        int result = mBankName != null ? mBankName.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mBic != null ? mBic.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mBlz != null ? mBlz.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String getBankName() {
        return mBankName;
    }

    @NonNull
    @Override
    public String getBic() {
        return mBic;
    }

    @NonNull
    @Override
    public String getBlz() {
        return mBlz;
    }
}
