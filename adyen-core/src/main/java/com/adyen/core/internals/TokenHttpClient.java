package com.adyen.core.internals;

import android.support.annotation.NonNull;

import com.adyen.core.interfaces.HttpResponseCallback;
import com.adyen.core.models.PaymentModule;
import com.adyen.core.utils.AsyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * HTTP Client for posting token information.
 */
public class TokenHttpClient extends HttpClient {

    private final Map<String, Object> paymentData;


    public TokenHttpClient(@NonNull Map<String, Object> paymentData) {
        this.paymentData = paymentData;
    }

    /**
     * Async POST of the payment token to the server's URL provided.
     *
     * @param url server URL
     * @param httpResponseCallback callback to receive the response or error
     */
    public void postPaymentToken(@NonNull final String url, final Map<String, String> headers,
                                 @NonNull final PaymentModule paymentModule,
                                 @NonNull final HttpResponseCallback httpResponseCallback) {
        try {
            final JSONObject paymentDataRequest = paymentDataToJsonRequest(paymentModule);
            AsyncHttpClient.post(url, headers, String.valueOf(paymentDataRequest),
                    httpResponseCallback);
        } catch (JSONException e) {
            httpResponseCallback.onFailure(e);
        }
    }

    @NonNull
    private JSONObject paymentDataToJsonRequest(@NonNull PaymentModule paymentModule)
            throws JSONException {
        JSONObject paymentDataJson = new JSONObject();

        JSONObject additionalDataJson = new JSONObject();
        additionalDataJson.put(paymentModule.toString() + ".token", String.valueOf(paymentData.get("token")));
        paymentDataJson.put("additionalData", additionalDataJson);

        JSONObject amountJson = new JSONObject();
        amountJson.put("currency", String.valueOf(paymentData.get("currency")));
        amountJson.put("value", Float.valueOf(String.valueOf(paymentData.get("amount"))));
        paymentDataJson.put("amount", amountJson);

        paymentDataJson.put("reference", String.valueOf(paymentData.get("reference")));

        return paymentDataJson;
    }

}
