package com.adyen.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.adyen.core.interfaces.PaymentDataCallback;
import com.adyen.core.interfaces.PaymentDetailsCallback;
import com.adyen.core.interfaces.PaymentMethodCallback;
import com.adyen.core.internals.PaymentTrigger;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.PaymentResponse;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.models.paymentdetails.PaymentDetails;

import org.json.JSONException;

import java.util.Map;

import static com.adyen.core.constants.Constants.PaymentRequest.PAYMENT_DETAILS;


class PaymentBroadcastReceivers {

    private static final String TAG = PaymentBroadcastReceivers.class.getSimpleName();

    private PaymentRequest paymentRequest;
    private PaymentStateHandler paymentStateHandler;

    PaymentBroadcastReceivers(PaymentStateHandler paymentStateHandler, PaymentRequest paymentRequest) {
        this.paymentRequest = paymentRequest;
        this.paymentStateHandler = paymentStateHandler;
    }

    private PaymentMethodCallback paymentMethodCallback = new PaymentMethodCallback() {
        @Override
        public void completionWithPaymentMethod(@NonNull final PaymentMethod selectedPaymentMethod) {
            paymentStateHandler.setPaymentMethod(selectedPaymentMethod);

            if (!(selectedPaymentMethod.getInputDetails() == null || selectedPaymentMethod.getInputDetails().isEmpty())) {
                paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(
                        PaymentTrigger.PAYMENT_DETAILS_REQUIRED);
            } else {
                paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(
                        PaymentTrigger.PAYMENT_DETAILS_NOT_REQUIRED);
            }
        }
    };

     private PaymentDataCallback paymentDataCallback = new PaymentDataCallback() {
        @Override
        public void completionWithPaymentData(@NonNull final byte[] paymentSetupData) {
            try {
                paymentStateHandler.setPaymentResponse(new PaymentResponse(paymentSetupData));
                paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(
                        PaymentTrigger.PAYMENT_DATA_PROVIDED);
            } catch (final JSONException jsonException) {
                Log.e(TAG, "Provided payment data response is invalid", jsonException);
                paymentStateHandler.setPaymentErrorThrowableAndTriggerError(
                        new Exception("Provided payment data response is invalid", jsonException));
            }
        }
    };


    private PaymentDetailsCallback paymentDetailsCallback = new PaymentDetailsCallback() {
        @Override
        public void completionWithPaymentDetails(PaymentDetails paymentDetails) {
            paymentStateHandler.setPaymentDetails(paymentDetails);
            paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(PaymentTrigger.PAYMENT_DETAILS_PROVIDED);
        }

        @Override
        public void completionWithPaymentDetails(@NonNull Map<String, Object> paymentDetails) {
            paymentStateHandler.setPaymentDetails(paymentDetails);
            paymentStateHandler.getPaymentProcessorStateMachine().onTrigger(PaymentTrigger.PAYMENT_DETAILS_PROVIDED);
        }
    };

    private BroadcastReceiver paymentRequestCancellationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d(TAG, "Payment Request Cancelled");
            paymentRequest.cancel();
        }
    };

    private BroadcastReceiver paymentMethodSelectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final PaymentMethod paymentMethod = (PaymentMethod) intent.getSerializableExtra("PaymentMethod");
            Log.d(TAG, "Payment Method Selected: " + paymentMethod.getName());
            paymentMethodCallback.completionWithPaymentMethod(paymentMethod);
        }
    };

    private BroadcastReceiver paymentDetailsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(PAYMENT_DETAILS)
                    && intent.getSerializableExtra(PAYMENT_DETAILS) instanceof PaymentDetails) {
                PaymentDetails paymentDetails = (PaymentDetails) intent.getSerializableExtra(PAYMENT_DETAILS);
                paymentDetailsCallback.completionWithPaymentDetails(paymentDetails);
            } else {
                //TODO something went wrong, notify error
            }
        }
    };

    private BroadcastReceiver androidPayInfoListener = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String androidPayToken = intent.getStringExtra("androidpay.token");
            final String errorMessage = intent.getStringExtra("androidpay.error");

            if (errorMessage != null) {
                Log.e(TAG, "androidPayInfoListener failed: " + errorMessage);
                paymentStateHandler.setPaymentErrorThrowableAndTriggerError(new Throwable(errorMessage));
            } else {
                PaymentDetails paymentDetails = new PaymentDetails(paymentStateHandler.getPaymentMethod().getInputDetails());
                if (androidPayToken != null) {
                    for (InputDetail inputDetail : paymentDetails.getInputDetails()) {
                        if (inputDetail.getType() == InputDetail.Type.AndroidPayToken) {
                            inputDetail.fill(androidPayToken);
                        }
                    }
                }
                paymentDetailsCallback.completionWithPaymentDetails(paymentDetails);
            }
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
        }
    };

    BroadcastReceiver getPaymentRequestCancellationReceiver() {
        return paymentRequestCancellationReceiver;
    }

    BroadcastReceiver getPaymentMethodSelectionReceiver() {
        return paymentMethodSelectionReceiver;
    }

    BroadcastReceiver getPaymentDetailsReceiver() {
        return paymentDetailsReceiver;
    }

    BroadcastReceiver getAndroidPayInfoListener() {
        return androidPayInfoListener;
    }

    PaymentMethodCallback getPaymentMethodCallback() {
        return paymentMethodCallback;
    }

    PaymentDetailsCallback getPaymentDetailsCallback() {
        return paymentDetailsCallback;
    }

    PaymentDataCallback getPaymentDataCallback() {
        return paymentDataCallback;
    }

}
