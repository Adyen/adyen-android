package com.adyen.checkout;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.adyen.core.models.Amount;
import com.adyen.core.utils.AmountUtil;

import java.text.ParseException;

/**
 * Fragment for collecting payment data from user.
 *
 */
public class PaymentDataEntryFragment extends Fragment {

    private static final String TAG = PaymentDataEntryFragment.class.getSimpleName();
    private PaymentRequestListener paymentRequestListener;
    private PaymentSetupRequest paymentSetupRequest;
    private View fragmentView;

    /**
     * The listener interface for receiving payment request actions.
     * Container Activity must implement this interface.
     */
    public interface PaymentRequestListener {
        void onPaymentRequested(@NonNull PaymentSetupRequest paymentSetupRequest);
    }

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        try {
            this.paymentRequestListener = (PaymentRequestListener) context;
        } catch (final ClassCastException classCastException) {
            throw new ClassCastException(context.toString() + " is not a PaymentRequestListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentView = inflater.inflate(R.layout.activity_main, container, false);

        final Button proceedButton = (Button) fragmentView.findViewById(R.id.proceed_button);
        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    paymentSetupRequest = buildPaymentRequest(fragmentView);
                    paymentRequestListener.onPaymentRequested(paymentSetupRequest);
                } catch (final ParseException parseException) {
                    Log.e(TAG, "Invalid amount string has been entered", parseException);
                }
            }
        });

        return fragmentView;
    }

    @NonNull
    private PaymentSetupRequest buildPaymentRequest(final View view) throws ParseException {
        Log.v(TAG, "buildPaymentRequest()");
        PaymentSetupRequest paymentRequest = new PaymentSetupRequest();
        final String amountValueString = ((EditText) view.findViewById(R.id.orderAmountEntry)).getText().toString();
        final String amountCurrencyString = ((EditText) view.findViewById(R.id.orderCurrencyEntry))
                .getText().toString();

        paymentRequest.setAmount(new Amount(AmountUtil.parseMajorAmount(amountCurrencyString, amountValueString),
                amountCurrencyString));
        paymentRequest.setCountryCode(((EditText) view.findViewById(R.id.countryEntry)).getText().toString());
        paymentRequest.setShopperLocale(((EditText) view.findViewById(R.id.shopperLocaleEntry)).getText().toString());
        paymentRequest.setMerchantAccount(((EditText) view.findViewById(R.id.merchantAccountEntry)).getText()
                .toString());
        String maxNumberOfInstallments = ((String) ((Spinner) view.findViewById(R.id.installmentsEntry)).getSelectedItem());
        paymentRequest.setMaxNumberOfInstallments(maxNumberOfInstallments);

        return paymentRequest;
    }

}
