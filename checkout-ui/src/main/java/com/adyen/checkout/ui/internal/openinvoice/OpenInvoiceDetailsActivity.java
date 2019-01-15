/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/10/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice;

import static com.adyen.checkout.core.model.OpenInvoiceDetails.KEY_BILLING_ADDRESS;
import static com.adyen.checkout.core.model.OpenInvoiceDetails.KEY_CONSENT_CHECKBOX;
import static com.adyen.checkout.core.model.OpenInvoiceDetails.KEY_DELIVERY_ADDRESS;
import static com.adyen.checkout.core.model.OpenInvoiceDetails.KEY_PERSONAL_DETAILS;
import static com.adyen.checkout.core.model.OpenInvoiceDetails.KEY_SEPARATE_DELIVERY_ADDRESS;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.Observer;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.SearchHandler;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.Address;
import com.adyen.checkout.core.model.FieldSetConfiguration;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.KlarnaDetails;
import com.adyen.checkout.core.model.KlarnaSsnLookupResponse;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.core.model.PersonalDetails;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.openinvoice.view.AddressDetailsInputLayout;
import com.adyen.checkout.ui.internal.openinvoice.view.InputDetailsGroupLayout;
import com.adyen.checkout.ui.internal.openinvoice.view.PersonalDetailsInputLayout;
import com.adyen.checkout.util.LocaleUtil;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.List;
import java.util.Locale;

public class OpenInvoiceDetailsActivity extends CheckoutDetailsActivity implements InputDetailsGroupLayout.ValidationChangeListener {
    private static final String EXTRA_PAYMENT_METHOD = "EXTRA_PAYMENT_METHOD";

    private static final String KLARNA_CONSENT_URL = "https://cdn.klarna.com/1.0/shared/content/legal/terms/2/de_de/consent";
    private static final String KLARNA_MORE_INFO_URL = "https://cdn.klarna.com/1.0/shared/content/legal/terms/2/%s/invoice";

    private static final String AFTERPAY_NL_CONSENT_URL = "https://www.afterpay.nl/nl/algemeen/betalen-met-afterpay/betalingsvoorwaarden";
    private static final String AFTERPAY_BE_CONSENT_URL = "https://www.afterpay.be/be/footer/betalen-met-afterpay/betalingsvoorwaarden";
    private static final String AFTERPAY_US_CONSENT_URL = "https://www.afterpay.nl/en/algemeen/pay-with-afterpay/payment-conditions";

    private PaymentMethod mPaymentMethod;
    private SearchHandler<KlarnaSsnLookupResponse> mSearchHandler;

    private RelativeLayout mSsnLookupLayout;
    private LinearLayout mDetailsInputLayout;
    private PersonalDetailsInputLayout mPersonalDetailsLayout;
    private AddressDetailsInputLayout mBillingAddressDetailsLayout;
    private LinearLayout mSeparateDeliveryAddressLayout;
    private AddressDetailsInputLayout mDeliveryAddressDetailsLayout;
    private LinearLayout mConsentLayout;

