package com.adyen.customuiapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.adyen.core.PaymentRequest;
import com.adyen.core.interfaces.HttpResponseCallback;
import com.adyen.core.interfaces.PaymentDataCallback;
import com.adyen.core.interfaces.PaymentDetailsCallback;
import com.adyen.core.interfaces.PaymentMethodCallback;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.interfaces.UriCallback;
import com.adyen.core.models.Issuer;
import com.adyen.core.models.Payment;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentRequestResult;
import com.adyen.core.models.paymentdetails.IdealPaymentDetails;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.utils.AsyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Main activity for demonstrating how to use Checkout SDK. Client should implement an activity
 * similar to this.
 * In this sample application, UI is completely custom made. This example application does not
 * include wallet payment methods (samsungpay and android pay) in order to keep it simple.
 */
public class MainActivity extends FragmentActivity implements
        PaymentDataEntryFragment.PaymentRequestListener,
        PaymentMethodSelectionFragment.PaymentMethodSelectionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PaymentSetupRequest paymentSetupRequest;

    private final List<PaymentMethod> availablePaymentMethods = new CopyOnWriteArrayList<>();
    private final List<PaymentMethod> preferredPaymentMethods = new CopyOnWriteArrayList<>();

    private static final String MERCHANT_SERVER_URL = "https://checkoutshopper-test.adyen.com/checkoutshopper/demo/easy-integration/merchantserver/";

    private static final String SETUP = "setup";
    private static final String VERIFY = "verify";

    private static final String MERCHANT_API_SECRET_KEY = //YOUR_API_KEY
    private static final String MERCHANT_APP_ID = "TestMerchantApp";

    private PaymentMethodCallback paymentMethodCallback;
    private Context context;
    private UriCallback uriCallback;

    private PaymentRequest paymentRequest;

    private PaymentRequestDetailsListener paymentRequestDetailsListener = new PaymentRequestDetailsListener() {
        @Override
        public void onPaymentMethodSelectionRequired(@NonNull final PaymentRequest paymentRequest,
                                                     final List<PaymentMethod> recurringMethods,
                                                     @NonNull final List<PaymentMethod> otherMethods,
                                                     @NonNull final PaymentMethodCallback callback) {
            Log.d(TAG, "paymentRequestDetailsListener.onPaymentMethodSelectionRequired");
            paymentMethodCallback = callback;
            preferredPaymentMethods.clear();
            preferredPaymentMethods.addAll(recurringMethods);
            availablePaymentMethods.clear();
            availablePaymentMethods.addAll(otherMethods);
            final PaymentMethodSelectionFragment paymentMethodSelectionFragment
                    = new PaymentMethodSelectionFragment();
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                    paymentMethodSelectionFragment).addToBackStack(null).commitAllowingStateLoss();
        }

        @Override
        public void onRedirectRequired(@NonNull final PaymentRequest paymentRequest, final String redirectUrl,
                                       @NonNull final UriCallback returnUriCallback) {
            Log.d(TAG, "paymentRequestDetailsListener.onRedirectRequired(): " + redirectUrl);
            uriCallback = returnUriCallback;
            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.launchUrl(context, Uri.parse(redirectUrl));
        }

        @Override
        public void onPaymentDetailsRequired(@NonNull final PaymentRequest paymentRequest,
                                             @NonNull final Map<String, Object> requiredFields,
                                             @NonNull final PaymentDetailsCallback callback) {
            Log.d(TAG, "paymentRequestDetailsListener.onPaymentDetailsRequired()");
            final String paymentMethodType = paymentRequest.getPaymentMethod().getType();
            if ("card".equals(paymentMethodType)) {
                final CreditCardFragment creditCardFragment = new CreditCardFragment();
                final Bundle bundle = new Bundle();
                //ONE CLICK CHECK
                if (requiredFields.containsKey("cardDetails.cvc")) {
                    bundle.putBoolean("oneClick", true);
                }
                creditCardFragment.setCreditCardInfoListener(new CreditCardFragment.CreditCardInfoListener() {
                    @Override
                    public void onCreditCardInfoProvided(PaymentDetails paymentDetails) {
                        callback.completionWithPaymentDetails(paymentDetails);
                    }
                });
                bundle.putString("public_key", paymentRequest.getPublicKey());
                bundle.putString("generation_time", paymentRequest.getGenerationTime());
                creditCardFragment.setArguments(bundle);

                getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                        creditCardFragment).addToBackStack(null).commitAllowingStateLoss();
            } else if ("ideal".equals(paymentMethodType)) {
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                final List<Issuer> issuers = paymentRequest.getPaymentMethod().getIssuers();
                final IssuerListAdapter issuerListAdapter = new IssuerListAdapter(MainActivity.this, issuers);
                alertDialog.setSingleChoiceItems(issuerListAdapter, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull final DialogInterface dialogInterface, final int i) {
                        final Issuer selectedIssuer = issuers.get(i);
                        dialogInterface.dismiss();
                        callback.completionWithPaymentDetails(new IdealPaymentDetails(selectedIssuer));
                    }
                });
                alertDialog.show();
            } else {
                    Log.w(TAG, "UI for " + paymentMethodType + " has not been implemented.");
                    paymentRequest.cancel();
            }
        }
    };

    private final PaymentRequestListener paymentRequestListener = new PaymentRequestListener() {
        @Override
        public void onPaymentDataRequested(@NonNull final PaymentRequest paymentRequest, @NonNull String token,
                                           @NonNull final PaymentDataCallback callback) {
            Log.d(TAG, "paymentRequestListener.provideSetupData()");
            final Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json; charset=UTF-8");
            headers.put("X-MerchantServer-App-SecretKey", MERCHANT_API_SECRET_KEY);
            headers.put("X-MerchantServer-App-Id", MERCHANT_APP_ID);

            AsyncHttpClient.post(MERCHANT_SERVER_URL + SETUP, headers, getSetupDataString(token), new HttpResponseCallback() {
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
        public void onPaymentResult(@NonNull PaymentRequest paymentRequest,
                                    @NonNull PaymentRequestResult paymentResult) {
            Log.d(TAG, "paymentRequestListener.onPaymentResult()");
            final String resultString;
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
        Log.d(TAG, "onCreate()");
        context = this;
        final Uri uri = getIntent().getData();
        if (uri == null) {
            setupInitScreen();
        } else {
            throw new IllegalStateException("Application was supposed to be declared singleTask");
        }

    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent);
        if (uriCallback != null) {
            Log.d(TAG, "Notifying paymentRequest about return URI");
            uriCallback.completionWithUri(intent.getData());
        }
    }

    @SuppressWarnings("unchecked")
    private void setupInitScreen() {
        final PaymentDataEntryFragment paymentDataEntryFragment = new PaymentDataEntryFragment();
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                paymentDataEntryFragment).commitAllowingStateLoss();
    }

    public List<PaymentMethod> getAvailablePaymentMethods() {
        return availablePaymentMethods;
    }

    public List<PaymentMethod> getPreferredPaymentMethods() {
        return preferredPaymentMethods;
    }

    @Override
    public void onPaymentRequested(final PaymentSetupRequest paymentSetupRequest) {
        Log.d(TAG, "onPaymentRequested");
        this.paymentSetupRequest = paymentSetupRequest;
        if (paymentRequest != null) {
            paymentRequest.cancel();
        }
        paymentRequest = new PaymentRequest(this, paymentRequestListener, paymentRequestDetailsListener);
        paymentRequest.start();
    }

    private String getSetupDataString(final String token) {
        final JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("reference", "M+M Black dress & accessories");
            jsonObject.put("shopperLocale", paymentSetupRequest.getShopperLocale());
            jsonObject.put("shopperReference", "demo.shopper");
            jsonObject.put("sessionValidity", "2017-05-10T13:09:50");
            jsonObject.put("token", token);

            jsonObject.put("appUrlScheme", "app://checkout");
            jsonObject.put("customerCountry", paymentSetupRequest.getCountryCode());
            jsonObject.put("currency", paymentSetupRequest.getAmount().getCurrency());
            jsonObject.put("quantity", paymentSetupRequest.getAmount().getValue());
            jsonObject.put("platform", "android");
            jsonObject.put("basketId", "M+M Black dress & accessories");
            jsonObject.put("customerId", "test");

        } catch (final JSONException jsonException) {
            Log.e("Unexpected error", "Setup failed");
        }
        return jsonObject.toString();
    }

    @Override
    public void onPaymentMethodSelected(@NonNull final PaymentMethod paymentMethod) {
        Log.d(TAG, "onPaymentMethodSelected(): " + paymentMethod.getType());
        if (paymentMethodCallback != null) {
            paymentMethodCallback.completionWithPaymentMethod(paymentMethod);
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

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }
}
