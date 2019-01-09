/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 17/01/2018.
 */

package com.adyen.checkout.ui.internal.card;

import static com.adyen.checkout.core.card.internal.CardValidatorImpl.AMEX_SECURITY_CODE_SIZE;
import static com.adyen.checkout.core.card.internal.CardValidatorImpl.GENERAL_CARD_SECURITY_CODE_SIZE;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.card.Card;
import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.card.CardValidator;
import com.adyen.checkout.core.card.Cards;
import com.adyen.checkout.core.card.EncryptedCard;
import com.adyen.checkout.core.card.EncryptionException;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.fragment.CheckoutDetailsFragment;
import com.adyen.checkout.ui.internal.common.fragment.ErrorDialogFragment;
import com.adyen.checkout.ui.internal.common.model.CheckoutMethodPickerListener;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;

import java.util.Date;

public class CardOneClickConfirmationFragment extends CheckoutDetailsFragment {
    private static final String ARG_PAYMENT_METHOD = "ARG_PAYMENT_METHOD";

    private TextView mSecurityCodePromptTextView;

    private CodeView mSecurityCodeView;

    private Button mPayButton;

    private TextView mSurchargeTextView;

    private Button mSelectOtherPaymentMethodButton;

    private NumpadView mNumpadView;

    private PaymentMethod mPaymentMethod;

    @NonNull
    public static CardOneClickConfirmationFragment newInstance(@NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_PAYMENT_REFERENCE, paymentReference);
        args.putParcelable(ARG_PAYMENT_METHOD, paymentMethod);

        CardOneClickConfirmationFragment fragment = new CardOneClickConfirmationFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getArguments().getParcelable(ARG_PAYMENT_METHOD);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_card_one_click_details, container, false);

        mSecurityCodePromptTextView = view.findViewById(R.id.textView_securityCodePrompt);
        com.adyen.checkout.core.model.Card card = mPaymentMethod.getStoredDetails().getCard();
        String maskedCardNumber = Cards.FORMATTER.maskNumber(card.getLastFourDigits());
        String securityCodePrompt = getString(R.string.checkout_card_one_click_security_code_prompt, maskedCardNumber);
        SpannableStringBuilder builder = new SpannableStringBuilder(securityCodePrompt);
        int start = securityCodePrompt.indexOf(maskedCardNumber);
        int end = start + maskedCardNumber.length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSecurityCodePromptTextView.setText(builder);

        mSecurityCodeView = view.findViewById(R.id.codeView_securityCode);
        mSecurityCodeView.setInputType(InputType.TYPE_NULL);
        mSecurityCodeView.setLength(
                mPaymentMethod.getTxVariant().equals(CardType.AMERICAN_EXPRESS.getTxVariant()) ? AMEX_SECURITY_CODE_SIZE
                        : GENERAL_CARD_SECURITY_CODE_SIZE);

        mPayButton = view.findViewById(R.id.button_pay);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        mSurchargeTextView = view.findViewById(R.id.textView_surcharge);

        PayButtonUtil.setPayButtonText(this, mPaymentMethod, mPayButton, mSurchargeTextView);

        updatePayButton();

        mSelectOtherPaymentMethodButton = view.findViewById(R.id.button_selectOtherPaymentMethod);
        mSelectOtherPaymentMethodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentActivity activity = CardOneClickConfirmationFragment.this.getActivity();

                if (activity instanceof CheckoutMethodPickerListener) {
                    ((CheckoutMethodPickerListener) activity).onClearSelection();
                }
            }
        });

        mNumpadView = view.findViewById(R.id.numpadView);
        mNumpadView.setKeyListener(new NumpadView.KeyListener() {
            @Override
            public void onCharClicked(char character) {
                mSecurityCodeView.getText().append(character);
                updatePayButton();
            }

            @Override
            public void onBackspace() {
                Editable text = mSecurityCodeView.getText();
                int length = text.length();

                if (length > 0) {
                    text.delete(length - 1, length);
                    updatePayButton();
                }
            }
        });

        return view;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        updatePayButton();
    }

    private void updatePayButton() {
        CardValidator.SecurityCodeValidationResult validationResult = getSecurityCodeValidationResult();
        String validatedSecurityCode = validationResult.getSecurityCode();
        boolean isAmex = CardType.forTxVariantProvider(mPaymentMethod) == CardType.AMERICAN_EXPRESS;

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            mPayButton.setEnabled(
                    validatedSecurityCode == null
                            || (!isAmex && validatedSecurityCode.length() == GENERAL_CARD_SECURITY_CODE_SIZE)
                            || (isAmex && validatedSecurityCode.length() == AMEX_SECURITY_CODE_SIZE)
            );
        } else {
            mPayButton.setEnabled(false);
        }
    }

    private void submit() {
        CardValidator.SecurityCodeValidationResult validationResult = getSecurityCodeValidationResult();

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            PaymentSession paymentSession = getPaymentSession();

            if (paymentSession == null) {
                return;
            }

            String validatedSecurityCode = validationResult.getSecurityCode();

            if (validatedSecurityCode != null) {
                Card card = new Card.Builder().setSecurityCode(validatedSecurityCode).build();

                try {
                    Date generationTime = paymentSession.getGenerationTime();
                    String publicKey = Objects.requireNonNull(paymentSession.getPublicKey(), CardHandler.ERROR_MESSAGE_PUBLIC_KEY_NULL);
                    EncryptedCard encryptedCard = Cards.ENCRYPTOR.encryptFields(card, generationTime, publicKey).call();
                    CardDetails cardDetails = new CardDetails.Builder()
                            .setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode())
                            .build();
                    getPaymentHandler().initiatePayment(mPaymentMethod, cardDetails);
                } catch (EncryptionException e) {
                    Context context = getContext();

                    if (context != null) {
                        ErrorDialogFragment
                                .newInstance(context, e)
                                .showIfNotShown(getChildFragmentManager());
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected exception while encrypting card.");
                }
            } else {
                getPaymentHandler().initiatePayment(mPaymentMethod, null);
            }
        }
    }

    @NonNull
    private CardValidator.SecurityCodeValidationResult getSecurityCodeValidationResult() {
        String securityCode = mSecurityCodeView.getText().toString();
        InputDetail securityCodeInputDetail = InputDetailImpl.findByKey(mPaymentMethod.getInputDetails(), CardDetails.KEY_ENCRYPTED_SECURITY_CODE);
        boolean isRequired = securityCodeInputDetail != null && !securityCodeInputDetail.isOptional();

        return Cards.VALIDATOR.validateSecurityCode(securityCode, isRequired, CardType.forTxVariantProvider(mPaymentMethod));
    }
}
