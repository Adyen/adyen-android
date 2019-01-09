/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 02/05/2018.
 */

package com.adyen.checkout.ui.internal.card;

import static com.adyen.checkout.core.card.internal.CardValidatorImpl.AMEX_SECURITY_CODE_SIZE;
import static com.adyen.checkout.core.card.internal.CardValidatorImpl.GENERAL_CARD_SECURITY_CODE_SIZE;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.AdditionalDetails;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.card.Card;
import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.card.CardValidator;
import com.adyen.checkout.core.card.Cards;
import com.adyen.checkout.core.card.EncryptedCard;
import com.adyen.checkout.core.card.EncryptionException;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.CupSecurePlusDetails;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.fragment.ErrorDialogFragment;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.util.PaymentMethodUtil;
import com.adyen.checkout.ui.internal.common.util.PhoneNumberUtil;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.Date;
import java.util.List;

public class CupSecurePlusOneClickDetailsActivity extends CheckoutDetailsActivity {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private TextView mSecurityCodePromptTextView;

    private CodeView mSecurityCodeView;

    private EditText mPhoneNumberEditText;

    private Button mPayButton;

    private TextView mSurchargeTextView;

    private PaymentMethod mPaymentMethod;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, CupSecurePlusOneClickDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        setTitle(mPaymentMethod.getName());
        setContentView(R.layout.activity_card_cup_secure_plus_one_click_details);

        mSecurityCodePromptTextView = findViewById(R.id.textView_securityCodePrompt);
        com.adyen.checkout.core.model.Card card = mPaymentMethod.getStoredDetails().getCard();
        String maskedCardNumber = Cards.FORMATTER.maskNumber(card.getLastFourDigits());
        String securityCodePrompt = getString(R.string.checkout_card_one_click_security_code_prompt, maskedCardNumber);
        SpannableStringBuilder builder = new SpannableStringBuilder(securityCodePrompt);
        int start = securityCodePrompt.indexOf(maskedCardNumber);
        int end = start + maskedCardNumber.length();
        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        mSecurityCodePromptTextView.setText(builder);

        int length = mPaymentMethod.getTxVariant().equals(CardType.AMERICAN_EXPRESS.getTxVariant()) ? AMEX_SECURITY_CODE_SIZE
                : GENERAL_CARD_SECURITY_CODE_SIZE;

        mSecurityCodeView = findViewById(R.id.codeView_securityCode);
        mSecurityCodeView.setLength(length);
        mSecurityCodeView.addTextChangedListener(new SimpleTextWatcher() {
            private boolean mDeleted;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDeleted = count == 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                updatePayButton();

                if (!mDeleted && getSecurityCodeValidationResult().getValidity() == CardValidator.Validity.VALID) {
                    KeyboardUtil.showAndSelect(mPhoneNumberEditText);
                }
            }
        });

        mPhoneNumberEditText = findViewById(R.id.editText_phoneNumber);
        mPhoneNumberEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updatePayButton();
            }
        });

        mPayButton = findViewById(R.id.button_pay);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });

        mSurchargeTextView = findViewById(R.id.textView_surcharge);

        PayButtonUtil.setPayButtonText(this, mPaymentMethod, mPayButton, mSurchargeTextView);

        updatePayButton();

        getPaymentHandler().setAdditionalDetailsHandler(this, new AdditionalDetailsHandler() {
            @Override
            public void onAdditionalDetailsRequired(@NonNull AdditionalDetails additionalDetails) {
                if (!PaymentMethodTypes.CUP.equals(additionalDetails.getPaymentMethodType())) {
                    return;
                }

                List<InputDetail> inputDetails = additionalDetails.getInputDetails();

                if (inputDetails.size() != 1 || !CupSecurePlusDetails.KEY_SMS_CODE.equals(inputDetails.get(0).getKey())) {
                    return;
                }

                CupSecurePlusOneClickDetailsActivity context = CupSecurePlusOneClickDetailsActivity.this;
                Intent intent = CupSecurePlusDetailsActivity.newIntent(context, getPaymentReference(), mPaymentMethod, additionalDetails);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        updatePayButton();
    }

    private void updatePayButton() {
        mPayButton.setEnabled(
                getSecurityCodeValidationResult().getValidity() == CardValidator.Validity.VALID
                        && getPhoneNumberValidationResult().getValidity() == PhoneNumberUtil.Validity.VALID
        );
    }

    private void submit() {
        CardValidator.SecurityCodeValidationResult securityCodeValidationResult = getSecurityCodeValidationResult();
        PhoneNumberUtil.ValidationResult phoneNumberValidationResult = getPhoneNumberValidationResult();

        if (securityCodeValidationResult.getValidity() != CardValidator.Validity.VALID
                || phoneNumberValidationResult.getValidity() != PhoneNumberUtil.Validity.VALID) {
            return;
        }

        PaymentSession paymentSession = getPaymentSession();

        if (paymentSession == null) {
            return;
        }

        String validatedSecurityCode = securityCodeValidationResult.getSecurityCode();
        String validatedPhoneNumber = phoneNumberValidationResult.getPhoneNumber();

        CardDetails.Builder cardDetailsBuilder = new CardDetails.Builder();

        if (validatedSecurityCode != null) {
            Card card = new Card.Builder().setSecurityCode(validatedSecurityCode).build();

            try {
                Date generationTime = paymentSession.getGenerationTime();
                String publicKey = Objects.requireNonNull(paymentSession.getPublicKey(), CardHandler.ERROR_MESSAGE_PUBLIC_KEY_NULL);
                EncryptedCard encryptedCard = Cards.ENCRYPTOR.encryptFields(card, generationTime, publicKey).call();
                cardDetailsBuilder.setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode());
            } catch (EncryptionException e) {
                ErrorDialogFragment
                        .newInstance(this, e)
                        .showIfNotShown(getSupportFragmentManager());

                return;
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception while encrypting card.");
            }
        }

        CardDetails cardDetails = cardDetailsBuilder.setPhoneNumber(validatedPhoneNumber).build();
        getPaymentHandler().initiatePayment(mPaymentMethod, cardDetails);
    }

    @NonNull
    private CardValidator.SecurityCodeValidationResult getSecurityCodeValidationResult() {
        String securityCode = mSecurityCodeView.getText().toString();
        InputDetail securityCodeInputDetail = InputDetailImpl.findByKey(mPaymentMethod.getInputDetails(), CardDetails.KEY_ENCRYPTED_SECURITY_CODE);
        boolean isRequired = securityCodeInputDetail != null && !securityCodeInputDetail.isOptional();

        return Cards.VALIDATOR.validateSecurityCode(securityCode, isRequired, CardType.forTxVariantProvider(mPaymentMethod));
    }


    @NonNull
    private PhoneNumberUtil.ValidationResult getPhoneNumberValidationResult() {
        PaymentMethodUtil.Requirement phoneNumberRequirement = PaymentMethodUtil
                .getRequirementForInputDetail(CardDetails.KEY_PHONE_NUMBER, mPaymentMethod);

        return PhoneNumberUtil.validate(mPhoneNumberEditText.getText().toString(), phoneNumberRequirement == PaymentMethodUtil.Requirement.REQUIRED);
    }
}
