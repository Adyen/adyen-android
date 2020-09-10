/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/11/2019.
 */

package com.adyen.checkout.afterpay;

import android.annotation.SuppressLint;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.SwitchCompat;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adyen.checkout.afterpay.ui.DateOfBirthInput;
import com.adyen.checkout.afterpay.ui.R;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.ui.view.AdyenLinearLayout;
import com.adyen.checkout.base.ui.view.AdyenTextInputEditText;
import com.adyen.checkout.base.util.BrowserUtils;
import com.adyen.checkout.core.code.Lint;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

@SuppressWarnings("MethodLength")
public class AfterPayView extends AdyenLinearLayout<AfterPayOutputData, AfterPayConfiguration, PaymentComponentState, AfterPayComponent>
        implements Observer<AfterPayOutputData>, CompoundButton.OnCheckedChangeListener {
    private static final String TAG = LogUtil.getTag();

    @SuppressLint(Lint.SYNTHETIC)
    AfterPayInputData mInputData = new AfterPayInputData();
    AfterPayPersonalDataInputData mPersonalDataInputData = new AfterPayPersonalDataInputData();
    AfterPayAddressInputData mBillingAddressInputData = new AfterPayAddressInputData();
    AfterPayAddressInputData mDeliveryAddressInputData = new AfterPayAddressInputData();

    @SuppressLint(Lint.SYNTHETIC)
    TextView mPersonalDetailSummery;
    @SuppressLint(Lint.SYNTHETIC)
    TextView mBillingAddressSummery;
    @SuppressLint(Lint.SYNTHETIC)
    TextView mDeliveryAddressSummery;

    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mFirstNameInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mLastNameInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mPhoneNumberInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mEmailAddressInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mDateOfBirthInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mStreetInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mHouseNumberInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mPostalCodeInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mCityInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mCountryInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mDeliveryStreetInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mDeliveryHouseNumberInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mDeliveryPostalCodeInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mDeliveryCityInputLayout;
    @SuppressLint(Lint.SYNTHETIC)
    TextInputLayout mDeliveryCountryInputLayout;

    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mFirstNameEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mLastNameEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mTelephoneNumberEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mEmailAddressEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mStreetEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mHouseNumberEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mPostalCodeEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mCityEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mCountryInputEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mDeliveryStreetEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mDeliveryHouseNumberEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mDeliveryPostalCodeEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mDeliveryCityEditText;
    @SuppressLint(Lint.SYNTHETIC)
    AdyenTextInputEditText mDeliveryCountryInputEditText;

    @SuppressLint(Lint.SYNTHETIC)
    TabLayout mGenderTabLayout;

    @SuppressLint(Lint.SYNTHETIC)
    DateOfBirthInput mDateOfBirthEditText;

    AppCompatCheckBox mAgreementCheckBox;

    SwitchCompat mSeparateDeliverySwitch;

    View mDeliveryAddressTitle;
    View mDeliveryAddressSection1;
    View mDeliveryAddressSection2;

    TextView mAgreementText;

    public AfterPayView(@NonNull Context context) {
        this(context, null);
    }

    public AfterPayView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // Regular View constructor
    @SuppressWarnings("JavadocMethod")
    public AfterPayView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.afterpay_view, this, true);

        final int padding = (int) getResources().getDimension(R.dimen.standard_margin);
        setPadding(padding, padding, padding, 0);
    }

    @Override
    public void initView() {
        mPersonalDetailSummery = findViewById(R.id.textView_personalDetailSummery);
        mBillingAddressSummery = findViewById(R.id.textView_billingAddressSummery);
        mDeliveryAddressSummery = findViewById(R.id.textView_deliveryAddressSummery);

        bindPersonalDetails();
        bindBillingAddress();
        bindDeliveryAddress();

        mDeliveryAddressTitle = findViewById(R.id.delivery_address_title);
        mDeliveryAddressSection1 = findViewById(R.id.delivery_address_section1);
        mDeliveryAddressSection2 = findViewById(R.id.delivery_address_section2);

        mSeparateDeliverySwitch = findViewById(R.id.separate_delivery_address);
        mSeparateDeliverySwitch.setChecked(false);
        mSeparateDeliverySwitch.setOnCheckedChangeListener(this);

        mAgreementText = findViewById(R.id.agreement_text);
        mAgreementText.setMovementMethod(LinkMovementMethod.getInstance());

        mAgreementCheckBox = findViewById(R.id.checkbox_agreement);
        mAgreementCheckBox.setOnCheckedChangeListener(this);

        final AfterPayConfiguration configuration = getComponent().getConfiguration();
        checkPersonalDetailsConfiguration(configuration.getPersonalDetailsVisibility());
        checkBillingAddressDetailsConfiguration(configuration.getBillingAddressVisibility());

        setInitInputData();
    }

    @Override
    protected void initLocalizedStrings(@NonNull final Context localizedContext) {
        final AfterPayConfiguration.CountryCode countryLocale = getComponent().getConfiguration().getCountryCode();

        final int agreementResId = countryLocale == AfterPayConfiguration.CountryCode.NL
                ? R.string.checkout_afterpay_agreement_nl_nl
                : R.string.checkout_afterpay_agreement_be_be;

        final String conditionText = localizedContext.getString(R.string.checkout_afterpay_agreement_text_condition);
        final SpannableString spannableAgreementText = new SpannableString(
                localizedContext.getString(R.string.checkout_afterpay_agreement_text, conditionText));

        final int startOfLinkSpan = spannableAgreementText.toString().indexOf(conditionText);
        final int endOfLinkSpan = startOfLinkSpan + conditionText.length();

        spannableAgreementText.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                getContext().startActivity(BrowserUtils.createBrowserIntent(Uri.parse(localizedContext.getString(agreementResId))));
            }
        }, startOfLinkSpan, endOfLinkSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        mAgreementText.setText(spannableAgreementText);

        // TODO implement the individual strings for standalone component.
    }

    private void bindPersonalDetails() {
        mInputData.setPersonalDataInputData(mPersonalDataInputData);

        mFirstNameInputLayout = findViewById(R.id.textInputLayout_firstName);
        mLastNameInputLayout = findViewById(R.id.textInputLayout_lastName);
        mDateOfBirthInputLayout = findViewById(R.id.textInputLayout_dateOfBirth);
        mPhoneNumberInputLayout = findViewById(R.id.textInputLayout_phoneNumber);
        mEmailAddressInputLayout = findViewById(R.id.textInputLayout_emailAddress);

        mFirstNameEditText = (AdyenTextInputEditText) mFirstNameInputLayout.getEditText();
        mLastNameEditText = (AdyenTextInputEditText) mLastNameInputLayout.getEditText();
        mTelephoneNumberEditText = (AdyenTextInputEditText) mPhoneNumberInputLayout.getEditText();
        mEmailAddressEditText = (AdyenTextInputEditText) mEmailAddressInputLayout.getEditText();

        mDateOfBirthEditText = (DateOfBirthInput) mDateOfBirthInputLayout.getEditText();
        mGenderTabLayout = findViewById(R.id.tabLayout_gender);

        mFirstNameEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mPersonalDataInputData.setFirstName(editable.toString());
                notifyInputDataChanged();
            }
        });

        mFirstNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mFirstNameInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayPersonalDataOutputData personalDataOutputData = outputData.getAfterPayPersonalDataOutputData();

                    if (!hasFocus && !personalDataOutputData.getFirstNameField().isValid()) {
                        mFirstNameInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mFirstNameInputLayout.getHint()));
                    }
                }
            }
        });

        mLastNameEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mPersonalDataInputData.setLastName(editable.toString());
                notifyInputDataChanged();
            }
        });

        mLastNameEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mLastNameInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayPersonalDataOutputData personalDataOutputData = outputData.getAfterPayPersonalDataOutputData();
                    if (!hasFocus && !personalDataOutputData.getLastNameField().isValid()) {
                        mLastNameInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mLastNameInputLayout.getHint()));
                    }
                }
            }
        });

        mTelephoneNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mPersonalDataInputData.setTelephoneNumber(editable.toString());
                notifyInputDataChanged();
            }
        });

        mTelephoneNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mPhoneNumberInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayPersonalDataOutputData personalDataOutputData = outputData.getAfterPayPersonalDataOutputData();
                    if (!hasFocus && !personalDataOutputData.getTelephoneNumberField().isValid()) {
                        mPhoneNumberInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mPhoneNumberInputLayout.getHint()));
                    }
                }
            }
        });

        mEmailAddressEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mPersonalDataInputData.setShopperEmail(editable.toString());
                notifyInputDataChanged();
            }
        });

        mEmailAddressEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mEmailAddressInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayPersonalDataOutputData personalDataOutputData = outputData.getAfterPayPersonalDataOutputData();
                    if (!hasFocus && !personalDataOutputData.getShopperEmailField().isValid()) {
                        mEmailAddressInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mEmailAddressInputLayout.getHint()));
                    }
                }
            }
        });

        mDateOfBirthEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mPersonalDataInputData.setDateOfBirth(mDateOfBirthEditText.getCalendar());
                notifyInputDataChanged();
            }
        });

        mDateOfBirthInputLayout.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mDateOfBirthInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayPersonalDataOutputData personalDataOutputData = outputData.getAfterPayPersonalDataOutputData();
                    if (!hasFocus && !personalDataOutputData.getDateOfBirthField().isValid()) {
                        mDateOfBirthInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mDateOfBirthInputLayout.getHint()));
                    }
                }
            }
        });

        mGenderTabLayout.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                final Gender gender = tab.getPosition() == 0 ? Gender.M : Gender.F;
                mPersonalDataInputData.setGender(gender);
                notifyInputDataChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                this.onTabSelected(tab);
            }
        });
    }

    private void bindBillingAddress() {
        mInputData.setBillingAddressInputData(mBillingAddressInputData);

        mStreetInputLayout = findViewById(R.id.textInputLayout_street);
        mHouseNumberInputLayout = findViewById(R.id.textInputLayout_house_number);
        mPostalCodeInputLayout = findViewById(R.id.textInputLayout_postal_code);
        mCityInputLayout = findViewById(R.id.textInputLayout_city);
        mCountryInputLayout = findViewById(R.id.textInputLayout_country);

        mStreetEditText = (AdyenTextInputEditText) mStreetInputLayout.getEditText();
        mHouseNumberEditText = (AdyenTextInputEditText) mHouseNumberInputLayout.getEditText();
        mPostalCodeEditText = (AdyenTextInputEditText) mPostalCodeInputLayout.getEditText();
        mCityEditText = (AdyenTextInputEditText) mCityInputLayout.getEditText();
        mCountryInputEditText = (AdyenTextInputEditText) mCountryInputLayout.getEditText();
        mCountryInputLayout.setEnabled(false);

        mStreetEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mBillingAddressInputData.setStreet(editable.toString());
                notifyInputDataChanged();
            }
        });

        mStreetEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mStreetInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getBillingAddressOutputData();

                    if (!hasFocus && !addressOutputData.getStreet().isValid()) {
                        mStreetInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mStreetInputLayout.getHint()));
                    }
                }
            }
        });

        mHouseNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mBillingAddressInputData.setHouseNumberOrName(editable.toString());
                notifyInputDataChanged();
            }
        });

        mHouseNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mHouseNumberInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getBillingAddressOutputData();

                    if (!hasFocus && !addressOutputData.getHouseNumberOrName().isValid()) {
                        mHouseNumberInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mHouseNumberInputLayout.getHint()));
                    }
                }
            }
        });

        mPostalCodeEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mBillingAddressInputData.setPostalCode(editable.toString());
                notifyInputDataChanged();
            }
        });

        mPostalCodeEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mPostalCodeInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getBillingAddressOutputData();

                    if (!hasFocus && !addressOutputData.getPostalCode().isValid()) {
                        mPostalCodeInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mPostalCodeInputLayout.getHint()));
                    }
                }
            }
        });

        mCityEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mBillingAddressInputData.setCity(editable.toString());
                notifyInputDataChanged();
            }
        });

        mCityEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mCityInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getBillingAddressOutputData();

                    if (!hasFocus && !addressOutputData.getCity().isValid()) {
                        mCityInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mCityInputLayout.getHint()));
                    }
                }
            }
        });

        mCountryInputEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mBillingAddressInputData.setLocale(getComponent().getConfiguration().getCountryCode().getLocale());
                notifyInputDataChanged();
            }
        });

        mCountryInputEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mCountryInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getBillingAddressOutputData();

                    if (!hasFocus && !addressOutputData.getLocale().isValid()) {
                        mCountryInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mCountryInputLayout.getHint()));
                    }
                }
            }
        });
    }

    private void bindDeliveryAddress() {
        mInputData.setDeliveryAddressInputData(mDeliveryAddressInputData);

        mDeliveryStreetInputLayout = findViewById(R.id.textInputLayout_delivery_street);
        mDeliveryHouseNumberInputLayout = findViewById(R.id.textInputLayout_delivery_house_number);
        mDeliveryPostalCodeInputLayout = findViewById(R.id.textInputLayout_delivery_postal_code);
        mDeliveryCityInputLayout = findViewById(R.id.textInputLayout_delivery_city);
        mDeliveryCountryInputLayout = findViewById(R.id.textInputLayout_delivery_country);

        mDeliveryStreetEditText = (AdyenTextInputEditText) mDeliveryStreetInputLayout.getEditText();
        mDeliveryHouseNumberEditText = (AdyenTextInputEditText) mDeliveryHouseNumberInputLayout.getEditText();
        mDeliveryPostalCodeEditText = (AdyenTextInputEditText) mDeliveryPostalCodeInputLayout.getEditText();
        mDeliveryCityEditText = (AdyenTextInputEditText) mDeliveryCityInputLayout.getEditText();
        mDeliveryCountryInputEditText = (AdyenTextInputEditText) mDeliveryCountryInputLayout.getEditText();
        mDeliveryCountryInputEditText.setEnabled(false);

        mDeliveryStreetEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mDeliveryAddressInputData.setStreet(editable.toString());
                notifyInputDataChanged();
            }
        });

        mDeliveryStreetEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mDeliveryStreetInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getDeliveryAddressOutputData();

                    if (!hasFocus && !addressOutputData.getStreet().isValid()) {
                        mDeliveryStreetInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mDeliveryStreetInputLayout.getHint()));
                    }
                }
            }
        });

        mDeliveryHouseNumberEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mDeliveryAddressInputData.setHouseNumberOrName(editable.toString());
                notifyInputDataChanged();
            }
        });

        mDeliveryHouseNumberEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mDeliveryHouseNumberInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getDeliveryAddressOutputData();

                    if (!hasFocus && !addressOutputData.getHouseNumberOrName().isValid()) {
                        mDeliveryHouseNumberInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mDeliveryHouseNumberInputLayout.getHint()));
                    }
                }
            }
        });

        mDeliveryPostalCodeEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mDeliveryAddressInputData.setPostalCode(editable.toString());
                notifyInputDataChanged();
            }
        });

        mDeliveryPostalCodeEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mDeliveryPostalCodeInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getDeliveryAddressOutputData();

                    if (!hasFocus && !addressOutputData.getPostalCode().isValid()) {
                        mDeliveryPostalCodeInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mDeliveryPostalCodeInputLayout.getHint()));
                    }
                }
            }
        });

        mDeliveryCityEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mDeliveryAddressInputData.setCity(editable.toString());
                notifyInputDataChanged();
            }
        });

        mDeliveryCityEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mDeliveryCityInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getDeliveryAddressOutputData();

                    if (!hasFocus && !addressOutputData.getCity().isValid()) {
                        mDeliveryCityInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mDeliveryCityEditText.getHint()));
                    }
                }
            }
        });

        mDeliveryCountryInputEditText.setOnChangeListener(new AdyenTextInputEditText.Listener() {
            @Override
            public void onTextChanged(@NonNull Editable editable) {
                mDeliveryAddressInputData.setLocale(getComponent().getConfiguration().getCountryCode().getLocale());
                notifyInputDataChanged();
            }
        });

        mDeliveryCountryInputEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                mDeliveryCountryInputLayout.setErrorEnabled(!hasFocus);

                final AfterPayOutputData outputData = getComponent().getOutputData();
                if (outputData != null) {
                    final AfterPayAddressOutputData addressOutputData = outputData.getDeliveryAddressOutputData();

                    if (!hasFocus && !addressOutputData.getLocale().isValid()) {
                        mDeliveryCountryInputLayout.setError(
                                getContext().getString(R.string.checkout_afterpay_is_not_valid, mDeliveryCountryInputLayout.getHint()));
                    }
                }
            }
        });
    }

    private void checkDeliveryAddressDetailsConfiguration(AfterPayConfiguration.VisibilityState state, boolean isSeparateDeliverEnable) {
        final int visibilityStatus = isSeparateDeliverEnable ? View.VISIBLE : View.GONE;
        switch (state) {
            case READ_ONLY:
            case HIDDEN:
                mDeliveryStreetInputLayout.setVisibility(GONE);
                mDeliveryHouseNumberInputLayout.setVisibility(GONE);
                mDeliveryPostalCodeInputLayout.setVisibility(GONE);
                mDeliveryCityInputLayout.setVisibility(GONE);
                mDeliveryCountryInputLayout.setVisibility(GONE);

                if (state == AfterPayConfiguration.VisibilityState.READ_ONLY && isSeparateDeliverEnable) {
                    mDeliveryAddressSummery.setVisibility(visibilityStatus);
                    mDeliveryAddressTitle.setVisibility(visibilityStatus);
                } else {
                    mDeliveryAddressSummery.setVisibility(visibilityStatus);
                    mDeliveryAddressTitle.setVisibility(visibilityStatus);
                }

                break;
            case EDITABLE:
                mDeliveryAddressTitle.setVisibility(visibilityStatus);
                mDeliveryAddressSection1.setVisibility(visibilityStatus);
                mDeliveryAddressSection2.setVisibility(visibilityStatus);
                mDeliveryCountryInputLayout.setVisibility(visibilityStatus);
                break;
            default:
                // unknown state
        }
    }

    private void checkBillingAddressDetailsConfiguration(AfterPayConfiguration.VisibilityState state) {
        switch (state) {
            case READ_ONLY:
            case HIDDEN:
                mStreetInputLayout.setVisibility(GONE);
                mHouseNumberInputLayout.setVisibility(GONE);
                mPostalCodeInputLayout.setVisibility(GONE);
                mCityInputLayout.setVisibility(GONE);
                mCountryInputLayout.setVisibility(GONE);

                if (state == AfterPayConfiguration.VisibilityState.READ_ONLY) {
                    mBillingAddressSummery.setVisibility(VISIBLE);
                }

                break;
            default:
                // unknown state
        }
    }

    private void checkPersonalDetailsConfiguration(AfterPayConfiguration.VisibilityState state) {
        switch (state) {
            case READ_ONLY:
            case HIDDEN:
                mFirstNameInputLayout.setVisibility(GONE);
                mLastNameInputLayout.setVisibility(GONE);
                mDateOfBirthInputLayout.setVisibility(GONE);
                mPhoneNumberInputLayout.setVisibility(GONE);
                mEmailAddressInputLayout.setVisibility(GONE);
                mGenderTabLayout.setVisibility(GONE);

                if (state == AfterPayConfiguration.VisibilityState.READ_ONLY) {
                    mPersonalDetailSummery.setVisibility(VISIBLE);
                }

                break;
            default:
                // unknown state
        }
    }

    private void setInitInputData() {
        final AfterPayInputData initInputData = getComponent().getInitInputData();
        final AfterPayPersonalDataInputData personalDataInputData = initInputData.getPersonalDataInputData();
        final AfterPayAddressInputData billingAddressInputData = initInputData.getBillingAddressInputData();
        final AfterPayAddressInputData deliveryAddressInputData = initInputData.getDeliveryAddressInputData();

        if (personalDataInputData != null) {
            mFirstNameEditText.setText(personalDataInputData.getFirstName());
            mLastNameEditText.setText(personalDataInputData.getLastName());
            mTelephoneNumberEditText.setText(personalDataInputData.getTelephoneNumber());
            mEmailAddressEditText.setText(personalDataInputData.getShopperEmail());
            mDateOfBirthEditText.setDate(personalDataInputData.getDateOfBirth());
            setGender(personalDataInputData.getGender());

            final String personalDetailPreview = getResources().getString(R.string.checkout_afterpay_personal_details_summery,
                    personalDataInputData.getFirstName(),
                    personalDataInputData.getLastName(),
                    personalDataInputData.getShopperEmail(),
                    personalDataInputData.getTelephoneNumber());

            mPersonalDetailSummery.setText(personalDetailPreview);
        }

        if (billingAddressInputData != null) {
            mStreetEditText.setText(billingAddressInputData.getStreet());
            mHouseNumberEditText.setText(billingAddressInputData.getHouseNumberOrName());
            mPostalCodeEditText.setText(billingAddressInputData.getPostalCode());
            mCityEditText.setText(billingAddressInputData.getCity());
            mCountryInputEditText.setText(billingAddressInputData.getLocale().getDisplayCountry());

            final String billingAddressPreview = getResources().getString(R.string.checkout_afterpay_address_summery,
                    billingAddressInputData.getStreet(),
                    billingAddressInputData.getHouseNumberOrName(),
                    billingAddressInputData.getPostalCode(),
                    billingAddressInputData.getCity(),
                    billingAddressInputData.getLocale());

            mBillingAddressSummery.setText(billingAddressPreview);
        }

        if (deliveryAddressInputData != null) {
            mDeliveryStreetEditText.setText(deliveryAddressInputData.getStreet());
            mDeliveryHouseNumberEditText.setText(deliveryAddressInputData.getHouseNumberOrName());
            mDeliveryPostalCodeEditText.setText(deliveryAddressInputData.getPostalCode());
            mDeliveryCityEditText.setText(deliveryAddressInputData.getCity());
            mDeliveryCountryInputEditText.setText(deliveryAddressInputData.getLocale().getDisplayCountry());

            final String deliveryAddressPreview = getResources().getString(R.string.checkout_afterpay_address_summery,
                    deliveryAddressInputData.getStreet(),
                    deliveryAddressInputData.getHouseNumberOrName(),
                    deliveryAddressInputData.getPostalCode(),
                    deliveryAddressInputData.getCity(),
                    deliveryAddressInputData.getLocale());

            mDeliveryAddressSummery.setText(deliveryAddressPreview);
        }

        mSeparateDeliverySwitch.setChecked(initInputData.isSeparateDeliveryAddressEnable());
    }

    @Override
    public void onChanged(@Nullable AfterPayOutputData afterPayOutputData) {
        Logger.v(TAG, "sepaOutputData changed");
    }

    @Override
    public void onComponentAttached() {
        // nothing to do
    }

    @Override
    protected void observeComponentChanges(@NonNull LifecycleOwner lifecycleOwner) {
        getComponent().observeOutputData(lifecycleOwner, this);
    }

    @Override
    public boolean isConfirmationRequired() {
        return true;
    }

    @Override
    public void highlightValidationErrors() {
        // TODO: 2020-01-08 IMPLEMENT
    }

    @SuppressLint(Lint.SYNTHETIC)
    void notifyInputDataChanged() {
        getComponent().inputDataChanged(mInputData);
    }

    @Override
    public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
        final int viewId = buttonView.getId();

        if (viewId == R.id.checkbox_agreement) {
            mInputData.setAgreementChecked(isChecked);
        } else if (viewId == R.id.separate_delivery_address) {
            mInputData.setSeparateDeliveryAddress(isChecked);
            checkDeliveryAddressDetailsConfiguration(getComponent().getConfiguration().getDeliveryAddressVisibility(), isChecked);
        }

        notifyInputDataChanged();
    }

    private void setGender(Gender gender) {
        final int index = (gender == Gender.M) ? 0 : 1;
        final TabLayout.Tab tab = mGenderTabLayout.getTabAt(index);
        if (tab != null) {
            tab.select();
        }
    }
}
