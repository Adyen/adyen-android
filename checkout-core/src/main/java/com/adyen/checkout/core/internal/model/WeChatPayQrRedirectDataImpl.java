package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.WeChatPayQrRedirectData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 10/07/2018.
 */
public final class WeChatPayQrRedirectDataImpl extends JsonObject implements WeChatPayQrRedirectData {
    public static final Parcelable.Creator<WeChatPayQrRedirectDataImpl> CREATOR = new DefaultCreator<>(WeChatPayQrRedirectDataImpl.class);

    private final String mQrCodeImage;

    private final String mQrCodeUrl;

    private WeChatPayQrRedirectDataImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mQrCodeImage = jsonObject.getString("qrCodeImage");
        mQrCodeUrl = jsonObject.getString("qrCodeUrl");
    }

    @NonNull
    @Override
    public String getQrCodeUrl() {
        return mQrCodeImage;
    }

    @NonNull
    @Override
    public String getRedirectTargetUrl() {
        return mQrCodeUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WeChatPayQrRedirectDataImpl that = (WeChatPayQrRedirectDataImpl) o;

        if (mQrCodeImage != null ? !mQrCodeImage.equals(that.mQrCodeImage) : that.mQrCodeImage != null) {
            return false;
        }
        return mQrCodeUrl != null ? mQrCodeUrl.equals(that.mQrCodeUrl) : that.mQrCodeUrl == null;
    }

    @Override
    public int hashCode() {
        int result = mQrCodeImage != null ? mQrCodeImage.hashCode() : 0;
        result = 31 * result + (mQrCodeUrl != null ? mQrCodeUrl.hashCode() : 0);
        return result;
    }
}
