package com.adyen.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.adyen.core.constants.Constants;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.R;

import static com.adyen.core.constants.Constants.DataKeys.AMOUNT;
import static com.adyen.core.constants.Constants.DataKeys.PAYMENT_METHOD;

/**
 * Builder to create {@link CreditCardFragment}.
 */
public class CreditCardFragmentBuilder {

    public enum CvcFieldStatus {
        REQUIRED,
        OPTIONAL,
        NOCVC
    }

    //mandatory fields
    private Amount amount;
    private PaymentMethod paymentMethod;
    private String generationtime;
    private String publicKey;
    private CvcFieldStatus cvcFieldStatus;
    private boolean paymentCardScanEnabled;
    private CreditCardFragment.CreditCardInfoListener creditCardInfoListener;

    //optional
    private String shopperReference;

    private int theme = R.style.AdyenTheme;

    public CreditCardFragmentBuilder() {

    }

    public CreditCardFragmentBuilder setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    /**
     * Set the {@link Amount} of this payment.
     * This is only to display the amount in the ui.
     * @param amount The {@link Amount} to be displayed to the user.
     * @return CreditCardFragmentBuilder
     */
    public CreditCardFragmentBuilder setAmount(final Amount amount) {
        this.amount = amount;
        return this;
    }


    public CreditCardFragmentBuilder setShopperReference(final String shopperReference) {
        this.shopperReference = shopperReference;
        return this;
    }

    public CreditCardFragmentBuilder setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public CreditCardFragmentBuilder setGenerationtime(final String generationtime) {
        this.generationtime = generationtime;
        return this;
    }

    public CreditCardFragmentBuilder setTheme(final int theme) {
        this.theme = theme;
        return this;
    }

    public CreditCardFragmentBuilder setCreditCardInfoListener(
            @NonNull final CreditCardFragment.CreditCardInfoListener creditCardInfoListener) {
        this.creditCardInfoListener = creditCardInfoListener;
        return this;
    }

    public CreditCardFragmentBuilder setCVCFieldStatus(final CvcFieldStatus cvcFieldStatus) {
        this.cvcFieldStatus = cvcFieldStatus;
        return this;
    }

    public CreditCardFragmentBuilder setPaymentCardScanEnabled(final boolean paymentCardScanEnabled) {
        this.paymentCardScanEnabled = paymentCardScanEnabled;
        return this;
    }

    /**
     * Build the {@link CreditCardFragment}.
     * This will fail with {@link IllegalStateException} if one or more mandatory parameters have not been set.
     * @return The fragment that can be displayed in the ui.
     */
    public CreditCardFragment build() {
        checkParameters();

        CreditCardFragment fragment = new CreditCardFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(AMOUNT, amount);
        bundle.putString(Constants.DataKeys.SHOPPER_REFERENCE, shopperReference);
        bundle.putString(Constants.DataKeys.PUBLIC_KEY, publicKey);
        bundle.putString(Constants.DataKeys.GENERATION_TIME, generationtime);
        bundle.putSerializable(PAYMENT_METHOD, paymentMethod);
        bundle.putString(Constants.DataKeys.CVC_FIELD_STATUS, cvcFieldStatus.name());
        bundle.putBoolean(Constants.DataKeys.PAYMENT_CARD_SCAN_ENABLED, paymentCardScanEnabled);
        bundle.putInt("theme", theme);

        fragment.setArguments(bundle);

        fragment.setCreditCardInfoListener(creditCardInfoListener);

        return fragment;
    }

    private void checkParameters() {
        if (amount == null) {
            throw new IllegalStateException("Amount not set.");
        }
        if (paymentMethod == null) {
            throw new IllegalStateException("PaymentMethod not set.");
        }
        if (publicKey == null) {
            throw new IllegalStateException("PublicKey not set.");
        }
        if (generationtime == null) {
            throw new IllegalStateException("Generationtime not set.");
        }
        if (creditCardInfoListener == null) {
            throw new IllegalStateException("CreditCardInfoListener not set.");
        }
    }
}
