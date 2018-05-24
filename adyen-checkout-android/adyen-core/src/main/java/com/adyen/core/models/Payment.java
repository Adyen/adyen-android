package com.adyen.core.models;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.adyen.core.models.Payment.PaymentStatus.ERROR;

/**
 * The class to keep the payment result.
 */

public class Payment {

    private static final String TAG  = Payment.class.getSimpleName();

    private PaymentStatus paymentStatus;
    private String payload;

    /**
     * Represents the payment status.
     */
    public enum PaymentStatus {
        RECEIVED("Received"),
        AUTHORISED("Authorised"),
        ERROR("Error"),
        REFUSED("Refused"),
        CANCELLED("Cancelled");

        private String status;

        PaymentStatus(String statusString) {
            this.status = statusString;
        }

        static PaymentStatus getPaymentStatus(final String string) {
            for (PaymentStatus status : PaymentStatus.values()) {
                if (status.status.equalsIgnoreCase(string)) {
                    return status;
                }
            }
            return null;
        }

        @Override
        public String toString() {
            return status;
        }
    }

    /**
     * The constructor method to be used when a JSONObject is received from a server as the payment result.
     * @param jsonObject JSONObject that contains the payment result.
     */
    public Payment(@NonNull JSONObject jsonObject) {
        try {
            paymentStatus = PaymentStatus.getPaymentStatus(jsonObject.getString("resultCode"));
            payload = jsonObject.getString("payload");
        } catch (final JSONException jsonException) {
            Log.e(TAG, "Payment result code cannot be resolved.");
            paymentStatus = ERROR;
        }
    }

    /**
     * The constructor method to be used when a return URI is received from the server.
     * This is required if a payment method requires redirection.
     *
     * @param uri Return URI which is received from the server.
     */
    public Payment(@NonNull Uri uri) {
        Log.d(TAG, "URI: " + uri);
        paymentStatus = PaymentStatus.getPaymentStatus(uri.getQueryParameter("resultCode"));
        payload = uri.getQueryParameter("payload");
    }

    /**
     * Get {@link PaymentStatus} for this payment.
     * @return PaymentStatus.
     */
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    /**
     * Get the payment result payload.
     * This payload should be used to verify the payment with the server.
     * @return Payload.
     */
    public String getPayload() {
        return payload;
    }

}
