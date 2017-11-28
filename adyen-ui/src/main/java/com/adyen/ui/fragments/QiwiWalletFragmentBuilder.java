package com.adyen.ui.fragments;

import android.os.Bundle;

import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;

import static com.adyen.core.constants.Constants.DataKeys.AMOUNT;

/**
 * Builder to create {@link QiwiWalletFragment}.
 */
public class QiwiWalletFragmentBuilder {
    private PaymentMethod paymentMethod;
    private Amount amount;

    private int theme = R.style.AdyenTheme;

    private QiwiWalletFragment.QiwiWalletPaymentDetailsListener qiwiWalletPaymentDetailsListener;

    public QiwiWalletFragmentBuilder() {

    }

    public QiwiWalletFragmentBuilder setPaymentMethod(final PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    /**
     * Set the {@link Amount} of this payment.
     * This is only to display the amount in the ui.
     * @param amount The {@link Amount} to be displayed to the user.
     * @return CreditCardFragmentBuilder
     */
    public QiwiWalletFragmentBuilder setAmount(final Amount amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Set the theme that should be applied to the generated fragment.
     * If not set the default Adyen style will be applied.
     * @param theme The theme that should be applied.
     * @return QiwiWalletFragmentBuilder
     */
    public QiwiWalletFragmentBuilder setTheme(final int theme) {
        this.theme = theme;
        return this;
    }

    public QiwiWalletFragmentBuilder setQiwiWalletPaymentDetailsListener(QiwiWalletFragment.QiwiWalletPaymentDetailsListener listener) {
        this.qiwiWalletPaymentDetailsListener = listener;
        return this;
    }

    /**
     * Build the {@link QiwiWalletFragment}.
     * This will fail with {@link IllegalStateException} if one or more mandatory parameters have not been set.
     * @return The fragment that can be displayed in the ui.
     */
    public QiwiWalletFragment build() {
        checkParameters();

        QiwiWalletFragment fragment = new QiwiWalletFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(CheckoutActivity.PAYMENT_METHOD, paymentMethod);
        bundle.putSerializable(AMOUNT, amount);
        bundle.putInt("theme", theme);

        fragment.setArguments(bundle);
        fragment.setQiwiWalletPaymentDetailsListener(qiwiWalletPaymentDetailsListener);

        return fragment;
    }

    private void checkParameters() {
        if (amount == null) {
            throw new IllegalStateException("Amount not set.");
        }

        if (paymentMethod == null) {
            throw new IllegalStateException("PaymentMethod not set.");
        }

        if (qiwiWalletPaymentDetailsListener == null) {
            throw new IllegalStateException("QiwiWalletPaymentDataListener not set.");
        }
    }
}
