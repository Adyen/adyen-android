package com.adyen.ui.fragments;

import android.os.Bundle;

import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.R;

import java.util.ArrayList;
import java.util.List;

import static com.adyen.ui.activities.CheckoutActivity.PAYMENT_METHODS;
import static com.adyen.ui.activities.CheckoutActivity.PREFERED_PAYMENT_METHODS;

/**
 * Builder to create {@link PaymentMethodSelectionFragment}.
 */
public class PaymentMethodSelectionFragmentBuilder {

    //mandatory
    private ArrayList<PaymentMethod> paymentMethods = new ArrayList<>();

    //optional
    private ArrayList<PaymentMethod> preferredPaymentMethods = new ArrayList<>();


    private int theme = R.style.AdyenTheme;

    private PaymentMethodSelectionFragment.PaymentMethodSelectionListener paymentMethodSelectionListener;

    public PaymentMethodSelectionFragmentBuilder() {

    }

    /**
     * Sets the payment methods that are available.
     * @param paymentMethods Available paymentmethods that should be available for selection by the user.
     * @return PaymentMethodSelectionFragmentBuilder
     */
    public PaymentMethodSelectionFragmentBuilder setPaymentMethods(final List<PaymentMethod> paymentMethods) {
        this.paymentMethods.clear();
        this.paymentMethods.addAll(paymentMethods);
        return this;
    }

    /**
     * Sets the stored payment methods from this shopper.
     * @param preferredPaymentMethods The available stored payment methods.
     * @return PaymentMethodSelectionFragmentBuilder
     */
    public PaymentMethodSelectionFragmentBuilder setPreferredPaymentMethods(final List<PaymentMethod> preferredPaymentMethods) {
        this.preferredPaymentMethods.clear();
        this.preferredPaymentMethods.addAll(preferredPaymentMethods);
        return this;
    }

    /**
     * Set the theme that should be applied to the generated fragment.
     * If not set the default Adyen style will be applied.
     * @param theme The theme that should be applied.
     * @return PaymentMethodSelectionFragmentBuilder
     */
    public PaymentMethodSelectionFragmentBuilder setTheme(final int theme) {
        this.theme = theme;
        return this;
    }

    public PaymentMethodSelectionFragmentBuilder setPaymentMethodSelectionListener(
            PaymentMethodSelectionFragment.PaymentMethodSelectionListener paymentMethodSelectionListener) {
        this.paymentMethodSelectionListener = paymentMethodSelectionListener;
        return this;
    }

    /**
     * Build the {@link PaymentMethodSelectionFragment}.
     * This will fail with {@link IllegalStateException} if one or more mandatory parameters have not been set.
     * @return The fragment that can be displayed in the ui.
     */
    public PaymentMethodSelectionFragment build() {
        checkParameters();

        PaymentMethodSelectionFragment fragment = new PaymentMethodSelectionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PAYMENT_METHODS, paymentMethods);
        bundle.putSerializable(PREFERED_PAYMENT_METHODS, preferredPaymentMethods);
        bundle.putInt("theme", theme);

        fragment.setArguments(bundle);
        fragment.setPaymentMethodSelectionListener(paymentMethodSelectionListener);
        return fragment;
    }

    private void checkParameters() {
        if (paymentMethods == null) {
            throw new IllegalStateException("PaymentMethods not set.");
        }
        if (paymentMethodSelectionListener == null) {
            throw new IllegalStateException("PaymentMethodSelectionListener not set.");
        }
    }
}
