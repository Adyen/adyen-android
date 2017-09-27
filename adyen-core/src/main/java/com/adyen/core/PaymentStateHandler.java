package com.adyen.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.adyen.core.exceptions.PostResponseFormatException;
import com.adyen.core.exceptions.UIModuleNotAvailableException;
import com.adyen.core.interfaces.DeletePreferredPaymentMethodListener;
import com.adyen.core.interfaces.HttpResponseCallback;
import com.adyen.core.interfaces.PaymentRequestDetailsListener;
import com.adyen.core.interfaces.PaymentRequestListener;
import com.adyen.core.interfaces.State;
import com.adyen.core.interfaces.UriCallback;
import com.adyen.core.internals.ModuleAvailabilityUtil;
import com.adyen.core.internals.PaymentProcessorStateMachine;
import com.adyen.core.internals.PaymentRequestState;
import com.adyen.core.internals.PaymentTrigger;
import com.adyen.core.models.Amount;
import com.adyen.core.models.Payment;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentRequestResult;
import com.adyen.core.models.PaymentResponse;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.utils.AsyncHttpClient;
import com.adyen.core.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import static com.adyen.core.constants.Constants.PaymentRequest.ADYEN_UI_FINALIZE_INTENT;
import static com.adyen.core.constants.Constants.PaymentRequest.PAYMENT_DETAILS_PROVIDED_INTENT;
import static com.adyen.core.constants.Constants.PaymentRequest.PAYMENT_METHOD_SELECTED_INTENT;

class PaymentStateHandler implements State.StateChangeListener {

    private static final String TAG = PaymentStateHandler.class.getSimpleName();

    private static final String SDK_RETURN_URL = "adyencheckout://";
    private static final String REDIRECT_RESPONSE = "redirect";
    private static final String COMPLETE_RESPONSE = "complete";
    private static final String ERROR_RESPONSE = "error";
    private static final String URL_JSON_KEY = "url";
    private static final String ANDROID_PAY_TOKEN_PROVIDED = "com.adyen.androidpay.ui.AndroidTokenProvided";

    private Throwable paymentErrorThrowable;

    @NonNull private PaymentRequestListener merchantPaymentRequestListener;
    @Nullable private PaymentRequestDetailsListener merchantPaymentRequestDetailsListener;

    private ArrayList<PaymentRequestDetailsListener> paymentRequestDetailsListeners = new ArrayList<>();
    private ArrayList<PaymentRequestListener> paymentRequestListeners = new ArrayList<>();

    private PaymentResponse paymentResponse;
    private Context context;
    private PaymentMethod paymentMethod;
    private JSONObject responseJson;
    private PaymentRequestResult paymentResult;
    private PaymentRequest paymentRequest;

    private List<PaymentMethod> filteredPaymentMethodsList;
    private List<PaymentMethod> preferredPaymentMethods;

    private PaymentProcessorStateMachine paymentProcessorStateMachine;
    private PaymentBroadcastReceivers paymentBroadcastReceivers;

    private PaymentDetails paymentDetails;
    @Deprecated private Map<String, Object> requiredFieldsPaymentDetails;

    PaymentStateHandler(Context context, PaymentRequest paymentRequest,
                        @NonNull final PaymentRequestListener paymentRequestListener,
                        @Nullable final PaymentRequestDetailsListener paymentRequestDetailsListener) {
        this.context = context;
        this.paymentRequest = paymentRequest;
        this.merchantPaymentRequestListener = paymentRequestListener;
        this.merchantPaymentRequestDetailsListener = paymentRequestDetailsListener;

        paymentBroadcastReceivers = new PaymentBroadcastReceivers(this, paymentRequest);
        filteredPaymentMethodsList = new ArrayList<>();
        paymentProcessorStateMachine = new PaymentProcessorStateMachine(this);

        paymentRequestListeners.add(paymentRequestListener);

        if (paymentRequestDetailsListener != null) {
            paymentRequestDetailsListeners.add(paymentRequestDetailsListener);
        } else {
            try {
                paymentRequestDetailsListeners.add(ListenerFactory.createAdyenPaymentRequestDetailsListener(context));
                paymentRequestListeners.add(ListenerFactory.createAdyenPaymentRequestListener(context));
            } catch (UIModuleNotAvailableException e) {
                setPaymentErrorThrowable(e);
            }

        }
    }

