/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 29/01/2018.
 */

package com.adyen.checkout.ui.internal.common.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.adyen.checkout.base.LogoApi;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.NetworkingState;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentMethodHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.PaymentResult;
import com.adyen.checkout.core.RedirectDetails;
import com.adyen.checkout.core.handler.ErrorHandler;
import com.adyen.checkout.core.handler.RedirectHandler;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.internal.common.fragment.ErrorDialogFragment;
import com.adyen.checkout.ui.internal.common.fragment.ProgressDialogFragment;
import com.adyen.checkout.ui.internal.common.model.CheckoutSessionProvider;

public abstract class CheckoutSessionActivity extends AppCompatActivity implements CheckoutSessionProvider {

    @NonNull
    public static final String EXTRA_PAYMENT_REFERENCE = "EXTRA_PAYMENT_REFERENCE";

    private PaymentHandler mPaymentHandler;

    private PaymentSession mPaymentSession;

    @NonNull
    @Override
    public PaymentReference getPaymentReference() {
        return getIntent().getParcelableExtra(EXTRA_PAYMENT_REFERENCE);
    }

    @NonNull
    @Override
    public PaymentHandler getPaymentHandler() {
        return mPaymentHandler;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentHandler = getPaymentReference().getPaymentHandler(this);
        mPaymentHandler.getNetworkingStateObservable().observe(this, new Observer<NetworkingState>() {
            @Override
            public void onChanged(@NonNull NetworkingState networkingState) {
                if (networkingState.isExecutingRequests()) {
                    ProgressDialogFragment.show(CheckoutSessionActivity.this);
                } else {
                    ProgressDialogFragment.hide(CheckoutSessionActivity.this);
                }
            }
        });
        mPaymentHandler.getPaymentSessionObservable().observe(this, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                mPaymentSession = paymentSession;
            }
        });
        mPaymentHandler.getPaymentResultObservable().observe(this, new Observer<PaymentResult>() {
            @Override
            public void onChanged(@NonNull PaymentResult paymentResult) {
                handlePaymentComplete(paymentResult);
            }
        });
        mPaymentHandler.setRedirectHandler(this, new RedirectHandler() {
            @Override
            public void onRedirectRequired(@NonNull RedirectDetails redirectDetails) {
                Intent intent = RedirectHandlerActivity.newIntent(CheckoutSessionActivity.this, getPaymentReference(), redirectDetails);
                startActivity(intent);
            }
        });
        mPaymentHandler.setErrorHandler(this, new ErrorHandler() {
            @Override
            public void onError(@NonNull CheckoutException error) {
                handleCheckoutException(error);
            }
        });
    }

    @NonNull
    protected LogoApi getLogoApi() {
        return mPaymentHandler.getLogoApi();
    }

    @Nullable
    protected PaymentSession getPaymentSession() {
        return mPaymentSession;
    }

    protected void handlePaymentComplete(@NonNull PaymentResult paymentResult) {
        Intent data = new Intent();
        data.putExtra(PaymentMethodHandler.RESULT_PAYMENT_RESULT, paymentResult);
        setResult(PaymentMethodHandler.RESULT_CODE_OK, data);
        finish();
    }

    protected void handleCheckoutException(@NonNull CheckoutException checkoutException) {
        if (checkoutException.isFatal()) {
            Intent resultData = new Intent();
            resultData.putExtra(PaymentMethodHandler.RESULT_CHECKOUT_EXCEPTION, checkoutException);
            setResult(PaymentMethodHandler.RESULT_CODE_ERROR, resultData);
            finish();
        } else {
            ErrorDialogFragment
                    .newInstance(this, checkoutException)
                    .showIfNotShown(getSupportFragmentManager());
        }
    }
}
