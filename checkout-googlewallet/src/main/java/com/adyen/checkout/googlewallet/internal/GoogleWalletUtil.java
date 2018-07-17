package com.adyen.checkout.googlewallet.internal;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.Amount;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentMethodDetails;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.util.AmountFormat;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 24/04/2018.
 */
public abstract class GoogleWalletUtil {
    static final int TIMEOUT_DEFAULT_SECONDS = 4;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    @NonNull
    static Wallet.WalletOptions buildWalletOptions(int environment) {
        return new Wallet.WalletOptions.Builder()
                .setEnvironment(environment)
                .build();
    }

    @NonNull
    static Collection<Integer> getAllowedPaymentMethods() {
        // Uncomment to show the Google Wallet payment method, even if the shopper hasn't tokenized a card yet.
        return Arrays.asList(/*WalletConstants.PAYMENT_METHOD_CARD, */WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD);
    }

    @NonNull
    static Collection<Integer> getAllowedCardNetworks(@NonNull PaymentSession paymentSession) {
        Collection<Integer> result = new ArrayList<>();

        List<PaymentMethod> paymentMethods = paymentSession.getPaymentMethods();

        if (PaymentMethodImpl.findByType(paymentMethods, CardType.AMERICAN_EXPRESS.getTxVariant()) != null) {
            result.add(WalletConstants.CARD_NETWORK_AMEX);
        }

        if (PaymentMethodImpl.findByType(paymentMethods, CardType.DISCOVER.getTxVariant()) != null) {
            result.add(WalletConstants.CARD_NETWORK_DISCOVER);
        }

        if (PaymentMethodImpl.findByType(paymentMethods, CardType.JCB.getTxVariant()) != null) {
            result.add(WalletConstants.CARD_NETWORK_JCB);
        }

        if (PaymentMethodImpl.findByType(paymentMethods, CardType.MASTERCARD.getTxVariant()) != null) {
            result.add(WalletConstants.CARD_NETWORK_MASTERCARD);
        }

        if (PaymentMethodImpl.findByType(paymentMethods, CardType.VISA.getTxVariant()) != null) {
            result.add(WalletConstants.CARD_NETWORK_VISA);
        }

        return result;
    }

    /**
     * Loads the {@link PaymentMethodDetails} and notifies the {@link Listener} once the details are loaded.
     */
    public abstract void loadPaymentDetails();

    /**
     * Dispatch the results from {@link Activity#onActivityResult(int, int, Intent)} to this method.
     *
     * @param requestCode The request code.
     * @param resultCode The result code.
     * @param data The {@link Intent data}.
     */
    public abstract void onActivityResult(int requestCode, int resultCode, @Nullable Intent data);

    @NonNull
    String formatAmountValue(@NonNull Amount amount) {
        BigDecimal bigDecimal = AmountFormat.toBigDecimal(amount);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);

        return DECIMAL_FORMAT.format(bigDecimal);
    }

    /**
     * Base provider for a {@link GoogleWalletUtil}.
     */
    public interface BaseProvider {
        /**
         * @return The hosting {@link Activity}.
         */
        @NonNull
        Activity getHost();

        /**
         * @return The current {@link PaymentSession}.
         */
        @NonNull
        PaymentSession getPaymentSession();

        /**
         * @return The wallet {@link PaymentMethod}.
         */
        @NonNull
        PaymentMethod getPaymentMethod();
    }

    public interface Listener<T, P extends PaymentMethodDetails> {
        /**
         * Called when the {@link PaymentMethodDetails} have been received.
         *
         * @param t The result type.
         * @param p The {@link PaymentMethodDetails}.
         */
        void onPaymentMethodDetails(@NonNull T t, @NonNull P p);

        /**
         * Called when the shopper has cancelled the loading of the {@link PaymentMethodDetails}.
         *
         * @param errorCode The error code
         * @see WalletConstants
         */
        void onCancelled(int errorCode);

        /**
         * Called when an error occurred. Generally, there is no need to show an error to the shopper as the Wallet API will do that.
         *
         * @param errorCode The error code.
         * @see WalletConstants
         */
        void onError(int errorCode);
    }
}
