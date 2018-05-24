package com.adyen.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;

import java.util.Collection;


/**
 * Fragment for collecting payment details for Qiwi Wallet
 * Should be instantiated via {@link QiwiWalletFragmentBuilder}.
 */
public class QiwiWalletFragment extends Fragment {

    private PaymentMethod paymentMethod;
    private Amount amount;

    private int theme;

    private QiwiWalletPaymentDetailsListener qiwiWalletPaymentDetailsListener;

    /**
     * Use {@link QiwiWalletFragmentBuilder} instead.
     */
    public QiwiWalletFragment() {
        //Default empty constructor
    }


    public interface QiwiWalletPaymentDetailsListener {
        void onPaymentDetails(String countryCode, String telephoneNumber);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        amount = (Amount) args.get(CheckoutActivity.AMOUNT);

        paymentMethod = (PaymentMethod) args.getSerializable(CheckoutActivity.PAYMENT_METHOD);

        theme = args.getInt("theme");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View fragmentView;
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        fragmentView = localInflater.inflate(R.layout.qiwi_wallet_fragment, container, false);


        final EditText telephoneNumber = (EditText) fragmentView.findViewById(R.id.telephone_number_edit_text);

        final Spinner countryCode = (Spinner) fragmentView.findViewById(R.id.country_code_spinner);

        Collection<InputDetail> inputDetails = paymentMethod.getInputDetails();
        for (InputDetail inputDetail : inputDetails) {
            if ("qiwiwallet.telephoneNumberPrefix".equals(inputDetail.getKey())) {
                java.util.ArrayList<String> countryCodes = new java.util.ArrayList<>();
                for (InputDetail.Item country : inputDetail.getItems()) {
                    countryCodes.add(country.getName() + " (" + country.getId() + ")");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.select_dialog_item, countryCodes);
                countryCode.setAdapter(adapter);

                // TODO: Use a proper list adapter here to get rid of string magic below.
            }
        }

        final Button confirmButton = (Button) fragmentView.findViewById(R.id.collect_direct_debit_data);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String countryCodeString = countryCode.getSelectedItem().toString();
                String strippedCountryCode = countryCodeString.substring(countryCodeString.indexOf("+"), countryCodeString.indexOf(")"));
                qiwiWalletPaymentDetailsListener.onPaymentDetails(strippedCountryCode, telephoneNumber.getText().toString());
            }
        });
        confirmButton.setEnabled(true);

        final TextView amountTextview = (TextView) fragmentView.findViewById(R.id.amount_text_view);
        final String valueString = AmountUtil.format(amount, true, StringUtils.getLocale(getActivity()));
        final String amountString = getString(R.string.payButton_formatted, valueString);
        amountTextview.setText(amountString);

        if (getActivity() instanceof CheckoutActivity) {
            ((CheckoutActivity) getActivity()).setActionBarTitle(paymentMethod.getName());
        }

        return fragmentView;
    }

    void setQiwiWalletPaymentDetailsListener(QiwiWalletPaymentDetailsListener listener) {
        this.qiwiWalletPaymentDetailsListener = listener;
    }

}
