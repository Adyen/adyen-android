package com.example.customwithadyenui;

import android.content.Context;
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
import com.adyen.core.models.Payment;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentRequestResult;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.IdealPaymentDetails;
import com.adyen.core.models.paymentdetails.SepaDirectDebitPaymentDetails;
import com.adyen.core.utils.AsyncHttpClient;
import com.adyen.ui.fragments.CreditCardFragment;
import com.adyen.ui.fragments.CreditCardFragmentBuilder;
import com.adyen.ui.fragments.IssuerSelectionFragment;
import com.adyen.ui.fragments.IssuerSelectionFragmentBuilder;
import com.adyen.ui.fragments.PaymentMethodSelectionFragment;
import com.adyen.ui.fragments.PaymentMethodSelectionFragmentBuilder;
import com.adyen.ui.fragments.SepaDirectDebitFragment;
import com.adyen.ui.fragments.SepaDirectDebitFragmentBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main activity for demonstrating how to use Checkout SDK. Client should implement an activity
 * similar to this.
 * In this sample application, UI is completely custom made. This example application does not
 * include wallet payment methods (samsungpay and android pay) in order to keep it simple.
 */
public class MainActivity extends FragmentActivity implements
        PaymentDataEntryFragment.PaymentRequestListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private PaymentSetupRequest paymentSetupRequest;

    private static final String MERCHANT_SERVER_URL = "https://checkoutshopper-test.adyen.com/checkoutshopper/demo/easy-integration/merchantserver/";

    private static final String SETUP = "setup";
    private static final String VERIFY = "verify";

    private static final String MERCHANT_API_SECRET_KEY = //YOUR_API_KEY
    private static final String MERCHANT_APP_ID = "TestMerchantApp";

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

            final PaymentMethodSelectionFragment paymentMethodSelectionFragment
                    = new PaymentMethodSelectionFragmentBuilder()
                    .setPaymentMethods(otherMethods)
                    .setPreferredPaymentMethods(recurringMethods)
                    .setPaymentMethodSelectionListener(new PaymentMethodSelectionFragment.PaymentMethodSelectionListener() {
                        @Override
                        public void onPaymentMethodSelected(PaymentMethod paymentMethod) {
                            callback.completionWithPaymentMethod(paymentMethod);
                        }
                    })
                    .build();


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

            if (PaymentMethod.Type.CARD.equals(paymentMethodType)) {
                CreditCardFragment creditCardFragment = new CreditCardFragmentBuilder()
                        .setPaymentMethod(paymentRequest.getPaymentMethod())
                        .setPublicKey(paymentRequest.getPublicKey())
                        .setGenerationtime(paymentRequest.getGenerationTime())
                        .setAmount(paymentRequest.getAmount())
                        .setShopperReference(paymentRequest.getShopperReference())
                        .setCreditCardInfoListener(new CreditCardFragment.CreditCardInfoListener() {
                            @Override
                            public void onCreditCardInfoProvided(CreditCardPaymentDetails paymentDetails) {
                                callback.completionWithPaymentDetails(paymentDetails);
                            }
                        })
                        .build();

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, creditCardFragment).addToBackStack(null)
                        .commitAllowingStateLoss();
            } else if (PaymentMethod.Type.IDEAL.equalsIgnoreCase(paymentMethodType)) {
                IssuerSelectionFragment issuerSelectionFragment = new IssuerSelectionFragmentBuilder()
                        .setPaymentMethod(paymentRequest.getPaymentMethod())
                        .setIssuerSelectionListener(new IssuerSelectionFragment.IssuerSelectionListener() {
                            @Override
                            public void onIssuerSelected(IdealPaymentDetails issuerSelectionPaymentDetails) {
                                callback.completionWithPaymentDetails(issuerSelectionPaymentDetails);
                            }
                        })
                        .build();

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, issuerSelectionFragment).addToBackStack(null)
                        .commitAllowingStateLoss();
            } else if (PaymentMethod.Type.SEPA_DIRECT_DEBIT.equals(paymentMethodType)) {
                SepaDirectDebitFragment sepaDirectDebitFragment = new SepaDirectDebitFragmentBuilder()
                        .setAmount(paymentRequest.getAmount())
                        .setSEPADirectDebitPaymentDetailsListener(new SepaDirectDebitFragment.SEPADirectDebitPaymentDetailsListener() {
                            @Override
                            public void onPaymentDetails(SepaDirectDebitPaymentDetails paymentDetails) {
                                callback.completionWithPaymentDetails(paymentDetails);
                            }
                        })
                        .build();

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, sepaDirectDebitFragment).addToBackStack(null)
                        .commitAllowingStateLoss();

            } else {
                Log.w(TAG, "UI for " + paymentMethodType + " has not been implemented.");
                Toast.makeText(MainActivity.this, "UI for " + paymentMethodType + " has not been implemented.", Toast.LENGTH_LONG).show();
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

            jsonObject.put("shopperLocale", paymentSetupRequest.getShopperLocale());
            jsonObject.put("token", token);

            jsonObject.put("appUrlScheme", "example-shopping-app://");
            jsonObject.put("customerCountry", paymentSetupRequest.getCountryCode());
            jsonObject.put("currency", paymentSetupRequest.getAmount().getCurrency());
            jsonObject.put("quantity", paymentSetupRequest.getAmount().getValue());
            jsonObject.put("platform", "android");
            jsonObject.put("basketId", "M+M Black dress & accessories");

        } catch (final JSONException jsonException) {
            Log.e("Unexpected error", "Setup failed");
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
        headers.put("X-MerchantServer-App-SecretKey", MERCHANT_API_SECRET_KEY);
        AsyncHttpClient.post(MERCHANT_SERVER_URL + VERIFY, headers, verifyString, new HttpResponseCallback() {
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

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStackImmediate();
        } else {
            super.onBackPressed();
        }
    }
}
