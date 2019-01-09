/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 07/06/2018.
 */

package com.adyen.checkout.googlepay.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.model.GooglePayConfiguration;
import com.adyen.checkout.core.model.GooglePayDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.googlepay.GooglePayHandler;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.WalletConstants;

import org.json.JSONException;

public class GooglePayDetailsActivity extends Activity {
    private static final String EXTRA_PAYMENT_REFERENCE = "EXTRA_PAYMENT_REFERENCE";

    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final int REQUEST_CODE_GOOGLE_PAY = 1;

    private PaymentMethod mPaymentMethod;

    private PaymentHandler mPaymentHandler;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, GooglePayDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        PaymentReference paymentReference = intent.getParcelableExtra(EXTRA_PAYMENT_REFERENCE);
        mPaymentMethod = intent.getParcelableExtra(EXTRA_PAYMENT_METHOD);
        mPaymentHandler = paymentReference.getPaymentHandler(this);

        final GooglePayConfiguration configuration;
        try {
            configuration = mPaymentMethod.getConfiguration(GooglePayConfiguration.class);
        } catch (CheckoutException e) {
            throw new RuntimeException("Invalid Google Pay configuration.");
        }

        if (savedInstanceState == null) {
            final Observable<PaymentSession> paymentSessionObservable = mPaymentHandler.getPaymentSessionObservable();
            paymentSessionObservable.observe(this, new Observer<PaymentSession>() {
                @Override
                public void onChanged(@NonNull PaymentSession paymentSession) {
                    Task<PaymentData> paymentDataTask = GooglePayUtil
                            .getPaymentDataTask(GooglePayDetailsActivity.this, paymentSession, configuration);
                    AutoResolveHelper.resolveTask(paymentDataTask, GooglePayDetailsActivity.this, REQUEST_CODE_GOOGLE_PAY);
                    paymentSessionObservable.removeObserver(this);
                }
            });
        }
        mPaymentHandler.setErrorHandler(this, new ErrorHandler() {
            @Override
            public void onError(@NonNull CheckoutException error) {
                Intent resultData = new Intent();
                resultData.putExtra(GooglePayHandler.RESULT_CHECKOUT_EXCEPTION, error);
                setResult(GooglePayHandler.RESULT_CODE_ERROR, resultData);
                finish();
            }
        });
        mPaymentHandler.getPaymentResultObservable().observe(this, new Observer<PaymentResult>() {
            @Override
            public void onChanged(@NonNull PaymentResult paymentResult) {
                Intent resultData = new Intent();
                resultData.putExtra(GooglePayHandler.RESULT_PAYMENT_RESULT, paymentResult);
                setResult(GooglePayHandler.RESULT_CODE_OK, resultData);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_GOOGLE_PAY) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String token = GooglePayUtil.getPaymentToken(data);
                    GooglePayDetails googlePayDetails = new GooglePayDetails.Builder(token).build();
                    mPaymentHandler.initiatePayment(mPaymentMethod, googlePayDetails);
                } catch (JSONException e) {
                    CheckoutException checkoutException = new CheckoutException.Builder("Could not retrieve token from PaymentData.", e).build();
                    Intent resultData = new Intent();
                    resultData.putExtra(GooglePayHandler.RESULT_CHECKOUT_EXCEPTION, checkoutException);
                    setResult(GooglePayHandler.RESULT_CODE_ERROR, resultData);
                    finish();
                }
            } else {
                Status status = AutoResolveHelper.getStatusFromIntent(data);
                int errorCode = status != null ? status.getStatusCode() : WalletConstants.ERROR_CODE_UNKNOWN;

                int result = resultCode == RESULT_CANCELED
                        ? GooglePayHandler.RESULT_CODE_CANCELED
                        : GooglePayHandler.RESULT_CODE_ERROR;

                Intent resultData = new Intent();
                resultData.putExtra(GooglePayHandler.RESULT_ERROR_CODE, errorCode);
                setResult(result, resultData);
                finish();
            }
        }
    }
}
