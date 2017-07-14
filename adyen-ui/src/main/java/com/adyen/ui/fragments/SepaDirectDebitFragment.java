package com.adyen.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.adyen.core.models.Amount;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;
import com.adyen.ui.utils.AdyenInputValidator;
import com.adyen.ui.views.CardHolderEditText;
import com.adyen.ui.views.CheckoutCheckBox;
import com.adyen.ui.views.IBANEditText;


/**
 * Fragment for collecting payment details for SEPA Direct Debit.
 * Should be instantiated via {@link SepaDirectDebitFragmentBuilder}.
 */
public class SepaDirectDebitFragment extends Fragment {

    private Amount amount;

    private int theme;

    private SEPADirectDebitPaymentDetailsListener sepaDirectDebitPaymentDetailsListener;

    /**
     * Use {@link SepaDirectDebitFragmentBuilder} instead.
     */
    public SepaDirectDebitFragment() {
        //Default empty constructor
    }


    public interface SEPADirectDebitPaymentDetailsListener {
        void onPaymentDetails(String iban, String accountHolder);
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        amount = (Amount) args.get(CheckoutActivity.AMOUNT);

        theme = args.getInt("theme");

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View fragmentView;
        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        fragmentView = localInflater.inflate(R.layout.sepa_direct_debit_fragment, container, false);

        final AdyenInputValidator validator = new AdyenInputValidator();

        final IBANEditText ibanEditText = (IBANEditText) fragmentView.findViewById(R.id.adyen_sepa_iban_edit_text);
        ibanEditText.setValidator(validator);

        final CardHolderEditText ibanAccountHolder = (CardHolderEditText) fragmentView.findViewById(
                R.id.adyen_bank_account_holder_name);
        ibanAccountHolder.setValidator(validator);

        final CheckoutCheckBox consentCheckbox = (CheckoutCheckBox) fragmentView.findViewById(R.id.consent_direct_debit_checkbox);
        validator.addInputField(consentCheckbox);
        consentCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                validator.setReady(consentCheckbox, isChecked);
            }
        });
        fragmentView.findViewById(R.id.layout_click_area_consent_checkbox).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                consentCheckbox.forceRippleAnimation();
                consentCheckbox.toggle();
            }
        });

        final Button confirmButton = (Button) fragmentView.findViewById(R.id.collect_direct_debit_data);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sepaDirectDebitPaymentDetailsListener.onPaymentDetails(ibanEditText.getIbanNumber(), ibanEditText.getIbanNumber());
            }
        });
        validator.setOnReadyStateChangedListener(new AdyenInputValidator.OnReadyStateChangedListener() {
            @Override
            public void onReadyStateChanged(boolean isReady) {
                confirmButton.setEnabled(isReady);
            }
        });

        final TextView amountTextview = (TextView) fragmentView.findViewById(R.id.amount_text_view);
        final String valueString = AmountUtil.format(amount, true, StringUtils.getLocale(getActivity()));
        final String amountString = getString(R.string.pay_with_amount, valueString);
        amountTextview.setText(amountString);

        return fragmentView;
    }

    void setSEPADirectDebitPaymentDetailsListener(SEPADirectDebitPaymentDetailsListener listener) {
        this.sepaDirectDebitPaymentDetailsListener = listener;
    }

}
