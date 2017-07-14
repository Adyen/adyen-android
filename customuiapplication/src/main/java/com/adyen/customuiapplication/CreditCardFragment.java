package com.adyen.customuiapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Date;

import adyen.com.adyencse.encrypter.exception.EncrypterException;
import adyen.com.adyencse.pojo.Card;

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
        void onCreditCardInfoProvided(String paymentDetails);
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
                    if (creditCardInfoListener != null) {
                        creditCardInfoListener.onCreditCardInfoProvided(cvcView.getText().toString());
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

                    Card card = new Card();
                    card.setNumber(creditCardNoView.getText().toString());
                    card.setCardHolderName("checkout shopper");
                    card.setCvc(cvcView.getText().toString());
                    card.setExpiryMonth(expiryDateView.getText().subSequence(0, 2).toString());
                    card.setExpiryYear("20" + expiryDateView.getText().subSequence(2, 4).toString());
                    card.setGenerationTime(new Date());

                    try {
                        String creditCardData = card.serialize(publicKey);
                        if (creditCardInfoListener != null) {
                            creditCardInfoListener.onCreditCardInfoProvided(creditCardData);
                        } else {
                            Log.w(TAG, "No listener provided.");
                        }
                    } catch (EncrypterException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return view;
    }

}
