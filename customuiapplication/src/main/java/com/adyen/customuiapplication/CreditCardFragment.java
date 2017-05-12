package com.adyen.customuiapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.adyen.core.models.paymentdetails.CVCOnlyPaymentDetails;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.PaymentDetails;

import org.json.JSONException;
import org.json.JSONObject;

import adyen.com.adyencse.encrypter.ClientSideEncrypter;
import adyen.com.adyencse.encrypter.exception.EncrypterException;

/**
 * Fragment for collecting credit card info.
 */

public class CreditCardFragment extends Fragment {

    private static final String TAG = CreditCardFragment.class.getSimpleName();
    private CreditCardInfoListener creditCardInfoListener;
    private boolean oneClick;
    private String publicKey;
    private String generationTime;

    /**
     * The listener interface for receiving payment method selection result.
     * Container Activity must implement this interface.
     */
    public interface CreditCardInfoListener {
        void onCreditCardInfoProvided(PaymentDetails paymentDetails);
    }

    public void setCreditCardInfoListener(@NonNull final CreditCardInfoListener creditCardInfoListener) {
        this.creditCardInfoListener = creditCardInfoListener;
    }

    @Override
    public void setArguments(final Bundle args) {
        super.setArguments(args);
        oneClick = args.getBoolean("oneClick");
        publicKey = args.getString("public_key");
        generationTime = args.getString("generation_time");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view;

        if (oneClick) {
            view = inflater.inflate(R.layout.credit_card_one_click_form, container, false);
            final EditText cvcView = ((EditText) view.findViewById(R.id.credit_card_cvc));
            view.findViewById(R.id.collectCreditCardData).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    CVCOnlyPaymentDetails paymentDetails = new CVCOnlyPaymentDetails(cvcView.getText().toString());
                    if (creditCardInfoListener != null) {
                        creditCardInfoListener.onCreditCardInfoProvided(paymentDetails);
                    } else {
                        Log.w(TAG, "No listener provided.");
                    }
                }
            });

        } else {
            view = inflater.inflate(R.layout.credit_card_form, container, false);
            final EditText creditCardNoView = ((EditText) view.findViewById(R.id.credit_card_no));
            final EditText expiryDateView = ((EditText) view.findViewById(R.id.credit_card_exp_date));
            final EditText cvcView = ((EditText) view.findViewById(R.id.credit_card_cvc));

            view.findViewById(R.id.collectCreditCardData).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final JSONObject sensitiveData = new JSONObject();
                    try {
                        sensitiveData.put("number", creditCardNoView.getText());
                        sensitiveData.put("expiryMonth", expiryDateView.getText().subSequence(0, 2));
                        sensitiveData.put("expiryYear", "20" + expiryDateView.getText().subSequence(2, 4));
                        sensitiveData.put("cvc", cvcView.getText());
                        sensitiveData.put("holderName", "checkout shopper");
                        sensitiveData.put("generationtime", generationTime);

                        try {
                            ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
                            String encryptedData = encrypter.encrypt(sensitiveData.toString());

                            CreditCardPaymentDetails paymentDetails = new CreditCardPaymentDetails(encryptedData, true);
                            if (creditCardInfoListener != null) {
                                creditCardInfoListener.onCreditCardInfoProvided(paymentDetails);
                            } else {
                                Log.w(TAG, "No listener provided.");
                            }
                        } catch (EncrypterException e) {
                            e.printStackTrace();
                        }


                    } catch (final JSONException jsonException) {
                        Log.e(TAG, "Credit card information cannot be collected");
                    }
                }
            });
        }
        return view;
    }

}
