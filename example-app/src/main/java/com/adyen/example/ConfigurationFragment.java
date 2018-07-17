package com.adyen.example;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.adyen.checkout.ui.CheckoutSetupParameters;
import com.adyen.checkout.util.AmountFormat;
import com.adyen.checkout.util.internal.CheckoutCurrency;
import com.adyen.checkout.util.internal.SimpleTextWatcher;
import com.adyen.example.model.PaymentSetupRequest;

import java.util.Locale;
import java.util.MissingResourceException;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 14/11/2017.
 */
public class ConfigurationFragment extends Fragment {
    private TextView mAmountTextView;

    private TextView mCountryTextView;

    private TextView mShopperLocaleTextView;

    private TextView mInstallmentsTextView;

    private TextView mCardHolderNameTextView;

    private EditText mAmountValueEditText;

    private EditText mAmountCurrencyEditText;

    private EditText mCountryCodeEditText;

    private EditText mShopperLocaleEditText;

    private EditText mMerchantReferenceEditText;

    private EditText mShopperReferenceEditText;

    private EditText mShopperEmailEditText;

    private EditText mInstallmentsEditText;

    private EditText mCardHolderNameEditText;

    @SuppressLint("HardwareIds")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);

        mAmountTextView = view.findViewById(R.id.textView_amount);
        mCountryTextView = view.findViewById(R.id.textView_country);
        mShopperLocaleTextView = view.findViewById(R.id.textView_shopperLocale);
        mInstallmentsTextView = view.findViewById(R.id.textView_installments);
        mCardHolderNameTextView = view.findViewById(R.id.textView_cardHolderName);

        mAmountValueEditText = view.findViewById(R.id.editText_amountValue);
        mAmountValueEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedAmount();
            }
        });
        mAmountCurrencyEditText = view.findViewById(R.id.editText_amountCurrency);
        mAmountCurrencyEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedAmount();
            }
        });
        mCountryCodeEditText = view.findViewById(R.id.editText_countryCode);
        mCountryCodeEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedCountry();
            }
        });
        mShopperLocaleEditText = view.findViewById(R.id.editText_shopperLocale);
        mShopperLocaleEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mShopperLocaleEditText.removeTextChangedListener(this);

                for (int i = 0; i < s.length(); i++) {
                    char c = s.charAt(i);

                    if ((i == 0 || i == 1) && Character.isUpperCase(c)) {
                        s.replace(i, i + 1, String.valueOf(Character.toLowerCase(c)));
                    } else if ((i == 3 || i == 4) && Character.isLowerCase(c)) {
                        s.replace(i, i + 1, String.valueOf(Character.toUpperCase(c)));
                    }
                }

                mShopperLocaleEditText.addTextChangedListener(this);

                displayFormattedShopperLocale();
            }
        });
        mMerchantReferenceEditText = view.findViewById(R.id.editText_merchantReference);
        mShopperReferenceEditText = view.findViewById(R.id.editText_shopperReference);
        mShopperEmailEditText = view.findViewById(R.id.editText_shopperEmail);
        mInstallmentsEditText = view.findViewById(R.id.editText_installments);
        mInstallmentsEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedInstallments();
            }
        });
        mCardHolderNameEditText = view.findViewById(R.id.editText_cardHolderName);
        mCardHolderNameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedCardHolderNameRequirement();
            }
        });

        if (savedInstanceState == null) {
            String merchantReference = getAbsoluteHashString(BuildConfig.CHECKOUT_API_KEY);
            String shopperReference = Build.MANUFACTURER + "_" + Build.MODEL + "_"
                    + getAbsoluteHashString(Settings.Secure.getString(view.getContext().getContentResolver(), Settings.Secure.ANDROID_ID));
            String shopperEmail = "customer@provider.com";
            mMerchantReferenceEditText.setText(merchantReference);
            mShopperReferenceEditText.setText(shopperReference);
            mShopperEmailEditText.setText(shopperEmail);
        }

        displayFormattedAmount();
        displayFormattedCountry();
        displayFormattedShopperLocale();
        displayFormattedCardHolderNameRequirement();
        displayFormattedInstallments();

        return view;
    }

    @Nullable
    public PaymentSetupRequest getPaymentSetupRequest(@NonNull CheckoutSetupParameters checkoutSetupParameters) {
        boolean valid = true;

        PaymentSetupRequest.Amount amount = null;

        try {
            amount = getAmount();
        } catch (IllegalArgumentException e) {
            mAmountCurrencyEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        Locale shopperLocale = null;

        try {
            shopperLocale = getShopperLocale();
        } catch (IllegalArgumentException | MissingResourceException e) {
            mShopperLocaleEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        Locale country = null;

        try {
            country = getCountry();
        } catch (MissingResourceException e) {
            mCountryCodeEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        String merchantReference = mMerchantReferenceEditText.getText().toString();
        String shopperReference = mShopperReferenceEditText.getText().toString();

        String shopperEmail = null;

        try {
            shopperEmail = getShopperEmail();
        } catch (IllegalArgumentException e) {
            mShopperEmailEditText.setError(e.getMessage());
            valid = false;
        }

        PaymentSetupRequest.Configuration configuration = null;

        try {
            PaymentSetupRequest.CardHolderNameRequirement cardHolderNameRequirement = getCardHolderNameRequirement();

            if (cardHolderNameRequirement != null) {
                if (configuration == null) {
                    configuration = new PaymentSetupRequest.Configuration();
                }

                configuration.setCardHolderName(cardHolderNameRequirement);
            }

        } catch (IllegalArgumentException e) {
            mCardHolderNameEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        try {
            PaymentSetupRequest.Installments installments = getInstallments();

            if (installments != null) {
                if (configuration == null) {
                    configuration = new PaymentSetupRequest.Configuration();
                }

                configuration.setInstallments(installments);
            }
        } catch (NumberFormatException e) {
            mInstallmentsEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        if (valid) {
            String sdkToken = checkoutSetupParameters.getSdkToken();
            String returnUrl = checkoutSetupParameters.getReturnUrl();

            return new PaymentSetupRequest.Builder(BuildConfig.MERCHANT_ACCOUNT, sdkToken, returnUrl, amount)
                    .setShopperLocale(shopperLocale.toString())
                    .setCountryCode(country.getCountry())
                    .setReference(merchantReference)
                    .setShopperReference(shopperReference)
                    .setShopperEmail(shopperEmail)
                    .setConfiguration(configuration)
                    .build();
        } else {
            return null;
        }
    }

    private void displayFormattedAmount() {
        Context context = getContext();

        if (context != null) {
            try {
                PaymentSetupRequest.Amount amount = getAmount();
                mAmountTextView.setText(AmountFormat.format(getContext(), amount.getValue(), amount.getCurrency()));
            } catch (IllegalArgumentException e) {
                mAmountTextView.setText("???");
            }
        }
    }

    private void displayFormattedCountry() {
        try {
            mCountryTextView.setText(getCountry().getDisplayCountry());
        } catch (MissingResourceException e) {
            mCountryTextView.setText("???");
        }
    }

    private void displayFormattedShopperLocale() {
        try {
            Locale shopperLocale = getShopperLocale();
            mShopperLocaleTextView.setText(String.format("%s - %s", shopperLocale.getDisplayLanguage(), shopperLocale.getDisplayCountry()));
        } catch (IllegalArgumentException | MissingResourceException e) {
            mShopperLocaleTextView.setText("???");
        }
    }

    private void displayFormattedInstallments() {
        try {
            PaymentSetupRequest.Installments installments = getInstallments();

            if (installments == null) {
                mInstallmentsTextView.setText("-");
            } else {
                mInstallmentsTextView.setText(String.valueOf(installments.getMaxNumberOfInstallments()));
            }
        } catch (NumberFormatException e) {
            mInstallmentsTextView.setText("???");
        }
    }

    private void displayFormattedCardHolderNameRequirement() {
        try {
            PaymentSetupRequest.CardHolderNameRequirement cardHolderNameRequirement = getCardHolderNameRequirement();

            if (cardHolderNameRequirement == null) {
                mCardHolderNameTextView.setText("-");
            } else {
                mCardHolderNameTextView.setText(cardHolderNameRequirement.name());
            }
        } catch (IllegalArgumentException e) {
            mCardHolderNameTextView.setText("???");
        }
    }

    @NonNull
    private PaymentSetupRequest.Amount getAmount() throws IllegalArgumentException {
        String currencyText = mAmountCurrencyEditText.getText().toString().toUpperCase();
        long value = Long.parseLong(mAmountValueEditText.getText().toString());
        String currency = CheckoutCurrency.valueOf(currencyText).name();

        return new PaymentSetupRequest.Amount(value, currency);
    }

    @NonNull
    private Locale getCountry() throws MissingResourceException {
        Locale country = new Locale("", mCountryCodeEditText.getText().toString().toUpperCase());
        // Check if valid.
        country.getISO3Country();

        return country;
    }

    @NonNull
    private Locale getShopperLocale() throws IllegalArgumentException, MissingResourceException {
        String[] shopperLocaleParts = mShopperLocaleEditText.getText().toString().split("_");

        if (shopperLocaleParts.length != 2) {
            throw new IllegalArgumentException("Invalid shopper locale.");
        }

        Locale shopperLocale = new Locale(shopperLocaleParts[0].toLowerCase(), shopperLocaleParts[1].toUpperCase());
        // Check if valid.
        shopperLocale.getISO3Language();
        shopperLocale.getISO3Country();

        return shopperLocale;
    }

    @Nullable
    private String getShopperEmail() throws IllegalArgumentException {
        String shopperEmail = mShopperEmailEditText.getText().toString().trim();

        if (shopperEmail.isEmpty()) {
            return null;
        }

        if (Patterns.EMAIL_ADDRESS.matcher(shopperEmail).matches()) {
            return shopperEmail;
        } else {
            throw new IllegalArgumentException("Invalid shopper email.");
        }
    }

    @Nullable
    private PaymentSetupRequest.CardHolderNameRequirement getCardHolderNameRequirement() throws IllegalArgumentException {
        String cardHolderNameValue = mCardHolderNameEditText.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(cardHolderNameValue)) {
            return null;
        } else {
            return PaymentSetupRequest.CardHolderNameRequirement.valueOf(cardHolderNameValue);
        }
    }

    @Nullable
    private PaymentSetupRequest.Installments getInstallments() throws NumberFormatException {
        String installmentsString = mInstallmentsEditText.getText().toString().trim();

        if (installmentsString.isEmpty()) {
            return null;
        }

        int installmentsCount = Integer.parseInt(installmentsString);

        if (installmentsCount > 1) {
            return new PaymentSetupRequest.Installments(installmentsCount);
        } else {
            return null;
        }
    }

    @NonNull
    private String getAbsoluteHashString(@NonNull Object object) {
        return String.valueOf(Math.abs(object.hashCode()));
    }
}
