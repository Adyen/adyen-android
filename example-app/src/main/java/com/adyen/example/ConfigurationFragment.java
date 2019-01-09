/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 14/11/2017.
 */

package com.adyen.example;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
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
import com.adyen.example.model.request.Address;
import com.adyen.example.model.request.Amount;
import com.adyen.example.model.request.Configuration;
import com.adyen.example.model.request.Installments;
import com.adyen.example.model.request.LineItem;
import com.adyen.example.model.request.ShopperInput;
import com.adyen.example.model.request.ShopperName;

import java.util.ArrayList;
import java.util.Locale;
import java.util.MissingResourceException;

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

    private SwitchCompat mAddShopperInputSwitch;

    //PaymentSetupRequest configuration parameters
    private String mShopperLocale = null;

    private String mCountry = null;

    private String mMerchantReference = null;

    private String mShopperReference = null;

    private String mShopperEmail = null;

    private Configuration mConfiguration = null;

    private ArrayList<LineItem> mLineItems = null;

    private ShopperName mShopperName = null;

    private String mDateOfBirth = null;

    private String mTelephoneNumber = null;

    private String mSocialSecurityNumber = null;

    private Address mBillingAddress = null;

    private Address mDeliveryAddress = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_configuration, container, false);

        findViews(view);
        setupTextChangeListeners();

        if (savedInstanceState == null) {
            String merchantReference = getAbsoluteHashString(BuildConfig.CHECKOUT_API_KEY);
            @SuppressLint("HardwareIds")
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

    private void findViews(View view) {
        mAmountTextView = view.findViewById(R.id.textView_amount);
        mCountryTextView = view.findViewById(R.id.textView_country);
        mShopperLocaleTextView = view.findViewById(R.id.textView_shopperLocale);
        mInstallmentsTextView = view.findViewById(R.id.textView_installments);
        mCardHolderNameTextView = view.findViewById(R.id.textView_cardHolderName);
        mAmountValueEditText = view.findViewById(R.id.editText_amountValue);
        mAmountCurrencyEditText = view.findViewById(R.id.editText_amountCurrency);
        mCountryCodeEditText = view.findViewById(R.id.editText_countryCode);
        mShopperLocaleEditText = view.findViewById(R.id.editText_shopperLocale);
        mMerchantReferenceEditText = view.findViewById(R.id.editText_merchantReference);
        mShopperReferenceEditText = view.findViewById(R.id.editText_shopperReference);
        mShopperEmailEditText = view.findViewById(R.id.editText_shopperEmail);
        mInstallmentsEditText = view.findViewById(R.id.editText_installments);
        mCardHolderNameEditText = view.findViewById(R.id.editText_cardHolderName);
        mAddShopperInputSwitch = view.findViewById(R.id.switch_addShopperInput);
    }

    private void setupTextChangeListeners() {
        mAmountValueEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedAmount();
            }
        });
        mAmountCurrencyEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedAmount();
            }
        });
        mCountryCodeEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedCountry();
            }
        });
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

        mInstallmentsEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedInstallments();
            }
        });
        mCardHolderNameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                displayFormattedCardHolderNameRequirement();
            }
        });
    }

    @Nullable
    public PaymentSetupRequest getPaymentSetupRequest(@NonNull CheckoutSetupParameters checkoutSetupParameters) {
        boolean valid = true;

        Amount amount = null;

        try {
            amount = getAmount();
        } catch (IllegalArgumentException e) {
            mAmountCurrencyEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        try {
            mShopperLocale = getShopperLocale().toString();
        } catch (IllegalArgumentException | MissingResourceException e) {
            mShopperLocaleEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        try {
            mCountry = getCountry().getCountry();
        } catch (MissingResourceException e) {
            mCountryCodeEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        mMerchantReference = mMerchantReferenceEditText.getText().toString();
        mShopperReference = mShopperReferenceEditText.getText().toString();

        try {
            mShopperEmail = getShopperEmail();
        } catch (IllegalArgumentException e) {
            mShopperEmailEditText.setError(e.getMessage());
            valid = false;
        }

        //Set CardHolderName
        try {
            Configuration.CardHolderNameRequirement cardHolderNameRequirement = getCardHolderNameRequirement();
            if (cardHolderNameRequirement != null) {
                if (mConfiguration == null) {
                    mConfiguration = new Configuration();
                }
                mConfiguration.setCardHolderName(cardHolderNameRequirement);
            }
        } catch (IllegalArgumentException e) {
            mCardHolderNameEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        //Set Installments
        try {
            Installments installments = getInstallments();
            if (installments != null) {
                if (mConfiguration == null) {
                    mConfiguration = new Configuration();
                }
                mConfiguration.setInstallments(installments);
            }
        } catch (NumberFormatException e) {
            mInstallmentsEditText.setError(e.getLocalizedMessage());
            valid = false;
        }

        //Set ShopperInput
        if (mAddShopperInputSwitch.isChecked()) {
            ShopperInput shopperInput = getShopperInput();
            if (mConfiguration == null) {
                mConfiguration = new Configuration();
            }
            mConfiguration.setShopperInput(shopperInput);


            mBillingAddress = getBillingAddress();
            mDeliveryAddress = getDeliveryAddress();
            mShopperName = getShopperName();
            mDateOfBirth = getDateOfBirth();
            mTelephoneNumber = getTelephoneNumber();
            mSocialSecurityNumber = getSocialSecurityNumber();
        }

        if (valid) {
            //adding mocked line item by default
            mLineItems = new ArrayList<>();
            mLineItems.add(new LineItem("Item1", amount.getValue().intValue()));
        }

        if (valid) {
            String sdkToken = checkoutSetupParameters.getSdkToken();
            String returnUrl = checkoutSetupParameters.getReturnUrl();

            return new PaymentSetupRequest.Builder(BuildConfig.MERCHANT_ACCOUNT, sdkToken, returnUrl, amount)
                    .setShopperLocale(mShopperLocale)
                    .setCountryCode(mCountry)
                    .setReference(mMerchantReference)
                    .setShopperReference(mShopperReference)
                    .setShopperEmail(mShopperEmail)
                    .setConfiguration(mConfiguration)
                    .setLineItems(mLineItems)
                    .setShopperName(mShopperName)
                    .setDateOfBirth(mDateOfBirth)
                    .setTelephoneNumber(mTelephoneNumber)
                    .setSocialSecurityNumber(mSocialSecurityNumber)
                    .setBillingAddress(mBillingAddress)
                    .setDeliveryAddress(mDeliveryAddress)
                    .build();
        } else {
            return null;
        }
    }

    private void displayFormattedAmount() {
        Context context = getContext();

        if (context != null) {
            try {
                Amount amount = getAmount();
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
            Installments installments = getInstallments();

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
            Configuration.CardHolderNameRequirement cardHolderNameRequirement = getCardHolderNameRequirement();

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
    private Amount getAmount() throws IllegalArgumentException {
        String currencyText = mAmountCurrencyEditText.getText().toString().toUpperCase();
        long value = Long.parseLong(mAmountValueEditText.getText().toString());
        String currency = CheckoutCurrency.valueOf(currencyText).name();

        return new Amount(value, currency);
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
    private Configuration.CardHolderNameRequirement getCardHolderNameRequirement() throws IllegalArgumentException {
        String cardHolderNameValue = mCardHolderNameEditText.getText().toString().trim().toUpperCase();

        if (TextUtils.isEmpty(cardHolderNameValue)) {
            return null;
        } else {
            return Configuration.CardHolderNameRequirement.valueOf(cardHolderNameValue);
        }
    }

    @Nullable
    private Installments getInstallments() throws NumberFormatException {
        String installmentsString = mInstallmentsEditText.getText().toString().trim();

        if (installmentsString.isEmpty()) {
            return null;
        }

        int installmentsCount = Integer.parseInt(installmentsString);

        if (installmentsCount > 1) {
            return new Installments(installmentsCount);
        } else {
            return null;
        }
    }

    @NonNull
    private String getAbsoluteHashString(@NonNull Object object) {
        return String.valueOf(Math.abs(object.hashCode()));
    }

    @NonNull
    private ShopperInput getShopperInput() {
        ShopperInput shopperInput = new ShopperInput();
        shopperInput.setPersonalDetails(ShopperInput.FieldVisibility.EDITABLE);
        shopperInput.setBillingAddress(ShopperInput.FieldVisibility.EDITABLE);
        shopperInput.setDeliveryAddress(ShopperInput.FieldVisibility.EDITABLE);
        return shopperInput;
    }

    @NonNull
    private ShopperName getShopperName() {

        ShopperName shopperName = new ShopperName();
        shopperName.setFirstName("Shopper");
        shopperName.setLastName("Android Checkout");
        shopperName.setGender(ShopperName.Gender.MALE);
        return shopperName;
    }

    @NonNull
    private String getTelephoneNumber() {
        switch (mCountry) {
            case "SE":
                return "0765260000";
            case "NO":
                return "40 123 456";
            case "NL":
                return "0612345678";
            case "DE":
                return "01522113356";
            default:
                return "12345678";
        }
    }

    @Nullable
    private String getSocialSecurityNumber() {
        String country = mCountry == null ? "" : mCountry;
        switch (country) {
            case "SE":
                return "4103219202";
            case "NO":
                return "01087000571";
            default:
                return null;
        }
    }

    @NonNull
    private String getDateOfBirth() {
        String country = mCountry == null ? "" : mCountry;
        switch (country) {
            case "NL":
                return "1970-07-10";
            case "DE":
                return "1960-07-07";
            default:
                return "1980-01-01";
        }
    }

    @NonNull
    private Address getBillingAddress() {
        Address billingAddress = new Address();

        String country = mCountry == null ? "" : mCountry;
        switch (country) {
            case "NL":
                billingAddress.setStreet("Neherkade");
                billingAddress.setHouseNumberOrName("1");
                billingAddress.setCity("Gravenhage");
                billingAddress.setCountry(country);
                billingAddress.setPostalCode("2521VA");
                billingAddress.setStateOrProvince("Noord");
                break;
            case "SE":
                billingAddress.setStreet("Stårgatan");
                billingAddress.setHouseNumberOrName("1");
                billingAddress.setCity("Ankeborg");
                billingAddress.setCountry(country);
                billingAddress.setPostalCode("12345");
                break;
            case "NO":
                billingAddress.setStreet("Sæffleberggate");
                billingAddress.setHouseNumberOrName("56");
                billingAddress.setCity("Oslo");
                billingAddress.setCountry(country);
                billingAddress.setPostalCode("0563");
                break;
            case "DE":
                billingAddress.setStreet("Hellersbergstraße");
                billingAddress.setHouseNumberOrName("14");
                billingAddress.setCity("Neuss");
                billingAddress.setCountry(country);
                billingAddress.setPostalCode("41460");
                break;
            default:
                billingAddress.setStreet("Strass");
                billingAddress.setHouseNumberOrName("1");
                billingAddress.setCity("Amsterdam");
                billingAddress.setCountry(country);
                billingAddress.setPostalCode("1234AB");
                billingAddress.setStateOrProvince("Noord");
                break;
        }
        return billingAddress;
    }

    @NonNull
    private Address getDeliveryAddress() {
        return getBillingAddress();

    }
}
