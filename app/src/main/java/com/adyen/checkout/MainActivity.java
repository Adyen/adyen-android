package com.adyen.checkout;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.adyen.core.PaymentRequest;
import com.adyen.core.interfaces.HttpResponseCallback;
import com.adyen.core.interfaces.PaymentDataCallback;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.models.Payment;
import com.adyen.core.models.PaymentRequestResult;
import com.adyen.core.utils.AsyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Main activity for demonstrating how to use Checkout SDK. Client should implement an activity
 * similar to this.
 *
 * In this example application, the payment UI is completely handled by SDK. This is the simplest way
 * to integrate.
 */
public class MainActivity extends FragmentActivity implements PaymentDataEntryFragment.PaymentRequestListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PaymentSetupRequest paymentSetupRequest;

    private static final String SETUP = "setup";
    private static final String VERIFY = "verify";

    private String merchantServerUrl = ""; // Add the URL for your server here
    private String merchantApiSecretKey = ""; // Add the api secret key for your server here
    private String merchantApiHeaderKey = ""; // Add the header key for api secret key here

    private PaymentRequest paymentRequest;
    private final PaymentRequestListener paymentRequestListener = new PaymentRequestListener() {

        @Override
        public void onPaymentDataRequested(@NonNull final PaymentRequest request, @NonNull String token,
                                           @NonNull final PaymentDataCallback callback) {
            if (paymentRequest != request) {
                Log.d(TAG, "onPaymentDataRequested(): This is not the payment request that we created.");
                return;
            }

            final Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; charset=UTF-8");
            headers.put(merchantApiHeaderKey, merchantApiSecretKey);

            AsyncHttpClient.post(merchantServerUrl + SETUP, headers, getSetupDataString(token), new HttpResponseCallback() {
                @Override
                public void onSuccess(final byte[] response) {
                    callback.completionWithPaymentData(response);
                }

                @Override
                public void onFailure(final Throwable e) {
                    Log.e(TAG, "HTTP Response problem: ", e);
                    paymentRequest.cancel();
                }
            });
        }

        @Override
        public void onPaymentResult(@NonNull PaymentRequest request,
                                    @NonNull PaymentRequestResult paymentResult) {
            if (paymentRequest != request) {
                Log.d(TAG, "onPaymentResult(): This is not the payment request that we created.");
                return;
            }
            Log.d(TAG, "paymentRequestListener.onPaymentResult() -> " + request);
            String resultString;
            if (paymentResult.isProcessed()) {
                resultString = paymentResult.getPayment().getPaymentStatus().toString();
                verifyPayment(paymentResult.getPayment());
            } else {
                resultString = paymentResult.getError().toString();
            }

            final Intent intent = new Intent(getApplicationContext(), PaymentResultActivity.class);
            intent.putExtra("Result", resultString);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PaymentDataEntryFragment paymentDataEntryFragment = new PaymentDataEntryFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                paymentDataEntryFragment).commitAllowingStateLoss();
    }

    @Override
    public void onPaymentRequested(final PaymentSetupRequest paymentSetupRequest) {
        Log.d(TAG, "onPaymentRequested");
        merchantServerUrl = TextUtils.isEmpty(merchantServerUrl) ? BuildConfig.SERVER_URL : merchantServerUrl;
        merchantApiSecretKey = TextUtils.isEmpty(merchantApiSecretKey) ? BuildConfig.API_KEY : merchantApiSecretKey;
        merchantApiHeaderKey = TextUtils.isEmpty(merchantApiHeaderKey) ? BuildConfig.API_HEADER_KEY : merchantApiHeaderKey;

        if (TextUtils.isEmpty(merchantApiSecretKey) || TextUtils.isEmpty(merchantApiHeaderKey) || TextUtils.isEmpty(merchantServerUrl)) {
            Toast.makeText(getApplicationContext(), "Server parameters have not been configured correctly", Toast.LENGTH_SHORT).show();
            return;
        }
        this.paymentSetupRequest = paymentSetupRequest;
        if (paymentRequest != null) {
            paymentRequest.cancel();
        }
        paymentRequest = new PaymentRequest(this, paymentRequestListener);
        paymentRequest.start();
    }

    private String getSetupDataString(final String token) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("merchantAccount", paymentSetupRequest.getMerchantAccount()); // Not required when communicating with merchant server
            jsonObject.put("shopperLocale", paymentSetupRequest.getShopperLocale());
            jsonObject.put("token", token);
            jsonObject.put("returnUrl", "example-shopping-app://");
            jsonObject.put("countryCode", paymentSetupRequest.getCountryCode());
            final JSONObject amount = new JSONObject();
            amount.put("value", paymentSetupRequest.getAmount().getValue());
            amount.put("currency", paymentSetupRequest.getAmount().getCurrency());
            jsonObject.put("amount", amount);
            jsonObject.put("channel", "android");
            jsonObject.put("reference", "Android Checkout SDK Payment: " + System.currentTimeMillis());
            jsonObject.put("shopperReference", "example-customer@exampleprovider");
            try {
                short maxNumberOfInstallments = Short.parseShort(paymentSetupRequest.getMaxNumberOfInstallments());
                if (maxNumberOfInstallments > 1) {
                    final JSONObject configuration = new JSONObject();
                    final JSONObject installments = new JSONObject();
                    installments.put("maxNumberOfInstallments", maxNumberOfInstallments);
                    configuration.put("installments", installments);
                    jsonObject.put("configuration", configuration);
                }
            } catch (final NumberFormatException numberFormatException) {
                Log.w(TAG, "Invalid value for maximum number of installments", numberFormatException);
            }
        } catch (final JSONException jsonException) {
            Log.e(TAG, "Setup failed", jsonException);
        }
        return jsonObject.toString();
    }

    private void verifyPayment(final Payment payment) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("payload", payment.getPayload());
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to verify payment.", Toast.LENGTH_LONG).show();
            return;
        }
        String verifyString = jsonObject.toString();

        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        headers.put(merchantApiHeaderKey, merchantApiSecretKey);

        AsyncHttpClient.post(merchantServerUrl + VERIFY, headers, verifyString, new HttpResponseCallback() {
            String resultString = "";
            @Override
            public void onSuccess(final byte[] response) {
                try {
                    JSONObject jsonVerifyResponse = new JSONObject(new String(response, Charset.forName("UTF-8")));
                    String authResponse = jsonVerifyResponse.getString("authResponse");
                    if (authResponse.equalsIgnoreCase(payment.getPaymentStatus().toString())) {
                        resultString = "Payment is " + payment.getPaymentStatus().toString().toLowerCase() + " and verified.";
                    } else {
                        resultString = "Failed to verify payment.";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    resultString = "Failed to verify payment.";
                }
                Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(final Throwable e) {
                Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_LONG).show();
            }
        });
    }

}
