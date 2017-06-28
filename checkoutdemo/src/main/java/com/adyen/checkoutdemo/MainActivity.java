package com.adyen.checkoutdemo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import java.util.Locale;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String MERCHANT_SERVER_URL = "https://checkoutshopper-test.adyen.com/checkoutshopper/demo/easy-integration/"
            + "merchantserver/";
    private static final String SETUP = "setup";
    private static final String VERIFY = "verify";
    private static final String MERCHANT_API_SECRET_KEY = //YOUR_API_KEY

    private static final String MERCHANT_APP_ID = "TestMerchantApp";

    private PaymentRequest paymentRequest;
    private Context context;


    private final PaymentRequestListener paymentRequestListener = new PaymentRequestListener() {
        @Override
        public void onPaymentDataRequested(@NonNull PaymentRequest paymentRequest, @NonNull String token,
                                           @NonNull final PaymentDataCallback paymentDataCallback) {
            final Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; charset=UTF-8");
            headers.put("X-MerchantServer-App-SecretKey", MERCHANT_API_SECRET_KEY);
            headers.put("X-MerchantServer-App-Id", MERCHANT_APP_ID);

            AsyncHttpClient.post(MERCHANT_SERVER_URL + SETUP, headers, getSetupDataString(token), new HttpResponseCallback() {
                @Override
                public void onSuccess(final byte[] response) {
                    paymentDataCallback.completionWithPaymentData(response);
                }
                @Override
                public void onFailure(final Throwable e) {
                    Log.e(TAG, "HTTP Response problem: ", e);
                }
            });
        }

        @Override
        public void onPaymentResult(@NonNull PaymentRequest paymentRequest,
                                    @NonNull PaymentRequestResult paymentRequestResult) {
            if (paymentRequestResult.isProcessed() && (
                    paymentRequestResult.getPayment().getPaymentStatus() == Payment.PaymentStatus.AUTHORISED
                            || paymentRequestResult.getPayment().getPaymentStatus()
                            == Payment.PaymentStatus.RECEIVED)) {
                verifyPayment(paymentRequestResult.getPayment());
                Intent intent  = new Intent(context, SuccessActivity.class);
                startActivity(intent);
                finish();
            } else {
                Intent intent  = new Intent(context, FailureActivity.class);
                startActivity(intent);
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        context = this;
        setStatusBarTranslucent(true);

        Button checkoutButton = (Button) findViewById(R.id.checkout_button);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentRequest = new PaymentRequest(context, paymentRequestListener);
                paymentRequest.start();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    private String getSetupDataString(final String token) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("shopperLocale", "NL");
            jsonObject.put("token", token);

            jsonObject.put("appUrlScheme", "example-shopping-app://");
            jsonObject.put("customerCountry", "NL");
            jsonObject.put("currency", "USD");
            jsonObject.put("quantity", "17408");
            jsonObject.put("platform", "android");
            jsonObject.put("basketId", "M+M Black dress & accessories");
            jsonObject.put("customerId", "emred");
        } catch (final JSONException jsonException) {
            Log.e("Unexpected error", "Setup failed");
        }
        return jsonObject.toString();
    }

    @TargetApi(19)
    protected void setStatusBarTranslucent(boolean makeTranslucent) {
        View v = findViewById(R.id.activity_main);
        if (v != null) {
            int paddingTop = 0;
            TypedValue tv = new TypedValue();
            getTheme().resolveAttribute(0, tv, true);
            paddingTop += TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            v.setPadding(0, makeTranslucent ? paddingTop : 0, 0, 0);
        }

        if (makeTranslucent) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
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
        headers.put("X-MerchantServer-App-SecretKey", MERCHANT_API_SECRET_KEY);
        headers.put("X-MerchantServer-App-Id", MERCHANT_APP_ID);
        AsyncHttpClient.post(MERCHANT_SERVER_URL + VERIFY, headers, verifyString, new HttpResponseCallback() {
            String resultString = "";
            @Override
            public void onSuccess(final byte[] response) {
                try {
                    JSONObject jsonVerifyResponse = new JSONObject(new String(response, Charset.forName("UTF-8")));
                    String authResponse = jsonVerifyResponse.getString("authResponse");
                    if (authResponse.equalsIgnoreCase(payment.getPaymentStatus().toString())) {
                        resultString = "Payment is " + payment.getPaymentStatus().toString().toLowerCase(Locale.getDefault()) + " and verified.";
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
