package com.adyen.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.adyen.core.constants.Constants;
import com.adyen.core.models.Amount;
import com.adyen.core.models.PaymentMethod;
import com.adyen.core.models.paymentdetails.CreditCardPaymentDetails;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.core.models.paymentdetails.PaymentDetails;
import com.adyen.core.utils.AmountUtil;
import com.adyen.core.utils.StringUtils;
import com.adyen.ui.R;
import com.adyen.ui.activities.CheckoutActivity;
import com.adyen.ui.adapters.InstallmentOptionsAdapter;
import com.adyen.ui.utils.AdyenInputValidator;
import com.adyen.ui.views.CVCEditText;
import com.adyen.ui.views.CardHolderEditText;
import com.adyen.ui.views.CheckoutCheckBox;
import com.adyen.ui.views.CreditCardEditText;
import com.adyen.ui.views.ExpiryDateEditText;
import com.adyen.ui.views.loadinganimation.ThreeDotsLoadingView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import adyen.com.adyencse.encrypter.ClientSideEncrypter;
import adyen.com.adyencse.encrypter.exception.EncrypterException;

import static com.adyen.core.models.paymentdetails.CreditCardPaymentDetails.INSTALLMENTS;

/**
 * Fragment for collecting {@link PaymentDetails} for credit card payments.
 * Should be instantiated via {@link CreditCardFragmentBuilder}.
 */
public class CreditCardFragment extends Fragment {

    private static final String TAG = CreditCardFragment.class.getSimpleName();
    private CreditCardInfoListener creditCardInfoListener;
    private boolean oneClick;
    private boolean nameRequired;
    private Amount amount;
    private String shopperReference;
    private PaymentMethod paymentMethod;
    private String publicKey;
    private String generationTime;

    private CreditCardEditText creditCardNoView;
    private ExpiryDateEditText expiryDateView;
    private CVCEditText cvcView;
    private CardHolderEditText cardHolderEditText;
    private CheckoutCheckBox saveCardCheckBox;
    private Spinner installmentsSpinner;

    private int theme;

    /**
     * Use {@link CreditCardFragmentBuilder} instead.
     */
    public CreditCardFragment() {
        //Default empty constructor
    }

    /**
     * The listener interface for receiving the card payment details.
     */
    public interface CreditCardInfoListener {
        void onCreditCardInfoProvided(CreditCardPaymentDetails creditCardPaymentDetails);
    }

     void setCreditCardInfoListener(@NonNull final CreditCardInfoListener creditCardInfoListener) {
        this.creditCardInfoListener = creditCardInfoListener;
    }