    private EditText mSsnLookupEditText;
    private ContentLoadingProgressBar mSsnLookupProgress;
    private SwitchCompat mSeparateDeliveryAddressSwitch;
    private TextView mConsentText;
    private SwitchCompat mConsentSwitch;
    private Button mPayButton;
    private TextView mSurchargeTextView;

    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull PaymentReference paymentReference,
            @NonNull PaymentMethod paymentMethod
    ) {
        Intent intent = new Intent(context, OpenInvoiceDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_PAYMENT_METHOD, paymentMethod);

        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_invoice_details);

        mPaymentMethod = getIntent().getParcelableExtra(EXTRA_PAYMENT_METHOD);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mPaymentMethod.getName());
        }

        findViews();

        try {
            setup();
        } catch (CheckoutException e) {
            handleCheckoutException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPersonalDetailsLayout.removeValidationChangeListener(this);
        mBillingAddressDetailsLayout.removeValidationChangeListener(this);
        mDeliveryAddressDetailsLayout.removeValidationChangeListener(this);
    }

    private void findViews() {
        mSsnLookupLayout = findViewById(R.id.layout_ssnLookup);
        mSsnLookupProgress = findViewById(R.id.progressBar_ssnLookup);
        mDetailsInputLayout = findViewById(R.id.layout_details_input);
        mSsnLookupEditText = findViewById(R.id.editText_ssnLookup);
        mPersonalDetailsLayout = findViewById(R.id.layout_personal_details_input);
        mBillingAddressDetailsLayout = findViewById(R.id.layout_billing_address_input);
        mSeparateDeliveryAddressLayout = findViewById(R.id.layout_separate_delivery_address);
        mSeparateDeliveryAddressSwitch = findViewById(R.id.switch_separate_delivery_address);
        mDeliveryAddressDetailsLayout = findViewById(R.id.layout_delivery_address_input);
        mConsentLayout = findViewById(R.id.layout_terms_and_conditions);
        mConsentText = findViewById(R.id.textView_terms_and_conditions);
        mConsentSwitch = findViewById(R.id.switch_terms_and_conditions);
        mPayButton = findViewById(R.id.button_pay);
        mSurchargeTextView = findViewById(R.id.textView_surcharge);
    }

    private void setup() throws CheckoutException {
        setupPayButton();
        setupInputLayout();
        setupSSnLookup();
        checkValidation();
        setupMoreInformationButton();
    }

    private void setupPayButton() {
        PayButtonUtil.setPayButtonText(this, mPaymentMethod, mPayButton, mSurchargeTextView);
        mPayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areAllDetailsValid()) {
                    try {
                        initiatePayment();
                    } catch (CheckoutException e) {
                        handleCheckoutException(e);
                    }
                }
            }
        });
    }

    private boolean isKlarna() {
        return PaymentMethodTypes.KLARNA.equals(mPaymentMethod.getType());
    }

    private boolean isAfterpay() {
        return PaymentMethodTypes.AFTERPAY.equals(mPaymentMethod.getType());
    }

    private void setupSSnLookup() {
        if (mPersonalDetailsLayout.getFormVisibility() == FieldSetConfiguration.FieldVisibility.READ_ONLY) {
            return;
        }

        getPaymentHandler().getPaymentSessionObservable().observe(this, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                mSearchHandler = SearchHandler.Factory.createKlarnaSsnLookupSearchHandler(getApplication(), paymentSession, mPaymentMethod);
                if (mSearchHandler == null) {
                    return;
                }

                mSearchHandler.getSearchResultsObservable().observe(OpenInvoiceDetailsActivity.this, new Observer<KlarnaSsnLookupResponse>() {
                    @Override
                    public void onChanged(@NonNull KlarnaSsnLookupResponse klarnaSsnLookupResponse) {
                        mSsnLookupProgress.hide();
                        mDetailsInputLayout.setVisibility(View.VISIBLE);
                        populateSsnLookupData(klarnaSsnLookupResponse);
                    }
                });

                mSearchHandler.getErrorObservable().observe(OpenInvoiceDetailsActivity.this, new Observer<CheckoutException>() {
                    @Override
                    public void onChanged(@NonNull CheckoutException ssnLookupException) {
                        mSsnLookupProgress.hide();

                        //TODO 30/11/2018 show SSN Lookup error feedback
                        mDetailsInputLayout.setVisibility(View.VISIBLE);
                        checkValidation();
                    }
                });

                mSsnLookupLayout.setVisibility(View.VISIBLE);
                mDetailsInputLayout.setVisibility(View.GONE);
                mPersonalDetailsLayout.setExternalSsnField();

                AsYouTypeSsnFormatter.SsnInputCompleteCallback ssnCompleteCallback = new AsYouTypeSsnFormatter.SsnInputCompleteCallback() {
                    @Override
                    public void onSsnInputFinished(@NonNull String ssnNumber) {
                        mSsnLookupProgress.show();
                        mSearchHandler.setSearchString(ssnNumber);
                    }
                };

                mSsnLookupEditText.addTextChangedListener(new AsYouTypeSsnFormatter(ssnCompleteCallback));
                mSsnLookupEditText.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(@NonNull Editable s) {
                        checkValidation();
                    }
                });

                //get SSN from personal info if available
                InputDetail personalDetails = InputDetailImpl.findByKey(mPaymentMethod.getInputDetails(), KEY_PERSONAL_DETAILS);
                if (personalDetails != null) {
                    InputDetail ssnDetail = InputDetailImpl.findByKey(personalDetails.getChildInputDetails(),
                            PersonalDetails.KEY_SOCIAL_SECURITY_NUMBER);
                    if (ssnDetail != null && !TextUtils.isEmpty(ssnDetail.getValue())) {
                        mSsnLookupEditText.setText(ssnDetail.getValue());
                    }
                }
            }
        });
    }

    private void populateSsnLookupData(KlarnaSsnLookupResponse klarnaSsnLookupResponse) {
        mPersonalDetailsLayout.fillSsnLookupName(klarnaSsnLookupResponse.getName());
        mBillingAddressDetailsLayout.fillSsnResponseAddress(klarnaSsnLookupResponse.getAddress());

        checkValidation();
    }

    private void setupInputLayout() throws CheckoutException {
        List<InputDetail> inputDetailsList = mPaymentMethod.getInputDetails();
        if (inputDetailsList != null) {
            for (final InputDetail detail : inputDetailsList) {
                switch (detail.getKey()) {
                    case KEY_PERSONAL_DETAILS:
                        if (detail.getType() == InputDetail.Type.FIELD_SET) {
                            mPersonalDetailsLayout.setVisibility(View.VISIBLE);
                            mPersonalDetailsLayout.setTitle(R.string.checkout_personal_details_title);
                            mPersonalDetailsLayout.populateInputDetailGroup(detail);
                            mPersonalDetailsLayout.addValidationChangeListener(this);
                        }
                        break;
                    case KEY_BILLING_ADDRESS:
                        if (detail.getType() == InputDetail.Type.ADDRESS) {
                            mBillingAddressDetailsLayout.setVisibility(View.VISIBLE);
                            mBillingAddressDetailsLayout.setTitle(R.string.checkout_billing_address_title);
                            mBillingAddressDetailsLayout.populateInputDetailGroup(detail);
                            mBillingAddressDetailsLayout.addValidationChangeListener(this);
                        }
                        break;
                    case KEY_SEPARATE_DELIVERY_ADDRESS:
                        mSeparateDeliveryAddressLayout.setVisibility(View.VISIBLE);
                        //set delivery address visibility according to this toggle
                        mSeparateDeliveryAddressSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    mDeliveryAddressDetailsLayout.setVisibility(View.VISIBLE);
                                } else {
                                    mDeliveryAddressDetailsLayout.setVisibility(View.GONE);
                                }
                                checkValidation();
                            }
                        });

                        //hide toggle and show delivery address if it's read only.
                        InputDetail deliveryAddress = InputDetailImpl.findByKey(mPaymentMethod.getInputDetails(), KEY_DELIVERY_ADDRESS);
                        if (deliveryAddress != null) {
                            try {
                                FieldSetConfiguration config = deliveryAddress.getConfiguration(FieldSetConfiguration.class);
                                if (config != null && config.getFieldVisibility() == FieldSetConfiguration.FieldVisibility.READ_ONLY) {
                                    mSeparateDeliveryAddressLayout.setVisibility(View.GONE);
                                    mDeliveryAddressDetailsLayout.setVisibility(View.VISIBLE);
                                }
                            } catch (CheckoutException e) {
                                //configuration not present or malformed
                            }
                        }

                        break;
                    case KEY_DELIVERY_ADDRESS:
                        if (detail.getType() == InputDetail.Type.ADDRESS) {
                            mDeliveryAddressDetailsLayout.setTitle(R.string.checkout_delivery_address_title);
                            mDeliveryAddressDetailsLayout.populateInputDetailGroup(detail);
                            mDeliveryAddressDetailsLayout.addValidationChangeListener(this);
                        }
                        break;
                    case KEY_CONSENT_CHECKBOX:
                        setupConsent();
                        break;
                    default:
                        if (!detail.isOptional()) {
                            throw new CheckoutException.Builder("Unexpected required detail: " + detail.getKey(), null).build();
                        }
                        break;
                }
            }
        }
    }

    private void setupConsent() {
        String highlighted;
        String termsAndConditions;

        if (isKlarna()) {
            highlighted = getString(R.string.checkout_klarna_consent);
            termsAndConditions = getString(R.string.checkout_klarna_terms_and_conditions_text);
        } else if (isAfterpay()) {
            highlighted = getString(R.string.checkout_afterpay_conditions);
            termsAndConditions = getString(R.string.checkout_afterpay_terms_and_conditions_text);
        } else {
            return;
        }

        final int replacePosition = termsAndConditions.indexOf("%s");

        mConsentLayout.setVisibility(View.VISIBLE);

        if (replacePosition > -1) {
            final int endOfSpan = replacePosition + highlighted.length();
            SpannableStringBuilder spannableTermsAndConditions = new SpannableStringBuilder(String.format(termsAndConditions, highlighted));
            spannableTermsAndConditions.setSpan(new UnderlineSpan(), replacePosition, endOfSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannableTermsAndConditions.setSpan(new StyleSpan(Typeface.BOLD), replacePosition, endOfSpan, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mConsentText.setText(spannableTermsAndConditions);
        } else {
            mConsentText.setText(String.format(termsAndConditions, highlighted));
        }

        if (isKlarna()) {
            klarnaConsentClick();
        } else if (isAfterpay()) {
            afterpayConsentClick();
        }

        mConsentSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checkValidation();
            }
        });
    }

    private void klarnaConsentClick() {
        mConsentText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchBrowser(Uri.parse(KLARNA_CONSENT_URL));
            }
        });
    }

    private void afterpayConsentClick() {
        getPaymentHandler().getPaymentSessionObservable().observe(this, new Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {

                String countryCode = paymentSession.getPayment().getCountryCode().toLowerCase(Locale.ROOT);
                Locale userLocale = LocaleUtil.getLocale(OpenInvoiceDetailsActivity.this);

                boolean isSameLocaleAndCountry = countryCode.toUpperCase(Locale.ROOT).equals(userLocale.getCountry().toUpperCase(Locale.ROOT));

                final String url;
                if (isSameLocaleAndCountry && "NL".equals(countryCode.toUpperCase(Locale.ROOT))) {
                    url = AFTERPAY_NL_CONSENT_URL;
                } else if (isSameLocaleAndCountry && "BE".equals(countryCode.toUpperCase(Locale.ROOT))) {
                    url = AFTERPAY_BE_CONSENT_URL;
                } else {
                    url = AFTERPAY_US_CONSENT_URL;
                }

                mConsentText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        launchBrowser(Uri.parse(url));
                    }
                });
            }
        });


    }

    private void setupMoreInformationButton() {
        //this button only applies for Klarna payment method
        if (isKlarna()) {
            final Button moreInformationButton = findViewById(R.id.button_more_information);
            moreInformationButton.setVisibility(View.VISIBLE);

            getPaymentHandler().getPaymentSessionObservable().observe(this, new Observer<PaymentSession>() {
                @Override
                public void onChanged(@NonNull PaymentSession paymentSession) {
                    //the binding legal page is based on the country of origin
                    String countryCode = paymentSession.getPayment().getCountryCode().toLowerCase(Locale.ROOT);
                    Locale userLocale = LocaleUtil.getLocale(OpenInvoiceDetailsActivity.this);
                    //but we can show an english version if the shopper locale is not the same
                    String klarnaCountryCode;
                    if (userLocale.getCountry().toLowerCase(Locale.ROOT).equals(countryCode)) {
                        klarnaCountryCode = countryCode + "_" + countryCode;
                    } else {
                        klarnaCountryCode = "en_" + countryCode;
                    }

                    final String klarnaUrl = String.format(KLARNA_MORE_INFO_URL, klarnaCountryCode);

                    moreInformationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            launchBrowser(Uri.parse(klarnaUrl));
                        }
                    });
                }
            });
        }
    }

    private void launchBrowser(@NonNull Uri uri) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getApplicationContext(), R.string.checkout_error_redirect_failed, Toast.LENGTH_LONG).show();
        }
    }

    private boolean areAllDetailsValid() {

        boolean isValid = mPersonalDetailsLayout.isValid() && mBillingAddressDetailsLayout.isValid();

        //validate SSN Lookup field
        if (isValid && mSsnLookupLayout.getVisibility() == View.VISIBLE) {
            isValid = mSsnLookupEditText.getText().length() == AsYouTypeSsnFormatter.MAX_SIZE;
        }

        //validate delivery address
        if (isValid && mSeparateDeliveryAddressLayout.getVisibility() == View.VISIBLE
                && mSeparateDeliveryAddressSwitch.isChecked()) {
            isValid = mDeliveryAddressDetailsLayout.isValid();
        }

        //validate consent
        if (isValid && mConsentLayout.getVisibility() == View.VISIBLE) {
            isValid = mConsentSwitch.isChecked();
        }

        return isValid;
    }

    @Override
    public void onValidationChanged(boolean isValid) {
        checkValidation();
    }

    private void checkValidation() {
        mPayButton.setEnabled(areAllDetailsValid());
    }

    private void initiatePayment() throws CheckoutException {
        String ssn = null;
        if (mSsnLookupLayout.getVisibility() == View.VISIBLE) {
            ssn = mSsnLookupEditText.getText().toString().replace(AsYouTypeSsnFormatter.SEPARATOR, "");
        }

        PersonalDetails personalDetails = mPersonalDetailsLayout.getPersonalDetails(ssn);
        Address billingAddress = mBillingAddressDetailsLayout.getAddress();

        if (personalDetails != null && billingAddress != null) {
            KlarnaDetails.Builder klarnaDetailBuilder = new KlarnaDetails.Builder(
                    personalDetails,
                    billingAddress
            );

            klarnaDetailBuilder.setSeparateDeliveryAddress(mSeparateDeliveryAddressSwitch.isChecked());

            if (mConsentLayout.getVisibility() == View.VISIBLE) {
                klarnaDetailBuilder.setConsentCheckbox(mConsentSwitch.isChecked());

                Address deliveryAddress = mDeliveryAddressDetailsLayout.getAddress();
                if (deliveryAddress != null) {
                    klarnaDetailBuilder.setDeliveryAddress(deliveryAddress);
                }
            }

            getPaymentHandler().initiatePayment(mPaymentMethod, klarnaDetailBuilder.build());
        } else {
            throw new CheckoutException.Builder("Payment details are not filled correctly and cannot be built.", null).build();
        }
    }

}
