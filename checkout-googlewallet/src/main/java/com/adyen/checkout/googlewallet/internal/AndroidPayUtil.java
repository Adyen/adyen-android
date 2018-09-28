package com.adyen.checkout.googlewallet.internal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.model.Amount;
import com.adyen.checkout.core.model.AndroidPayConfiguration;
import com.adyen.checkout.core.model.AndroidPayDetails;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wallet.Cart;
import com.google.android.gms.wallet.FullWallet;
import com.google.android.gms.wallet.FullWalletRequest;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.MaskedWallet;
import com.google.android.gms.wallet.MaskedWalletRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentMethodTokenizationType;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 18/10/2017.
 */
public class AndroidPayUtil extends GoogleWalletUtil {
    private final Provider mProvider;

    private final GoogleApiClient mGoogleApiClient;

    private final Listener<FullWallet, AndroidPayDetails> mListener;

    @NonNull
    public static Callable<Boolean> isReadyToPay(
            @NonNull Context context,
            @NonNull PaymentSession paymentSession,
            @NonNull PaymentMethod paymentMethod
    ) {
        if (isPlayServicesUnavailable(context)) {
            return CALLABLE_FALSE;
        }

        return new IsReadyToPayCallable(context, paymentSession, paymentMethod);
    }

    @NonNull
    private static GoogleApiClient buildGoogleApiClient(@NonNull Context context, @NonNull PaymentMethod paymentMethod) {
        try {
            AndroidPayConfiguration configuration = paymentMethod.getConfiguration(AndroidPayConfiguration.class);
            Wallet.WalletOptions walletOptions = buildWalletOptions(configuration.getEnvironment());

            return new GoogleApiClient.Builder(context)
                    .addApi(Wallet.API, walletOptions)
                    .build();
        } catch (CheckoutException e) {
            throw new RuntimeException("Invalid configuration.");
        }
    }

    AndroidPayUtil(@NonNull Provider provider, @NonNull Listener<FullWallet, AndroidPayDetails> listener) {
        mProvider = provider;
        mGoogleApiClient = buildGoogleApiClient(mProvider.getHost(), mProvider.getPaymentMethod());
        mListener = listener;
    }