    @Override
    public void setArguments(final Bundle args) {
        super.setArguments(args);
        oneClick = args.getBoolean(CheckoutActivity.ONE_CLICK);
        amount = (Amount) args.get(CheckoutActivity.AMOUNT);
        paymentMethod = (PaymentMethod) args.get(CheckoutActivity.PAYMENT_METHOD);
        shopperReference = args.getString(Constants.DataKeys.SHOPPER_REFERENCE);
        publicKey = args.getString(Constants.DataKeys.PUBLIC_KEY);
        generationTime = args.getString(Constants.DataKeys.GENERATION_TIME);

        for (InputDetail inputDetail : paymentMethod.getInputDetails()) {
            if (inputDetail.getKey().equals("cardHolderName")) {
                nameRequired = true;
            }
        }

        theme = args.getInt("theme");

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragmentView;

        final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), theme);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        fragmentView = localInflater.inflate(R.layout.credit_card_fragment, container, false);

        creditCardNoView = ((CreditCardEditText) fragmentView.findViewById(R.id.adyen_credit_card_no));
        expiryDateView = ((ExpiryDateEditText) fragmentView.findViewById(R.id.adyen_credit_card_exp_date));
        cvcView = ((CVCEditText) fragmentView.findViewById(R.id.adyen_credit_card_cvc));


        cardHolderEditText = ((CardHolderEditText) fragmentView.findViewById(R.id.credit_card_holder_name));
        if (nameRequired) {
            final LinearLayout cardHolderLayout = ((LinearLayout)
                    fragmentView.findViewById(R.id.card_holder_name_layout));
            cardHolderLayout.setVisibility(View.VISIBLE);
        }

        final Collection<InputDetail> inputDetails = paymentMethod.getInputDetails();
        for (final InputDetail inputDetail : inputDetails) {
            if (INSTALLMENTS.equals(inputDetail.getKey())) {
                fragmentView.findViewById(R.id.card_installments_area).setVisibility(View.VISIBLE);
                final List<InputDetail.Item> installmentOptions = inputDetail.getItems();
                installmentsSpinner = (Spinner) fragmentView.findViewById(R.id.installments_spinner);
                final InstallmentOptionsAdapter installmentOptionsAdapter = new InstallmentOptionsAdapter(getActivity(), installmentOptions);
                installmentsSpinner.setAdapter(installmentOptionsAdapter);
                break;
            }
        }

        final Button collectDataButton = (Button) fragmentView.findViewById(R.id.collectCreditCardData);

        final TextView checkoutTextView = (TextView) fragmentView.findViewById(R.id.amount_text_view);
        final String amountString = AmountUtil.format(amount, true, StringUtils.getLocale(getActivity()));
        final String checkoutString = getString(R.string.pay_with_amount, amountString);
        checkoutTextView.setText(checkoutString);
        AdyenInputValidator validator = new AdyenInputValidator();
        validator.setOnReadyStateChangedListener(new AdyenInputValidator.OnReadyStateChangedListener() {
            @Override
            public void onReadyStateChanged(boolean isReady) {
                collectDataButton.setEnabled(isReady);
            }
        });
        creditCardNoView.setValidator(validator);
        creditCardNoView.setCVCEditText(cvcView);
        creditCardNoView.setLogoUrl(paymentMethod.getLogoUrl());
        creditCardNoView.initializeLogo();

        final List<String> allowedCardTypes = new ArrayList<>();
        final List<PaymentMethod> memberPaymentMethods = paymentMethod.getMemberPaymentMethods();
        if (memberPaymentMethods != null) {
            for (final PaymentMethod memberPaymentMethod : memberPaymentMethods) {
                allowedCardTypes.add(memberPaymentMethod.getType());
            }
        }
        creditCardNoView.setAllowedCardTypes(allowedCardTypes);

        expiryDateView.setValidator(validator);
        cvcView.setValidator(validator);
        if (nameRequired) {
            cardHolderEditText.setValidator(validator);
        }

        saveCardCheckBox = (CheckoutCheckBox) fragmentView.findViewById(R.id.save_card_checkbox);
        if (!StringUtils.isEmptyOrNull(shopperReference)) {
            fragmentView.findViewById(R.id.layout_save_card).setVisibility(View.VISIBLE);
            fragmentView.findViewById(R.id.layout_click_area_save_card).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveCardCheckBox.forceRippleAnimation();
                    saveCardCheckBox.toggle();
                }
            });
        } else {
            fragmentView.findViewById(R.id.layout_save_card).setVisibility(View.GONE);
        }

        collectDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                String token = getToken();

                boolean storeDetails = !StringUtils.isEmptyOrNull(shopperReference)
                        && saveCardCheckBox.isChecked();

                if (creditCardInfoListener != null) {
                    final CreditCardPaymentDetails creditCardPaymentDetails = new CreditCardPaymentDetails(inputDetails);
                    creditCardPaymentDetails.fillCardToken(token);
                    if (installmentsSpinner != null) {
                        creditCardPaymentDetails.fillNumberOfInstallments(Short.valueOf(((InputDetail.Item) installmentsSpinner
                                .getSelectedItem()).getId()));
                    }
                    creditCardPaymentDetails.fillStoreDetails(storeDetails);
                    creditCardInfoListener.onCreditCardInfoProvided(creditCardPaymentDetails);
                } else {
                    Log.w(TAG, "No listener provided.");
                }

                checkoutTextView.setVisibility(View.GONE);
                final ThreeDotsLoadingView progressBar = ((ThreeDotsLoadingView)
                        fragmentView.findViewById(R.id.processing_progress_bar));
                progressBar.setVisibility(View.VISIBLE);

                cvcView.setEnabled(false);
                creditCardNoView.setEnabled(false);
                if (nameRequired) {
                    cardHolderEditText.setEnabled(false);
                }
                expiryDateView.setEnabled(false);

                InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);

            }
        });

        return fragmentView;
    }

    private String getToken() {
        if (!inputFieldsAvailable()) {
            return null;
        }
        if (TextUtils.isEmpty(publicKey)) {
            Log.e(TAG, "Public key is not available; credit card payment cannot be handled.");
            return "";
        }
        final JSONObject sensitiveData = new JSONObject();
        try {

            if (nameRequired) {
                sensitiveData.put("holderName", cardHolderEditText.getText());
            } else {
                sensitiveData.put("holderName", "checkout shopper");
            }
            sensitiveData.put("number", creditCardNoView.getCCNumber());

            sensitiveData.put("expiryMonth", expiryDateView.getMonth());
            sensitiveData.put("expiryYear", expiryDateView.getFullYear());
            sensitiveData.put("generationtime", generationTime);
            sensitiveData.put("cvc", cvcView.getCVC());

            ClientSideEncrypter encrypter = new ClientSideEncrypter(publicKey);
            String encryptedData = encrypter.encrypt(sensitiveData.toString());

            return encryptedData;
        } catch (JSONException e) {
            Log.e(TAG, "JSON Exception occurred while generating token.", e);
        } catch (EncrypterException e) {
            Log.e(TAG, "EncrypterException occurred while generating token.", e);
        }
        return "";
    }

    private boolean inputFieldsAvailable() {
        if (creditCardNoView == null
                || expiryDateView == null
                || cvcView == null
                || cardHolderEditText == null
                || saveCardCheckBox == null) {
            return false;
        }
        return true;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        CreditCardEditText creditCardNoView = ((CreditCardEditText) view.findViewById(R.id.adyen_credit_card_no));
        creditCardNoView.requestFocus();
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(creditCardNoView, InputMethodManager.SHOW_IMPLICIT);
    }

}
