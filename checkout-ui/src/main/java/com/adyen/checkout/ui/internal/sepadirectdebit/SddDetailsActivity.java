package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.SepaDirectDebitDetails;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.common.util.LockToCheckmarkAnimationDelegate;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.util.SnackbarSwipeHandler;
import com.adyen.checkout.ui.internal.common.util.TextViewUtil;
import com.adyen.checkout.ui.internal.common.util.recyclerview.CheckoutItemAnimator;
import com.adyen.checkout.ui.internal.common.util.recyclerview.SpacingItemDecoration;
import com.adyen.checkout.util.internal.SimpleTextWatcher;
import com.adyen.checkout.util.sepadirectdebit.AsYouTypeIbanFormatter;
import com.adyen.checkout.util.sepadirectdebit.HolderName;
import com.adyen.checkout.util.sepadirectdebit.Iban;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 12/08/2017.
 */
public class SddDetailsActivity extends CheckoutDetailsActivity implements IbanSuggestionsAdapter.Listener, View.OnClickListener {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final int SNACKBAR_DURATION = 10_000;

    private PaymentMethod mPaymentMethod;

    private EditText mIbanEditText;

    private RecyclerView mIbanSuggestionsRecyclerView;

    private IbanSuggestionsAdapter mIbanSuggestionsAdapter;

    private EditText mAccountHolderNameEditText;

    private SwitchCompat mConsentSwitchCompat;

    private Button mPayButton;

    private Snackbar mSnackbar;

    @NonNull
    public static Intent newIntent(@NonNull Context context, @NonNull PaymentReference paymentReference, @NonNull PaymentMethod paymentMethod) {
        Intent intent = new Intent(context, SddDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    public void onSuggestionClick(@NonNull Suggestion suggestion) {
        mIbanEditText.getEditableText().insert(suggestion.getTargetIndex(), suggestion.getValue());
        mIbanEditText.setSelection(mIbanEditText.length());
    }

    @Override
    public void onClick(View v) {
        if (v == mPayButton && validate()) {
            Iban iban = Iban.parse(mIbanEditText.getText().toString());
            String holderName = mAccountHolderNameEditText.getText().toString().trim();

            //noinspection ConstantConditions, checked by validate().
            SepaDirectDebitDetails sepaDirectDebitDetails = new SepaDirectDebitDetails.Builder(iban.getValue(), holderName).build();
            getPaymentHandler().initiatePayment(mPaymentMethod, sepaDirectDebitDetails);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        setContentView(R.layout.activity_sdd_details);
        setTitle(mPaymentMethod.getName());
        mIbanEditText = findViewById(R.id.editText_iban);

        ValidationListener validationListener = new ValidationListener();

        mIbanSuggestionsRecyclerView = findViewById(R.id.recyclerView_ibanSuggestions);
        mIbanSuggestionsRecyclerView.setItemAnimator(new CheckoutItemAnimator(getResources()));
        mIbanSuggestionsAdapter = new IbanSuggestionsAdapter(mIbanEditText, this);
        mIbanSuggestionsRecyclerView.setAdapter(mIbanSuggestionsAdapter);
        int spacing = getResources().getDimensionPixelSize(R.dimen.standard_half_margin);
        mIbanSuggestionsRecyclerView.addItemDecoration(new SpacingItemDecoration(spacing));

        mIbanEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                mIbanSuggestionsRecyclerView.setVisibility(mIbanSuggestionsAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });
        mIbanEditText.addTextChangedListener(validationListener);
        mIbanEditText.setOnFocusChangeListener(validationListener);
        AsYouTypeIbanFormatter.attach(mIbanEditText);

        mAccountHolderNameEditText = findViewById(R.id.editText_accountHolderName);
        mAccountHolderNameEditText.addTextChangedListener(validationListener);
        mAccountHolderNameEditText.setOnFocusChangeListener(validationListener);

        mConsentSwitchCompat = findViewById(R.id.switchCompat_consent);
        mConsentSwitchCompat.setOnCheckedChangeListener(validationListener);

        mPayButton = findViewById(R.id.button_continue);
        PayButtonUtil.setPayButtonText(this, mPayButton);
        mPayButton.setOnClickListener(this);

        validate();

        if (savedInstanceState == null) {
            KeyboardUtil.show(mIbanEditText);
        }
    }

    private void checkSuggestZeroPaddedIban() {
        String ibanInput = mIbanEditText.getText().toString();
        Iban zeroPaddedIban = Iban.parseByAddingMissingZeros(ibanInput);

        if (mSnackbar == null && Iban.parse(ibanInput) == null && zeroPaddedIban != null && zeroPaddedIban.isSepa()) {
            final String formattedIban = Iban.format(zeroPaddedIban.getValue());
            mSnackbar = Snackbar
                    .make(mIbanEditText, R.string.checkout_sdd_iban_suggestion, Snackbar.LENGTH_INDEFINITE)
                    .setAction(formattedIban, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mIbanEditText.setText(formattedIban);
                        }
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar sb, int event) {
                            mSnackbar = null;
                        }
                    });
            mSnackbar.show();
            SnackbarSwipeHandler.attach(this, mSnackbar);
            mIbanEditText.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSnackbar != null) {
                        mSnackbar.dismiss();
                    }
                }
            }, SNACKBAR_DURATION);
        }
    }

    private boolean validateIban() {
        String ibanValue = mIbanEditText.getText().toString();
        Iban iban = Iban.parse(ibanValue);
        boolean isValid = iban != null && iban.isSepa();
        updateTextColor(mIbanEditText, isValid || (mIbanEditText.hasFocus() && Iban.isPartial(ibanValue)));

        return isValid;
    }

    private boolean validateHolderName() {
        String holderName = mAccountHolderNameEditText.getText().toString();
        boolean isValid = HolderName.isValid(holderName);
        updateTextColor(mAccountHolderNameEditText, isValid || (mAccountHolderNameEditText.hasFocus() && HolderName.isPartial(holderName)));

        return isValid;
    }

    private boolean validate() {
        boolean isValid = validateIban() & validateHolderName() & mConsentSwitchCompat.isChecked();
        mPayButton.setEnabled(isValid);

        return isValid;
    }

    private void updateTextColor(@NonNull TextView textView, boolean showAsValid) {
        if (showAsValid) {
            TextViewUtil.setDefaultTextColor(textView);
        } else {
            TextViewUtil.setErrorTextColor(textView);
        }
    }

    private final class ValidationListener extends SimpleTextWatcher implements View.OnFocusChangeListener, CompoundButton.OnCheckedChangeListener {
        private final LockToCheckmarkAnimationDelegate mDelegate;

        private boolean mDeleted;

        private ValidationListener() {
            mDelegate = new LockToCheckmarkAnimationDelegate(mIbanEditText, new LockToCheckmarkAnimationDelegate.ValidationCallback() {
                @Override
                public boolean isValid() {
                    Iban iban = Iban.parse(mIbanEditText.getText().toString());

                    return iban != null && iban.isSepa();
                }
            });
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mDeleted = count == 0;
        }

        @Override
        public void afterTextChanged(Editable s) {
            validate();

            boolean isValid = Iban.parse(s.toString()) != null;

            if (s == mIbanEditText.getEditableText() && !mDeleted && mIbanEditText.getSelectionEnd() == mIbanEditText.length() && isValid) {
                mAccountHolderNameEditText.requestFocus();
            }

            mDelegate.onTextChanged();
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                checkSuggestZeroPaddedIban();
            }

            mDelegate.onFocusChanged();

            validate();
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            validate();
        }
    }
}
