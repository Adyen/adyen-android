package com.adyen.checkout.googlewallet.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.Amount;
import com.adyen.checkout.core.model.GooglePayConfiguration;
import com.adyen.checkout.core.model.GooglePayDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 23/04/2018.
 */
public final class GooglePayUtil extends GoogleWalletUtil {
    private final Provider mProvider;

    private final PaymentsClient mPaymentsClient;

    private final Listener<PaymentData, GooglePayDetails> mListener;

    @NonNull
    public static Callable<Boolean> isReadyToPay(
            @NonNull Context context,
            @NonNull PaymentSession paymentSession,
            @NonNull PaymentMethod paymentMethod
    ) {
        return new IsReadyToPayCallable(context, paymentSession, paymentMethod);
    }

    @NonNull
    private static PaymentsClient buildPaymentClient(@NonNull Context context, @NonNull PaymentMethod paymentMethod) {
        try {
            GooglePayConfiguration configuration = paymentMethod.getConfiguration(GooglePayConfiguration.class);

            return Wallet.getPaymentsClient(context, buildWalletOptions(configuration.getEnvironment()));
        } catch (CheckoutException e) {
            throw new RuntimeException("Invalid configuration.");
        }
    }

    GooglePayUtil(@NonNull Provider provider, @NonNull Listener<PaymentData, GooglePayDetails> listener) {
        mProvider = provider;
        mPaymentsClient = buildPaymentClient(provider.getHost(), provider.getPaymentMethod());
        mListener = listener;
    }

    @Override
    public void loadPaymentDetails() {
        PaymentSession paymentSession = mProvider.getPaymentSession();
        Amount amount = paymentSession.getPayment().getAmount();
        GooglePayConfiguration configuration;

        try {
            configuration = mProvider.getPaymentMethod().getConfiguration(GooglePayConfiguration.class);
        } catch (CheckoutException e) {
            throw new RuntimeException("Invalid configuration.", e);
        }

        PaymentDataRequest paymentDataRequest = PaymentDataRequest.newBuilder()
                .setTransactionInfo(buildTransactionInfo(amount))
                .addAllowedPaymentMethods(getAllowedPaymentMethods())
                .setCardRequirements(buildCardRequirements())
                .setPaymentMethodTokenizationParameters(getPaymentMethodTokenizationParameters(configuration))
                .build();
        Task<PaymentData> paymentDataTask = mPaymentsClient.loadPaymentData(paymentDataRequest);
        AutoResolveHelper.resolveTask(paymentDataTask, mProvider.getHost(), mProvider.getRequestCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == mProvider.getRequestCode()) {
            if (resultCode == Activity.RESULT_OK) {
                Objects.requireNonNull(data, "Data Intent is null.");
                PaymentData paymentData = Objects.requireNonNull(PaymentData.getFromIntent(data), "PaymentData is null.");
                PaymentMethodToken paymentMethodToken = Objects
                        .requireNonNull(paymentData.getPaymentMethodToken(), "PaymentMethodToken is null.");
                GooglePayDetails googlePayDetails = new GooglePayDetails.Builder(paymentMethodToken.getToken()).build();
                mListener.onPaymentMethodDetails(paymentData, googlePayDetails);
            } else {
                Status status = AutoResolveHelper.getStatusFromIntent(data);
                int errorCode = status != null ? status.getStatusCode() : WalletConstants.ERROR_CODE_UNKNOWN;

                if (resultCode == Activity.RESULT_CANCELED) {
                    mListener.onCancelled(errorCode);
                } else if (resultCode == AutoResolveHelper.RESULT_ERROR) {
                    mListener.onError(errorCode);
                } else {
                    throw new RuntimeException("Unexpected result code.");
                }
            }
        }
    }

    @NonNull
    private TransactionInfo buildTransactionInfo(@NonNull Amount amount) {
        return TransactionInfo.newBuilder()
                .setCurrencyCode(amount.getCurrency())
                .setTotalPrice(formatAmountValue(amount))
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .build();
    }

    @NonNull
    private CardRequirements buildCardRequirements() {
        PaymentSession paymentSession = mProvider.getPaymentSession();
        Collection<Integer> allowedCardNetworks = getAllowedCardNetworks(paymentSession);

        return CardRequirements.newBuilder()
                .addAllowedCardNetworks(allowedCardNetworks)
                .build();
    }

    @NonNull
    private PaymentMethodTokenizationParameters getPaymentMethodTokenizationParameters(@NonNull GooglePayConfiguration configuration) {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", configuration.getGateway())
                .addParameter("gatewayMerchantId", configuration.getGatewayMerchantId())
                .build();
    }

    /**
     * Provider for the {@link GooglePayUtil}.
     */
    public interface Provider extends BaseProvider {
        /**
         * @return The request code used to load the {@link PaymentData}.
         */
        int getRequestCode();
    }

    private static final class IsReadyToPayCallable implements Callable<Boolean> {
        private static final int TIMEOUT_DEFAULT = 5;

        private final Collection<Integer> mAllowedCardNetworks;

        private final PaymentsClient mPaymentsClient;

        private final IsReadyToPayRequest mIsReadyToPayRequest;

        private IsReadyToPayCallable(@NonNull Context context, @NonNull PaymentSession paymentSession, @NonNull PaymentMethod paymentMethod) {
            mAllowedCardNetworks = getAllowedCardNetworks(paymentSession);
            mPaymentsClient = buildPaymentClient(context.getApplicationContext(), paymentMethod);
            mIsReadyToPayRequest = IsReadyToPayRequest.newBuilder()
                    .addAllowedPaymentMethods(getAllowedPaymentMethods())
                    .addAllowedCardNetworks(mAllowedCardNetworks)
                    .build();
        }

        @Override
        public Boolean call() throws Exception {
            if (mAllowedCardNetworks.isEmpty()) {
                return false;
            }

            Task<Boolean> readyToPayTask = mPaymentsClient.isReadyToPay(mIsReadyToPayRequest);

            return Tasks.await(readyToPayTask, TIMEOUT_DEFAULT, TimeUnit.SECONDS);
        }
    }
}