    @Override
    public void loadPaymentDetails() {
        Amount amount = mProvider.getPaymentSession().getPayment().getAmount();
        AndroidPayConfiguration configuration;

        try {
            configuration = mProvider.getPaymentMethod().getConfiguration(AndroidPayConfiguration.class);
        } catch (CheckoutException e) {
            throw new RuntimeException("Invalid configuration.", e);
        }

        MaskedWalletRequest maskedWalletRequest = getMaskedWalletRequest(amount, configuration.getMerchantName(), configuration.getPublicKey());
        Wallet.Payments.loadMaskedWallet(mGoogleApiClient, maskedWalletRequest, mProvider.getMaskedWalletRequestCode());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == mProvider.getMaskedWalletRequestCode()) {
            if (isUnhandledLoadWalletSuccess(resultCode, data)) {
                Objects.requireNonNull(data, "Data Intent for MaskedWallet is null.");
                MaskedWallet maskedWallet = data.getParcelableExtra(WalletConstants.EXTRA_MASKED_WALLET);
                Amount amount = mProvider.getPaymentSession().getPayment().getAmount();
                FullWalletRequest fullWalletRequest = getFullWalletRequest(maskedWallet, amount);
                Wallet.Payments.loadFullWallet(mGoogleApiClient, fullWalletRequest, mProvider.getFullWalletRequestCode());
            }
        } else if (requestCode == mProvider.getFullWalletRequestCode()) {
            if (isUnhandledLoadWalletSuccess(resultCode, data)) {
                Objects.requireNonNull(data, "Data Intent for FullWallet is null.");
                FullWallet fullWallet = data.getParcelableExtra(WalletConstants.EXTRA_FULL_WALLET);
                AndroidPayDetails androidPayDetails = new AndroidPayDetails.Builder(fullWallet.getPaymentMethodToken().getToken()).build();
                mListener.onPaymentMethodDetails(fullWallet, androidPayDetails);
            }
        }
    }

    @NonNull
    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    @NonNull
    private MaskedWalletRequest getMaskedWalletRequest(@NonNull Amount amount, @NonNull String merchantName, @NonNull String publicKey) {
        return MaskedWalletRequest.newBuilder()
                .setCurrencyCode(amount.getCurrency())
                .setEstimatedTotalPrice(formatAmountValue(amount))
                .setMerchantName(merchantName)
                .setCart(getCart(amount))
                .setPaymentMethodTokenizationParameters(getPaymentMethodTokenizationParameters(publicKey))
                .build();
    }

    @NonNull
    private PaymentMethodTokenizationParameters getPaymentMethodTokenizationParameters(@NonNull String publicKey) {
        return PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(PaymentMethodTokenizationType.NETWORK_TOKEN)
                .addParameter("publicKey", publicKey)
                .build();
    }

    @NonNull
    private FullWalletRequest getFullWalletRequest(@NonNull MaskedWallet maskedWallet, @NonNull Amount amount) {
        return FullWalletRequest.newBuilder()
                .setGoogleTransactionId(maskedWallet.getGoogleTransactionId())
                .setCart(getCart(amount))
                .build();
    }

    @NonNull
    private Cart getCart(@NonNull Amount amount) {
        return Cart.newBuilder()
                .setCurrencyCode(amount.getCurrency())
                .setTotalPrice(formatAmountValue(amount))
                .build();
    }

    private boolean isUnhandledLoadWalletSuccess(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            return true;
        } else {
            int errorCode = data != null
                    ? data.getIntExtra(WalletConstants.EXTRA_ERROR_CODE, WalletConstants.ERROR_CODE_UNKNOWN)
                    : WalletConstants.ERROR_CODE_UNKNOWN;

            if (resultCode == Activity.RESULT_CANCELED) {
                mListener.onCancelled(errorCode);
                return false;
            } else {
                mListener.onError(errorCode);
                return false;
            }
        }
    }

    /**
     * Provider for the {@link AndroidPayUtil}.
     */
    public interface Provider extends BaseProvider {
        /**
         * @return The request code used to load the {@link MaskedWallet}.
         */
        int getMaskedWalletRequestCode();

        /**
         * @return The request code used to load the {@link FullWallet}.
         */
        int getFullWalletRequestCode();
    }

    private static final class IsReadyToPayCallable implements Callable<Boolean> {
        private final Collection<Integer> mAllowedCardNetworks;

        private final GoogleApiClient mGoogleApiClient;

        private IsReadyToPayCallable(@NonNull Context context, @NonNull PaymentSession paymentSession, @NonNull PaymentMethod paymentMethod) {
            mAllowedCardNetworks = getAllowedCardNetworks(paymentSession);
            mGoogleApiClient = buildGoogleApiClient(context.getApplicationContext(), paymentMethod);
        }

        @Override
        public Boolean call() {
            if (mAllowedCardNetworks.isEmpty()) {
                return false;
            }

            try {
                mGoogleApiClient.connect();
                IsReadyToPayRequest isReadyToPayRequest = IsReadyToPayRequest.newBuilder()
                        .addAllowedPaymentMethods(getAllowedPaymentMethods())
                        .addAllowedCardNetworks(mAllowedCardNetworks)
                        .build();

                PendingResult<BooleanResult> pendingResult = Wallet.Payments.isReadyToPay(mGoogleApiClient, isReadyToPayRequest);
                BooleanResult booleanResult = pendingResult.await(TIMEOUT_DEFAULT_SECONDS, TimeUnit.SECONDS);

                return booleanResult.getStatus().isSuccess() && booleanResult.getValue();
            } finally {
                mGoogleApiClient.disconnect();
            }
        }
    }
}
