package com.adyen.ui.fragments;

import android.os.Bundle;

import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.R;

import static com.adyen.core.constants.Constants.DataKeys.AMOUNT;
import static com.adyen.core.constants.Constants.DataKeys.PAYMENT_METHOD;

/**
 * Builder to create {@link SepaDirectDebitFragment}.
 */
public class SepaDirectDebitFragmentBuilder {
    private PaymentMethod paymentMethod;
    private Amount amount;

    private int theme = R.style.AdyenTheme;

    private SepaDirectDebitFragment.SEPADirectDebitPaymentDetailsListener sepaDirectDebitPaymentDetailsListener;

    public SepaDirectDebitFragmentBuilder() {

    }

    public SepaDirectDebitFragmentBuilder setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    /**
     * Set the {@link Amount} of this payment.
     * This is only to display the amount in the ui.
     * @param amount The {@link Amount} to be displayed to the user.
     * @return CreditCardFragmentBuilder
     */
    public SepaDirectDebitFragmentBuilder setAmount(final Amount amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Set the theme that should be applied to the generated fragment.
     * If not set the default Adyen style will be applied.
     * @param theme The theme that should be applied.
     * @return SepaDirectDebitFragmentBuilder
     */
    public SepaDirectDebitFragmentBuilder setTheme(final int theme) {
        this.theme = theme;
        return this;
    }

    public SepaDirectDebitFragmentBuilder setSEPADirectDebitPaymentDetailsListener(
            SepaDirectDebitFragment.SEPADirectDebitPaymentDetailsListener listener) {
        this.sepaDirectDebitPaymentDetailsListener = listener;
        return this;
    }

    /**
     * Build the {@link SepaDirectDebitFragment}.
     * This will fail with {@link IllegalStateException} if one or more mandatory parameters have not been set.
     * @return The fragment that can be displayed in the ui.
     */
    public SepaDirectDebitFragment build() {
        checkParameters();

        SepaDirectDebitFragment fragment = new SepaDirectDebitFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PAYMENT_METHOD, paymentMethod);
        bundle.putSerializable(AMOUNT, amount);
        bundle.putInt("theme", theme);

        fragment.setArguments(bundle);
        fragment.setSEPADirectDebitPaymentDetailsListener(sepaDirectDebitPaymentDetailsListener);

        return fragment;
    }

    private void checkParameters() {
        if (amount == null) {
            throw new IllegalStateException("Amount not set.");
        }
        if (sepaDirectDebitPaymentDetailsListener == null) {
            throw new IllegalStateException("SepaDirectDebitPaymentDetailsListener not set.");
        }
    }
}
