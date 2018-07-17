package com.adyen.checkout.googlewallet.internal;

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
import com.adyen.checkout.core.model.AndroidPayDetails;
import com.adyen.checkout.core.model.GooglePayDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentMethodDetails;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.googlewallet.GoogleWalletHandler;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.WalletConstants;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 07/06/2018.
 */
public class GoogleWalletDetailsActivity extends Activity {
    private static final String EXTRA_PAYMENT_REFERENCE = "EXTRA_PAYMENT_REFERENCE";

    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final int REQUEST_CODE_MASKED_WALLET = 1;

    private static final int REQUEST_CODE_FULL_WALLET = REQUEST_CODE_MASKED_WALLET + 1;

    private static final int REQUEST_CODE_GOOGLE_PAY = REQUEST_CODE_FULL_WALLET + 1;

    private PaymentReference mPaymentReference;

    private PaymentMethod mPaymentMethod;

    private PaymentHandler mPaymentHandler;

    private GoogleWalletUtil mGoogleWalletUtil;

    private boolean mStarted;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, GoogleWalletDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        mPaymentReference = intent.getParcelableExtra(EXTRA_PAYMENT_REFERENCE);
        mPaymentMethod = intent.getParcelableExtra(EXTRA_PAYMENT_METHOD);
        mPaymentHandler = mPaymentReference.getPaymentHandler(this);
        final Observable<PaymentSession> paymentSessionObservable = mPaymentHandler.getPaymentSessionObservable();
        paymentSessionObservable.observe(this, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                if (PaymentMethodTypes.ANDROID_PAY.equals(mPaymentMethod.getType())) {
                    AndroidPayContext androidPayContext = new AndroidPayContext(paymentSession);
                    mGoogleWalletUtil = new AndroidPayUtil(androidPayContext, androidPayContext);
                    ensureGoogleApiClientStarted();
                } else if (PaymentMethodTypes.GOOGLE_PAY.equals(mPaymentMethod.getType())) {
                    GooglePayContext googlePayContext = new GooglePayContext(paymentSession);
                    mGoogleWalletUtil = new GooglePayUtil(googlePayContext, googlePayContext);
                } else {
                    throw new RuntimeException("Invalid PaymentMethod: " + mPaymentMethod);
                }

                if (savedInstanceState == null) {
                    mGoogleWalletUtil.loadPaymentDetails();
                }

                paymentSessionObservable.removeObserver(this);
            }
        });
        mPaymentHandler.setErrorHandler(this, new ErrorHandler() {
            @Override
            public void onError(@NonNull CheckoutException error) {
                Intent resultData = new Intent();
                resultData.putExtra(GoogleWalletHandler.RESULT_CHECKOUT_EXCEPTION, error);
                setResult(GoogleWalletHandler.RESULT_CODE_ERROR, resultData);
                finish();
            }
        });
        mPaymentHandler.getPaymentResultObservable().observe(this, new Observer<PaymentResult>() {
            @Override
            public void onChanged(@NonNull PaymentResult paymentResult) {
                Intent resultData = new Intent();
                resultData.putExtra(GoogleWalletHandler.RESULT_PAYMENT_RESULT, paymentResult);
                setResult(GoogleWalletHandler.RESULT_CODE_OK, resultData);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_MASKED_WALLET || requestCode == REQUEST_CODE_FULL_WALLET || requestCode == REQUEST_CODE_GOOGLE_PAY) {
            mGoogleWalletUtil.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mStarted = true;
        ensureGoogleApiClientStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mStarted = false;

        if (mGoogleWalletUtil instanceof AndroidPayUtil) {
            ((AndroidPayUtil) mGoogleWalletUtil).getGoogleApiClient().disconnect();
        }
    }

    private void ensureGoogleApiClientStarted() {
        if (mGoogleWalletUtil instanceof AndroidPayUtil && mStarted) {
            ((AndroidPayUtil) mGoogleWalletUtil).getGoogleApiClient().connect();
        }
    }

    private void handleErrorCode(int resultCode, int errorCode) {
        String message;

        switch (errorCode) {
            case WalletConstants.ERROR_CODE_SERVICE_UNAVAILABLE:
                message = "The service is currently not available.";
                break;
            case WalletConstants.ERROR_CODE_MERCHANT_ACCOUNT_ERROR:
                message = "An error with the merchant account occurred.";
                break;
            case WalletConstants.ERROR_CODE_BUYER_ACCOUNT_ERROR:
                message = "An error with the shopper account occurred;";
                break;
            case WalletConstants.ERROR_CODE_AUTHENTICATION_FAILURE:
                message = "An error occurred during authentication.";
                break;
            case WalletConstants.ERROR_CODE_UNKNOWN:
                message = "An unknown error occurred.";
                break;
            default:
                message = "An internal error occurred.";
                break;
        }

        CheckoutException checkoutException = new CheckoutException.Builder(message, null).build();

        Intent resultData = new Intent();
        resultData.putExtra(GoogleWalletHandler.RESULT_CHECKOUT_EXCEPTION, checkoutException);
        resultData.putExtra(GoogleWalletHandler.RESULT_ERROR_CODE, errorCode);
        setResult(resultCode, resultData);
        finish();
    }

    private abstract class GoogleWalletContext<T, P extends PaymentMethodDetails>
            implements GoogleWalletUtil.BaseProvider, GoogleWalletUtil.Listener<T, P> {
        private final PaymentSession mPaymentSession;

        private GoogleWalletContext(@NonNull PaymentSession paymentSession) {
            mPaymentSession = paymentSession;
        }

        @NonNull
        @Override
        public Activity getHost() {
            return GoogleWalletDetailsActivity.this;
        }

        @NonNull
        @Override
        public PaymentSession getPaymentSession() {
            return mPaymentSession;
        }

        @NonNull
        @Override
        public PaymentMethod getPaymentMethod() {
            return mPaymentMethod;
        }

        @Override
        public void onPaymentMethodDetails(@NonNull T t, @NonNull P p) {
            mPaymentHandler.initiatePayment(mPaymentMethod, p);
        }

        @Override
        public void onCancelled(int errorCode) {
            if (errorCode == WalletConstants.ERROR_CODE_UNKNOWN) {
                setResult(GoogleWalletHandler.RESULT_CODE_CANCELED);
                finish();
            } else {
                handleErrorCode(GoogleWalletHandler.RESULT_CODE_CANCELED, errorCode);
            }
        }

        @Override
        public void onError(int errorCode) {
            handleErrorCode(GoogleWalletHandler.RESULT_CODE_ERROR, errorCode);
        }
    }

    private final class AndroidPayContext extends GoogleWalletContext<FullWallet, AndroidPayDetails> implements AndroidPayUtil.Provider {
        private AndroidPayContext(@NonNull PaymentSession paymentSession) {
            super(paymentSession);
        }

        @Override
        public int getMaskedWalletRequestCode() {
            return REQUEST_CODE_MASKED_WALLET;
        }

        @Override
        public int getFullWalletRequestCode() {
            return REQUEST_CODE_FULL_WALLET;
        }
    }

    private final class GooglePayContext extends GoogleWalletContext<PaymentData, GooglePayDetails> implements GooglePayUtil.Provider {
        private GooglePayContext(@NonNull PaymentSession paymentSession) {
            super(paymentSession);
        }

        @Override
        public int getRequestCode() {
            return REQUEST_CODE_GOOGLE_PAY;
        }
    }
}