    @Override
    public void onStateChanged(State state) {
        Log.d(TAG, "onStateChanged: " + state.toString());
        final PaymentRequestState paymentRequestState = (PaymentRequestState) state;
        switch (paymentRequestState) {
            case WAITING_FOR_PAYMENT_DATA:
                Log.d(TAG, "Waiting for client to provide payment data.");
                requestPaymentData();
                break;
            case FETCHING_AND_FILTERING_PAYMENT_METHODS:
                Log.d(TAG, "Fetching and filtering the available payment methods");
                fetchPaymentMethods();
                break;
            case WAITING_FOR_PAYMENT_METHOD_SELECTION:
                Log.d(TAG, "Waiting for user to select payment method.");
                requestPaymentMethodSelection();
                break;
            case WAITING_FOR_PAYMENT_METHOD_DETAILS:
                Log.d(TAG, "Waiting for payment details (The selected payment method requires additional"
                        + " information)");
                requestPaymentMethodDetails();
                break;
            case PROCESSING_PAYMENT:
                Log.d(TAG, "Processing payment.");
                processPayment();
                break;
            case WAITING_FOR_REDIRECTION:
                Log.d(TAG, "Waiting for redirection.");
                handleRedirect();
                break;
            case PROCESSED:
                Log.d(TAG, "Payment processed.");
                unregisterBroadcastReceivers();
                notifyPaymentResult();
                break;
            case ABORTED:
                Log.d(TAG, "Payment aborted.");
                unregisterBroadcastReceivers();
                notifyPaymentError();
                break;
            case CANCELLED:
                Log.d(TAG, "Payment cancelled.");
                paymentResult = new PaymentRequestResult(new Throwable("Cancelled"));
                notifyPaymentResult();
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ADYEN_UI_FINALIZE_INTENT));
                unregisterBroadcastReceivers();
                break;
            default:
                Log.e(TAG, "Unexpected state: " + state.toString());
                paymentErrorThrowable = new IllegalStateException("Internal error -"
                        + " payment request state machine failure.");
                unregisterBroadcastReceivers();
                notifyPaymentError();
        }
    }

    @Override
    public void onStateNotChanged(State state) {
        Log.d(TAG, "onStateNotChanged: " + state.toString());
        final PaymentRequestState paymentRequestState = (PaymentRequestState) state;
        switch (paymentRequestState) {
            case WAITING_FOR_PAYMENT_METHOD_DETAILS:
                Log.d(TAG, "Waiting for payment details (The selected payment method requires additional"
                        + " information)");
                requestPaymentMethodDetails();
                break;
            default:
                Log.d(TAG, "No action will be taken for this state.");
        }
    }

    private void requestPaymentData() {
        Log.d(TAG, "requestPaymentData()");

        final String token = DeviceTokenGenerator.getToken(context, this);

        for (PaymentRequestListener listener : paymentRequestListeners) {
            listener.onPaymentDataRequested(paymentRequest, token, paymentBroadcastReceivers.getPaymentDataCallback());
        }
    }

    private void requestPaymentMethodDetails() {
        Log.d(TAG, "requestPaymentMethodDetails()");

        //FIXME check if this receiver is still needed
        final IntentFilter androidPayIntentFilter = new IntentFilter(ANDROID_PAY_TOKEN_PROVIDED);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                paymentBroadcastReceivers.getAndroidPayInfoListener(), androidPayIntentFilter);

        for (PaymentRequestDetailsListener detailsListener : paymentRequestDetailsListeners) {
            detailsListener.onPaymentDetailsRequired(paymentRequest, paymentMethod.getInputDetails(),
                    paymentBroadcastReceivers.getPaymentDetailsCallback());
        }
    }

    private void requestPaymentMethodSelection() {
        Log.d(TAG, "requestPaymentMethodSelection()");

        for (PaymentRequestDetailsListener detailsListener : paymentRequestDetailsListeners) {
            detailsListener.onPaymentMethodSelectionRequired(paymentRequest, preferredPaymentMethods,
                    filteredPaymentMethodsList, paymentBroadcastReceivers.getPaymentMethodCallback());
        }

        final IntentFilter intentFilter = new IntentFilter(PAYMENT_METHOD_SELECTED_INTENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                paymentBroadcastReceivers.getPaymentMethodSelectionReceiver(),
                intentFilter);

        final IntentFilter intentFilterPaymentDetails = new IntentFilter(PAYMENT_DETAILS_PROVIDED_INTENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(
                paymentBroadcastReceivers.getPaymentDetailsReceiver(), intentFilterPaymentDetails);
    }

    private void notifyPaymentResult() {
        Log.d(TAG, "Notifying the payment result to the merchant application");
        for (PaymentRequestListener listener : paymentRequestListeners) {
            listener.onPaymentResult(paymentRequest, paymentResult);
        }

    }

    private void notifyPaymentError() {
        Log.d(TAG, "Notifying the payment error to the client app");
        for (PaymentRequestListener listener : paymentRequestListeners) {
            listener.onPaymentResult(paymentRequest, new PaymentRequestResult(paymentErrorThrowable));
        }
    }

    private void unregisterBroadcastReceivers() {
        try {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(
                    paymentBroadcastReceivers.getPaymentMethodSelectionReceiver());
            LocalBroadcastManager.getInstance(context).unregisterReceiver(
                    paymentBroadcastReceivers.getAndroidPayInfoListener());
            LocalBroadcastManager.getInstance(context).unregisterReceiver(
                    paymentBroadcastReceivers.getPaymentRequestCancellationReceiver());
            LocalBroadcastManager.getInstance(context).unregisterReceiver(
                    paymentBroadcastReceivers.getPaymentDetailsReceiver());
        } catch (final IllegalArgumentException possibleException) {
            // Do not do anything. It is possible that these broadcast receivers have not been registered yet.
            Log.w(TAG, "Accepted exception", possibleException);
        }
    }

    private void fetchPaymentMethods() {
        if (paymentResponse != null) {
            List<PaymentMethod> unfilteredPaymentMethods = paymentResponse.getAvailablePaymentMethods();
            this.preferredPaymentMethods = paymentResponse.getPreferredPaymentMethods();
            Observable<List<PaymentMethod>> listObservable = ModuleAvailabilityUtil.filterPaymentMethods(context,
                    unfilteredPaymentMethods);
            listObservable.subscribe(new Consumer<List<PaymentMethod>>() {
                @Override
                public void accept(List<PaymentMethod> filteredPaymentMethods) {
                    filteredPaymentMethods.removeAll(Collections.singleton(null));
                    PaymentStateHandler.this.filteredPaymentMethodsList.clear();
                    PaymentStateHandler.this.filteredPaymentMethodsList.addAll(filteredPaymentMethods);
                    paymentProcessorStateMachine.onTrigger(PaymentTrigger.PAYMENT_METHODS_AVAILABLE);
                }
            });
        }
    }

    private void processPayment() {
        Log.d(TAG, "processPayment()");
        if (sdkHandlesUI()) {
            Log.d(TAG, "Checkout SDK will display an animation while processing.");
            //TODO: implement transparent loading view
        } else {
            Log.d(TAG, "The merchant application will handle UI while Checkout SDK is processing payment.");
        }
        initiatePayment(new HttpResponseCallback() {
            @Override
            public void onSuccess(@NonNull final byte[] response) {
                Log.d(TAG, "processPayment(): onSuccess");
                try {
                    responseJson = new JSONObject(new String(response, Charset.forName("UTF-8")));
                    String type = responseJson.getString("type");
                    if (REDIRECT_RESPONSE.equals(type)) {
                        paymentProcessorStateMachine.onTrigger(PaymentTrigger.REDIRECTION_REQUIRED);
                    } else if (COMPLETE_RESPONSE.equals(type)) {
                        paymentResult = new PaymentRequestResult(new Payment(responseJson));
                        paymentProcessorStateMachine.onTrigger(PaymentTrigger.PAYMENT_RESULT_RECEIVED);
                    } else if (ERROR_RESPONSE.equals(type)) {
                        Log.w(TAG, "Payment failed: " + responseJson.toString());
                        paymentErrorThrowable = new PostResponseFormatException(
                                responseJson.getString("errorMessage"));
                        paymentProcessorStateMachine.onTrigger(PaymentTrigger.ERROR_OCCURRED);
                    } else {
                        Log.e(TAG, "Unknown response type: " + responseJson.toString());
                        paymentErrorThrowable = new PostResponseFormatException("Unknown response type. "
                                + "Response must be redirect or complete." + responseJson.toString());
                        paymentProcessorStateMachine.onTrigger(PaymentTrigger.ERROR_OCCURRED);
                    }
                } catch (@NonNull final JSONException exception) {
                    Log.e(TAG, "processPayment(): JSONException occurred", exception);
                    setPaymentErrorThrowableAndTriggerError(exception);
                }
            }

            @Override
            public void onFailure(final Throwable e) {
                Log.e(TAG, "processPayment(): onFailure");
                setPaymentErrorThrowableAndTriggerError(e);
            }
        });
    }

    /**
     * Do an HTTP request for initiating payment.
     * @param httpResponseCallback The callback for notifying the result of HTTP call.
     */
    private void initiatePayment(@NonNull final HttpResponseCallback httpResponseCallback) {
        Log.d(TAG, "initiatePayment()");
        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        final JSONObject paymentRequestPostData = new JSONObject();
        try {
            paymentRequestPostData.put("paymentData", paymentResponse.getPaymentData());
            paymentRequestPostData.put("paymentMethodData", paymentMethod.getPaymentMethodData());

            JSONObject jsonDetails = new JSONObject();

            if (paymentDetails != null) {
                jsonDetails = paymentDetailsToJson(paymentDetails);
            } else if (requiredFieldsPaymentDetails != null) {
                jsonDetails = Util.mapToJson(requiredFieldsPaymentDetails);
            }

            if (sdkHandlesUI()) {
                if (paymentDetails == null && requiredFieldsPaymentDetails == null) {
                    jsonDetails = new JSONObject();
                }
                Log.d(TAG, "Return url is going to be overridden by SDK");
                jsonDetails.put("overrideReturnUrl", SDK_RETURN_URL);
            }
            if (jsonDetails != null) {
                paymentRequestPostData.put("paymentDetails", jsonDetails);
            }
            AsyncHttpClient.post(paymentResponse.getInitiationURL(), headers, paymentRequestPostData.toString(),
                    httpResponseCallback);
        } catch (final JSONException jsonException) {
            Log.e(TAG, "initiatePayment() error", jsonException);
            setPaymentErrorThrowableAndTriggerError(jsonException);
        }
    }

    private void handleRedirect() {
        Log.d(TAG, "handleRedirect()");
        try {
            disableOtherRedirectHandlers();
            final String url = responseJson.getString(URL_JSON_KEY);
            for (PaymentRequestDetailsListener listener : paymentRequestDetailsListeners) {
                listener.onRedirectRequired(paymentRequest, url, uriCallback);
            }
        } catch (final JSONException jsonException) {
            Log.e(TAG, "handleRedirect() exception occurred", jsonException);
            setPaymentErrorThrowableAndTriggerError(jsonException);
        }
    }

    /**
     * @return true if Checkout SDK handles UI.
     */
    boolean sdkHandlesUI() {
        return merchantPaymentRequestDetailsListener == null;
    }

    private void disableOtherRedirectHandlers() {
        final Intent intent = new Intent();
        intent.setAction("adyen.core.utils.DISABLE_REDIRECTION_HANDLER");
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        intent.putExtra("PackageName", context.getPackageName());
        context.sendBroadcast(intent);
    }

    PaymentProcessorStateMachine getPaymentProcessorStateMachine() {
        return paymentProcessorStateMachine;
    }

    PaymentBroadcastReceivers getPaymentBroadcastReceivers() {
        return paymentBroadcastReceivers;
    }

    String getPublicKey() {
        return paymentResponse.getPublicKey();
    }

    String getGenerationTime() {
        return paymentResponse.getGenerationTime();
    }

    Amount getAmount() {
        return paymentResponse.getAmount();
    }

    String getShopperReference() {
        return paymentResponse.getShopperReference();
    }

    PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    PaymentRequestListener getPaymentRequestListener() {
        return merchantPaymentRequestListener;
    }

    PaymentRequestDetailsListener getPaymentRequestDetailsListener() {
        if (paymentRequestDetailsListeners == null || paymentRequestDetailsListeners.isEmpty()) {
            return null;
        }

        return paymentRequestDetailsListeners.get(0);
    }

    boolean hasPaymentRequestDetailsListener() {
        return (paymentRequestDetailsListeners != null && !paymentRequestDetailsListeners.isEmpty());
    }

    void setPaymentMethod(@NonNull PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    void clearPaymentMethod() {
        this.paymentMethod = null;
    }

    void setPaymentResponse(PaymentResponse paymentResponse) {
        this.paymentResponse = paymentResponse;
    }

    void setPaymentErrorThrowable(Throwable errorThrowable) {
        this.paymentErrorThrowable = errorThrowable;
    }

    void setPaymentErrorThrowableAndTriggerError(Throwable errorThrowable) {
        this.paymentErrorThrowable = errorThrowable;
        paymentProcessorStateMachine.onTrigger(PaymentTrigger.ERROR_OCCURRED);
    }

    private UriCallback uriCallback = new UriCallback() {
        @Override
        public void completionWithUri(@NonNull Uri uri) {
            Log.d(TAG, "completionWithUri: " + uri);
            paymentResult = new PaymentRequestResult(new Payment(uri));
            paymentProcessorStateMachine.onTrigger(PaymentTrigger.RETURN_URI_RECEIVED);
        }
    };

    void setPaymentDetails(PaymentDetails paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    void setPaymentDetails(Map<String, Object> paymentDetails) {
        this.requiredFieldsPaymentDetails = paymentDetails;
    }

    void deletePreferredPaymentMethod(final PaymentMethod paymentMethod, final DeletePreferredPaymentMethodListener listener) {
        Log.d(TAG, "deletePreferredPaymentMethod()");
        if (paymentMethod == null) {
            Log.e(TAG, "paymentMethod cannot be null.");
            return;
        }
        if (listener == null) {
            Log.e(TAG, "listener cannot be null.");
            return;
        }
        if (!paymentMethod.isOneClick() || !preferredPaymentMethods.contains(paymentMethod)) {
            listener.onFail();
            return;
        }

        final Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=UTF-8");
        final JSONObject paymentRequestPostData = new JSONObject();
        try {
            paymentRequestPostData.put("paymentData", paymentResponse.getPaymentData());
            paymentRequestPostData.put("paymentMethodData", paymentMethod.getPaymentMethodData());

            AsyncHttpClient.post(paymentResponse.getDisableRecurringDetailUrl(), headers, paymentRequestPostData.toString(),
                    new HttpResponseCallback() {
                        @Override
                        public void onSuccess(byte[] response) {
                            try {
                                JSONObject jsonObject = new JSONObject(new String(response, Charset.forName("UTF-8")));

                                if (jsonObject.has("resultCode") && jsonObject.getString("resultCode").equals("Success")) {
                                    listener.onSuccess();

                                    preferredPaymentMethods.remove(paymentMethod);

                                    for (PaymentRequestDetailsListener detailsListener : paymentRequestDetailsListeners) {
                                        detailsListener.onPaymentMethodSelectionRequired(paymentRequest, preferredPaymentMethods,
                                                filteredPaymentMethodsList, paymentBroadcastReceivers.getPaymentMethodCallback());
                                    }
                                    return;
                                }
                            } catch (JSONException e) {
                                Log.e(TAG, "Deletion of preferred payment method has failed", e);
                            }
                            listener.onFail();
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            Log.e(TAG, "Deletion of preferred payment method has failed", e);
                            listener.onFail();
                        }
                    });
        } catch (JSONException e) {
            Log.e(TAG, "Deletion of preferred payment method has failed", e);
            listener.onFail();
        }
    }

    private static JSONObject paymentDetailsToJson(@NonNull PaymentDetails paymentDetails) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (InputDetail inputDetail : paymentDetails.getInputDetails()) {
            if (inputDetail.getInputDetails() != null && !inputDetail.getInputDetails().isEmpty()) {
                JSONObject recursiveDetailJson = new JSONObject();
                for (InputDetail recursiveDetail : inputDetail.getInputDetails()) {
                    recursiveDetailJson.put(recursiveDetail.getKey(), recursiveDetail.getValue());
                }
                jsonObject.put(inputDetail.getKey(), recursiveDetailJson);
            } else {
                jsonObject.put(inputDetail.getKey(), inputDetail.getValue());
            }
        }
        return jsonObject;
    }

}
