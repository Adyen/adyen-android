package com.adyen.ui.fragments;

import android.os.Bundle;

import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.R;

import static com.adyen.core.constants.Constants.DataKeys.PAYMENT_METHOD;

/**
 * Builder to create {@link IssuerSelectionFragment}.
 */
public class IssuerSelectionFragmentBuilder {

    //mandatory fields
    private PaymentMethod paymentMethod;

    private IssuerSelectionFragment.IssuerSelectionListener issuerSelectionListener;

    private int theme = R.style.AdyenTheme;

    /**
     * Create a new instance of {@link IssuerSelectionFragmentBuilder}.
     */
    public IssuerSelectionFragmentBuilder() {

    }

    /**
     * Set the payment method that has been selected.
     * Needed to retrieve the issuers to disploay from the payment method.
     * @param paymentMethod The payment method that has been selected by the user.
     * @return IssuerSelectionFragmentBuilder
     */
    public IssuerSelectionFragmentBuilder setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    /**
     * Set the theme that should be applied to the generated fragment.
     * If not set the default Adyen style will be applied.
     * @param theme The theme that should be applied.
     * @return IssuerSelectionFragmentBuilder
     */
    public IssuerSelectionFragmentBuilder setTheme(final int theme) {
        this.theme = theme;
        return this;
    }

    /**
     * Set the {@link IssuerSelectionFragment.IssuerSelectionListener} to be notified when an issuer has been selected.
     * @param listener Listener that is notified when an issuer has been selected
     * @return The fragment that can be displayed in the ui.
     */
    public IssuerSelectionFragmentBuilder setIssuerSelectionListener(IssuerSelectionFragment.IssuerSelectionListener listener) {
        this.issuerSelectionListener = listener;
        return this;
    }


    /**
     * Build the fragment. Will fail with {@link IllegalStateException} if mandatory parameters have not been set.
     * @return The generated IssuerSelectionFragment
     */
    public IssuerSelectionFragment build() {
        checkParameters();

        IssuerSelectionFragment fragment = new IssuerSelectionFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PAYMENT_METHOD, paymentMethod);
        bundle.putInt("theme", theme);

        fragment.setArguments(bundle);
        fragment.setIssuerSelectionListener(issuerSelectionListener);

        return fragment;
    }

    private void checkParameters() {
        if (paymentMethod == null) {
            throw new IllegalStateException("PaymentMethod not set.");
        }
    }
}
