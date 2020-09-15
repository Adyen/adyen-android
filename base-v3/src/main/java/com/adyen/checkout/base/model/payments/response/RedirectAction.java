/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class RedirectAction extends Action {
    @NonNull
    public static final Creator<RedirectAction> CREATOR = new Creator<>(RedirectAction.class);

    public static final String ACTION_TYPE = ActionTypes.REDIRECT;

    private static final String METHOD = "method";
    private static final String URL = "url";

    @NonNull
    public static final Serializer<RedirectAction> SERIALIZER = new Serializer<RedirectAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull RedirectAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                jsonObject.putOpt(METHOD, modelObject.getMethod());
                jsonObject.putOpt(URL, modelObject.getUrl());
            } catch (JSONException e) {
                throw new ModelSerializationException(RedirectAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public RedirectAction deserialize(@NonNull JSONObject jsonObject) {
            final RedirectAction redirectAction = new RedirectAction();

            // getting parameters from parent class
            redirectAction.setType(jsonObject.optString(Action.TYPE, null));
            redirectAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            redirectAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            redirectAction.setMethod(jsonObject.optString(METHOD, null));
            redirectAction.setUrl(jsonObject.optString(URL, null));
            return redirectAction;
        }
    };

    private String method;
    private String url;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getMethod() {
        return method;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setMethod(@Nullable String method) {
        this.method = method;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }
}
