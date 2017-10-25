package com.adyen.androidpay;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adyen.androidpay.exceptions.GoogleApiClientNotInitializedException;
import com.adyen.androidpay.ui.AndroidPayActivity;
import com.adyen.core.PaymentRequest;
import com.adyen.core.exceptions.PostPaymentDataException;
import com.adyen.core.exceptions.PostResponseFormatException;
import com.adyen.core.interfaces.PaymentMethodAvailabilityCallback;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.services.PaymentMethodService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.Wallet;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * {@link PaymentMethodService} implementation for AndroidPay module.
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class AndroidPayService implements PaymentMethodService, GoogleApiClient.ConnectionCallbacks {

    private static final String TAG = AndroidPayService.class.getSimpleName();

    @Override
    @SuppressWarnings("unchecked")
    public void checkAvailability(@NonNull final Context context, @NonNull PaymentMethod paymentMethod,
                                  @NonNull final PaymentMethodAvailabilityCallback callback) {
        Log.d(TAG, "checkAvailability");

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);

        if (result != ConnectionResult.SUCCESS) {
            callback.onFail(new GoogleApiClientNotInitializedException("Google API not available"));
        }
        final GoogleApiClient googleApiClient = getGoogleApiClient(context);

        if (googleApiClient != null) {
            googleApiClient.connect();
            Wallet.Payments.isReadyToPay(googleApiClient).setResultCallback(
                    new ResultCallback<BooleanResult>() {
                        @Override
                        public void onResult(@NonNull BooleanResult booleanResult) {
                            if (booleanResult.getStatus().isSuccess()) {
                                callback.onSuccess(booleanResult.getValue());
                            } else {
                                Log.e(TAG, "isReadyToPay:" + booleanResult.getStatus());
                                String errorMessage = booleanResult.getStatus().getStatusCode()
                                        + booleanResult.getStatus().getStatusMessage();
                                callback.onFail(new Throwable(errorMessage));
                            }
                        }
                    });
        } else {
            callback.onFail(new GoogleApiClientNotInitializedException(
                    "Google API client is null or not connected"));
        }
    }

    /**
     * Return the Google API Client.
     *
     * @return {@link GoogleApiClient}
     */
    @NonNull
    private GoogleApiClient getGoogleApiClient(final Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder().build())
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void process(@NonNull final Context context, @NonNull final PaymentRequest paymentRequest,
                        @NonNull final PaymentRequestListener paymentRequestListener,
                        @Nullable final PaymentRequestDetailsListener paymentRequestDetailsListener) {
        PaymentMethod paymentMethod = paymentRequest.getPaymentMethod();
        Intent intent = new Intent(context, AndroidPayActivity.class);
        intent.putExtra("amount", paymentRequest.getAmount());
        intent.putExtra("publicKey", paymentMethod.getConfiguration().getPublicKey());
        intent.putExtra("merchantName", paymentMethod.getConfiguration().getMerchantName());
        intent.putExtra("environment", paymentMethod.getConfiguration().getEnvironment());
        context.startActivity(intent);
    }

    @Override
    public void getPaymentDetails(@NonNull Map<String, Object> paymentData,
                                  @NonNull PaymentRequest paymentRequest,
                                  @NonNull final PaymentDetailsListener paymentDetailsListener) {
//        postTokenToMerchantServer(paymentData, configuration, new HttpResponseCallback() {
//            @Override
//            public void onSuccess(@NonNull byte[] response) {
//                try {
//                    paymentDetailsListener.onPaymentDetails(processPostTokenResponse(new String(response)));
//                } catch (@NonNull JSONException|PostPaymentDataException
//                        |PostResponseFormatException e) {
//                    Log.e(TAG, e.getMessage(), e);
//                    paymentDetailsListener.onPaymentDetailsError(e);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Throwable e) {
//                Log.e(TAG, e.getMessage(), e);
//                paymentDetailsListener.onPaymentDetailsError(e);
//            }
//        });
    }

    private String processPostTokenResponse(String response) throws JSONException,
            PostPaymentDataException, PostResponseFormatException {
        JSONObject responseJson = new JSONObject(response);
        if (!responseJson.isNull("errorCode")) {
            throw new PostPaymentDataException(String.format("%s: %s",
                    responseJson.getString("errorCode"), responseJson.getString("message")));
        }

        if (responseJson.isNull("resultCode")) {
            throw new PostResponseFormatException("Invalid response format");
        }

        return responseJson.getString("resultCode");
    }

    @Override
    public void onConnected(@Nullable final Bundle bundle) {
        Log.i(TAG, "onConnected: Google API client is connected");
    }

    @Override
    public void onConnectionSuspended(final int i) {
        Log.i(TAG, "onConnected: Google API client is suspended");

    }
}
