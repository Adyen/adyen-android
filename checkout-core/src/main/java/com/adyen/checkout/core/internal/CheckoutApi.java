package com.adyen.checkout.core.internal;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Api;
import com.adyen.checkout.base.internal.Json;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.JsonSerializable;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.internal.model.GiroPayConfiguration;
import com.adyen.checkout.core.internal.model.GiroPayIssuersResponse;
import com.adyen.checkout.core.internal.model.PaymentInitiation;
import com.adyen.checkout.core.internal.model.PaymentInitiationResponse;
import com.adyen.checkout.core.internal.model.PaymentMethodDeletion;
import com.adyen.checkout.core.internal.model.PaymentMethodDeletionResponse;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.internal.model.PaymentSessionImpl;
import com.adyen.checkout.core.model.PaymentMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 08/09/2017.
 */
public final class CheckoutApi extends Api {
    private static CheckoutApi sInstance;

    @NonNull
    public static synchronized CheckoutApi getInstance(@NonNull Application application) {
        if (sInstance == null) {
            sInstance = new CheckoutApi(application);
        }

        return sInstance;
    }

    private CheckoutApi(@NonNull Application application) {
    }

    @NonNull
    public Callable<PaymentInitiationResponse> initiatePayment(
            @NonNull final PaymentSessionImpl paymentSession,
            @NonNull final PaymentInitiation paymentInitiation
    ) {
        return new Callable<PaymentInitiationResponse>() {
            @Override
            public PaymentInitiationResponse call() throws Exception {
                JSONObject response = post(paymentSession.getInitiationUrl(), paymentInitiation);

                return JsonObject.parseFrom(response, PaymentInitiationResponse.class);
            }
        };
    }

    @NonNull
    public Callable<PaymentMethodDeletionResponse> deletePaymentMethod(
            @NonNull final PaymentSessionImpl paymentSession,
            @NonNull PaymentMethodImpl paymentMethod
    ) {
        String paymentData = paymentSession.getPaymentData();
        String paymentMethodData = paymentMethod.getPaymentMethodData();
        final PaymentMethodDeletion paymentMethodDeletion = new PaymentMethodDeletion.Builder(paymentData, paymentMethodData).build();

        return new Callable<PaymentMethodDeletionResponse>() {
            @Override
            public PaymentMethodDeletionResponse call() throws Exception {
                JSONObject response = post(paymentSession.getDisableRecurringDetailUrl(), paymentMethodDeletion);

                try {
                    // TODO: 15/05/2018 Remove when backend does not return PaymentInitiationResponse.ErrorFields for this request.
                    PaymentInitiationResponse.ErrorFields error = JsonObject.parseFrom(response, PaymentInitiationResponse.ErrorFields.class);

                    throw new CheckoutException.Builder(error.getErrorMessage(), null)
                            .setPayload(error.getPayload())
                            .setFatal(error.getErrorCode() == PaymentInitiationResponse.ErrorCode.PAYMENT_SESSION_EXPIRED)
                            .build();
                } catch (JSONException e) {
                    return JsonObject.parseFrom(response, PaymentMethodDeletionResponse.class);
                }
            }
        };
    }

    @NonNull
    public Callable<GiroPayIssuersResponse> getGiroPayIssuers(@NonNull final PaymentMethod giroPayPaymentMethod, @NonNull final String searchString) {
        return new Callable<GiroPayIssuersResponse>() {
            @Override
            public GiroPayIssuersResponse call() throws Exception {
                GiroPayConfiguration configuration = giroPayPaymentMethod.getConfiguration(GiroPayConfiguration.class);
                String issuersUrl = configuration.getIssuersUrl();
                JsonSerializable body = new JsonSerializable() {
                    @NonNull
                    @Override
                    public JSONObject serialize() throws JSONException {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("searchString", searchString);

                        return jsonObject;
                    }
                };
                JSONObject response = post(issuersUrl, body);

                return JsonObject.parseFrom(response, GiroPayIssuersResponse.class);
            }
        };
    }

    @NonNull
    protected IOException parseException(@Nullable byte[] errorBytes) {
        if (errorBytes != null) {
            try {
                String errorJson = new String(errorBytes, CHARSET);
                new JSONObject(errorJson);

                // TODO: 15/05/2018 If we receive JSON, handle it.
                throw new RuntimeException("Received JSON error response: " + errorJson);
            } catch (JSONException e) {
                // errorBytes is not a valid JSON object, let super handle it.
            }
        }

        return super.parseException(errorBytes);
    }

    @NonNull
    private JSONObject post(@NonNull String url, @NonNull JsonSerializable jsonSerializable) throws IOException {
        try {
            byte[] response = super.post(url, Json.getDefaultHeaders(), jsonSerializable.serialize().toString().getBytes(CHARSET));
            String responseJson = new String(response, CHARSET);

            return new JSONObject(responseJson);
        } catch (JSONException e) {
            throw new RuntimeException("Error serializing " + jsonSerializable.getClass().getSimpleName(), e);
        }
    }
}
