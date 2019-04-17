/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 10/08/2017.
 */

package com.adyen.checkout.ui.internal.card;

import static com.adyen.checkout.core.card.internal.CardValidatorImpl.AMEX_NUMBER_SIZE;
import static com.adyen.checkout.core.card.internal.CardValidatorImpl.GENERAL_CARD_NUMBER_SIZE;

import android.app.Application;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adyen.checkout.base.internal.Objects;
import com.adyen.checkout.core.AdditionalDetails;
import com.adyen.checkout.core.AuthenticationDetails;
import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.PaymentHandler;
import com.adyen.checkout.core.PaymentReference;
import com.adyen.checkout.core.card.Card;
import com.adyen.checkout.core.card.CardType;
import com.adyen.checkout.core.card.CardValidator;
import com.adyen.checkout.core.card.Cards;
import com.adyen.checkout.core.card.EncryptedCard;
import com.adyen.checkout.core.card.EncryptionException;
import com.adyen.checkout.core.handler.AdditionalDetailsHandler;
import com.adyen.checkout.core.handler.AuthenticationHandler;
import com.adyen.checkout.core.internal.model.ChallengeAuthentication;
import com.adyen.checkout.core.internal.model.FingerprintAuthentication;
import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.internal.model.PaymentMethodImpl;
import com.adyen.checkout.core.model.CardDetails;
import com.adyen.checkout.core.model.ChallengeDetails;
import com.adyen.checkout.core.model.CupSecurePlusDetails;
import com.adyen.checkout.core.model.FingerprintDetails;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.nfc.NfcCardReader;
import com.adyen.checkout.threeds.Card3DS2Authenticator;
import com.adyen.checkout.threeds.ChallengeResult;
import com.adyen.checkout.threeds.ThreeDS2Exception;
import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.activity.CheckoutDetailsActivity;
import com.adyen.checkout.ui.internal.common.fragment.ErrorDialogFragment;
import com.adyen.checkout.ui.internal.common.fragment.ProgressDialogFragment;
import com.adyen.checkout.ui.internal.common.util.Adapter;
import com.adyen.checkout.ui.internal.common.util.ConnectivityDelegate;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.common.util.LockToCheckmarkAnimationDelegate;
import com.adyen.checkout.ui.internal.common.util.PayButtonUtil;
import com.adyen.checkout.ui.internal.common.util.PaymentMethodUtil;
import com.adyen.checkout.ui.internal.common.util.PhoneNumberUtil;
import com.adyen.checkout.ui.internal.common.util.TextViewUtil;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;
import com.adyen.checkout.ui.internal.common.util.image.Rembrandt;
import com.adyen.checkout.ui.internal.common.util.image.Target;
import com.adyen.checkout.ui.internal.common.util.recyclerview.CheckoutItemAnimator;
import com.adyen.checkout.ui.internal.common.util.recyclerview.SpacingItemDecoration;
import com.adyen.checkout.ui.internal.common.view.CustomTextInputLayout;
import com.adyen.checkout.util.PaymentMethodTypes;
import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class CardDetailsActivity extends CheckoutDetailsActivity
        implements View.OnClickListener, NfcCardReaderTutorialFragment.Listener, DialogInterface.OnDismissListener, AuthenticationHandler {
    private static final String EXTRA_TARGET_PAYMENT_METHOD = "EXTRA_TARGET_PAYMENT_METHOD";

    private static final int CARD_NUMBER_BLOCK_LENGTH = 4;

    private EditText mHolderNameEditText;

    private EditText mCardNumberEditText;

    private LockToCheckmarkAnimationDelegate mLockToCheckmarkAnimationDelegate;

    private EditText mExpiryDateEditText;

    private EditText mSecurityCodeEditText;

    private EditText mPhoneNumberEditText;

    private View mInstallmentsContainer;

    private Spinner mInstallmentsSpinner;

    private SwitchCompat mStoreDetailsSwitchCompat;

    private Button mPayButton;

    private TextView mSurchargeTextView;

    private PaymentMethod mTargetPaymentMethod;

    private List<PaymentMethod> mAllowedPaymentMethods;

    private NfcCardReader mNfcCardReader;

    private Card3DS2Authenticator mCard3DS2Authenticator;

    private ConnectivityDelegate mConnectivityDelegate;

    @NonNull
    public static Intent newIntent(
            @NonNull Context context,
            @NonNull PaymentReference paymentReference,
            @NonNull PaymentMethod targetPaymentMethod
    ) {
        Intent intent = new Intent(context, CardDetailsActivity.class);
        intent.putExtra(EXTRA_PAYMENT_REFERENCE, paymentReference);
        intent.putExtra(EXTRA_TARGET_PAYMENT_METHOD, targetPaymentMethod);

        return intent;
    }

    @Override
    public void onClick(@NonNull View view) {
        if (view == mPayButton) {
            try {
                CardDetails cardDetails = buildCardDetails();

                if (cardDetails != null) {
                    getPaymentHandler().initiatePayment(mTargetPaymentMethod, cardDetails);
                }
            } catch (EncryptionException e) {
                ErrorDialogFragment
                        .newInstance(this, e)
                        .showIfNotShown(getSupportFragmentManager());
            }
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        View focus = getWindow().getDecorView().findFocus();

        if (focus != null) {
            KeyboardUtil.show(focus);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mLockToCheckmarkAnimationDelegate.onTextChanged();
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTargetPaymentMethod = getIntent().getParcelableExtra(EXTRA_TARGET_PAYMENT_METHOD);

        setContentView(R.layout.activity_card_details);
        setTitle(mTargetPaymentMethod.getName());

        findViewById(android.R.id.content).setVisibility(View.GONE);

        mHolderNameEditText = findViewById(R.id.editText_holderName);
        mCardNumberEditText = findViewById(R.id.editText_cardNumber);
        mLockToCheckmarkAnimationDelegate = new LockToCheckmarkAnimationDelegate(
                mCardNumberEditText,
                new LockToCheckmarkAnimationDelegate.ValidationCallback() {
                    @Override
                    public boolean isValid() {
                        return isCardNumberValidForUser();
                    }
                });
        mExpiryDateEditText = findViewById(R.id.editText_expiryDate);
        mSecurityCodeEditText = findViewById(R.id.editText_securityCode);
        mPhoneNumberEditText = findViewById(R.id.editText_phoneNumber);
        mInstallmentsContainer = findViewById(R.id.linearLayout_installmentsContainer);
        mStoreDetailsSwitchCompat = findViewById(R.id.switchCompat_storeDetails);
        mPayButton = findViewById(R.id.button_pay);
        mSurchargeTextView = findViewById(R.id.textView_surcharge);

        try {
            mNfcCardReader = NfcCardReader.getInstance(this, new NfcCardReaderListener());
        } catch (NoClassDefFoundError e) {
            mNfcCardReader = null;
        }

        mConnectivityDelegate = new ConnectivityDelegate(this, new Observer<NetworkInfo>() {
            @Override
            public void onChanged(@Nullable NetworkInfo networkInfo) {
                updatePayButtonState();
            }
        });

        PaymentHandler paymentHandler = getPaymentHandler();
        final Observable<PaymentSession> paymentSessionObservable = paymentHandler.getPaymentSessionObservable();
        paymentSessionObservable.observe(this, new com.adyen.checkout.core.Observer<PaymentSession>() {
            @Override
            public void onChanged(@NonNull PaymentSession paymentSession) {
                mAllowedPaymentMethods = getAllowedPaymentMethods(paymentSession);

                boolean initialLaunch = savedInstanceState == null;
                boolean holderNameViewHasRequestedFocus = setupHolderNameViews(initialLaunch);
                setupCardNumberEditText(!holderNameViewHasRequestedFocus && initialLaunch);
                setupCardLogoViews();
                setupExpiryDateEditText();
                setupSecurityCodeViews();
                setupPhoneNumberViews();
                setupInstallmentViews();
                setupStoreDetailsSwitchCompat();
                setupPayButton();

                updatePayButtonState();

                findViewById(android.R.id.content).setVisibility(View.VISIBLE);
                paymentSessionObservable.removeObserver(this);
            }
        });

        try {
            mCard3DS2Authenticator = new Card3DS2Authenticator(this);
            paymentHandler.setAuthenticationHandler(this, this);
        } catch (NoClassDefFoundError e) {
            mCard3DS2Authenticator = null;
        }

        paymentHandler.setAdditionalDetailsHandler(this, new AdditionalDetailsHandler() {
            @Override
            public void onAdditionalDetailsRequired(@NonNull AdditionalDetails additionalDetails) {
                if (!PaymentMethodTypes.CUP.equals(additionalDetails.getPaymentMethodType())) {
                    return;
                }

                List<InputDetail> inputDetails = additionalDetails.getInputDetails();

                if (inputDetails.size() != 1 || !CupSecurePlusDetails.KEY_SMS_CODE.equals(inputDetails.get(0).getKey())) {
                    return;
                }

                PaymentMethod paymentMethod = PaymentMethodImpl.findByType(mAllowedPaymentMethods, PaymentMethodTypes.CUP);

                if (paymentMethod == null) {
                    return;
                }

                CardDetailsActivity context = CardDetailsActivity.this;
                Intent intent = CupSecurePlusDetailsActivity.newIntent(context, getPaymentReference(), paymentMethod, additionalDetails);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mNfcCardReader != null) {
            mNfcCardReader.enableWithSounds(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mNfcCardReader != null) {
            mNfcCardReader.disable();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCard3DS2Authenticator != null) {
            mCard3DS2Authenticator.release();
        }
    }

    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_details, menu);

        MenuItem cardReaderMenuItem = menu.findItem(R.id.action_card_reader_tutorial);

        if (mNfcCardReader != null) {
            cardReaderMenuItem.setVisible(true);
            Context themedActionBarContext = ThemeUtil.getThemedActionBarContext(this);
            ThemeUtil.setTintFromAttributeColor(themedActionBarContext, cardReaderMenuItem.getIcon(), R.attr.colorIconActiveFocused);
        } else {
            cardReaderMenuItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_card_reader_tutorial) {
            showCardReaderTutorialFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isNfcEnabledOnDevice() {
        return mNfcCardReader != null && mNfcCardReader.isNfcEnabledOnDevice();
    }

    @Override
    public void onAuthenticationDetailsRequired(@NonNull AuthenticationDetails authenticationDetails) {
        if (mCard3DS2Authenticator.isReleased()) {
            mCard3DS2Authenticator = new Card3DS2Authenticator(this);
        }

        try {
            switch (authenticationDetails.getResultCode()) {
                case IDENTIFY_SHOPPER: {
                    FingerprintAuthentication authentication = authenticationDetails.getAuthentication(FingerprintAuthentication.class);
                    String encodedFingerprintToken = authentication.getFingerprintToken();
                    mCard3DS2Authenticator.createFingerprint(encodedFingerprintToken, new Card3DS2Authenticator.FingerprintListener() {
                        @Override
                        public void onSuccess(@NonNull String fingerprint) {
                            FingerprintDetails fingerprintDetails = new FingerprintDetails(fingerprint);
                            getPaymentHandler().submitAuthenticationDetails(fingerprintDetails);
                        }

                        @Override
                        public void onFailure(@NonNull ThreeDS2Exception e) {
                            mCard3DS2Authenticator.release();
                            ErrorDialogFragment
                                    .newInstance(CardDetailsActivity.this, e)
                                    .showIfNotShown(getSupportFragmentManager());
                        }
                    });
                    break;
                }
                case CHALLENGE_SHOPPER: {
                    ChallengeAuthentication authentication = authenticationDetails.getAuthentication(ChallengeAuthentication.class);
                    String encodedChallengeToken = authentication.getChallengeToken();
                    mCard3DS2Authenticator.presentChallenge(encodedChallengeToken, new Card3DS2Authenticator.SimpleChallengeListener() {
                        @Override
                        public void onSuccess(@NonNull ChallengeResult challengeResult) {
                            mCard3DS2Authenticator.release();
                            ChallengeDetails challengeDetails = new ChallengeDetails(challengeResult.getPayload());
                            getPaymentHandler().submitAuthenticationDetails(challengeDetails);
                        }

                        @Override
                        public void onFailure(@NonNull ThreeDS2Exception e) {
                            mCard3DS2Authenticator.release();
                            ErrorDialogFragment
                                    .newInstance(CardDetailsActivity.this, e)
                                    .showIfNotShown(getSupportFragmentManager());
                        }
                    });
                    break;
                }
                default:
                    ErrorDialogFragment
                            .newInstance(CardDetailsActivity.this,
                                    new IllegalStateException("Unsupported result code: " + authenticationDetails.getResultCode()))
                            .showIfNotShown(getSupportFragmentManager());
                    break;
            }
        } catch (CheckoutException | ThreeDS2Exception e) {
            ErrorDialogFragment
                    .newInstance(CardDetailsActivity.this, e)
                    .showIfNotShown(getSupportFragmentManager());
        }
    }

    private boolean setupHolderNameViews(boolean initialLaunch) {
        CustomTextInputLayout holderNameLayout = findViewById(R.id.customTextInputLayout_holderName);
        mHolderNameEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateHolderNameEditText();
                updatePayButtonState();
            }
        });
        mHolderNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                validateHolderNameEditText();
            }
        });

        PaymentMethodUtil.Requirement holderNameRequirement = PaymentMethodUtil
                .getRequirementForInputDetail(CardDetails.KEY_HOLDER_NAME, mAllowedPaymentMethods);

        if (holderNameRequirement == PaymentMethodUtil.Requirement.NONE) {
            holderNameLayout.setVisibility(View.GONE);

            return false;
        } else {
            holderNameLayout.setVisibility(View.VISIBLE);

            if (initialLaunch && holderNameRequirement == PaymentMethodUtil.Requirement.REQUIRED) {
                mHolderNameEditText.requestFocus();

                return true;
            } else {
                return false;
            }
        }
    }

    private void setupCardNumberEditText(boolean requestFocus) {
        TextViewUtil.addInputFilter(
                mCardNumberEditText,
                new InputFilter.LengthFilter(CardValidator.NUMBER_MAXIMUM_LENGTH + CardValidator.NUMBER_MAXIMUM_LENGTH / CARD_NUMBER_BLOCK_LENGTH)
        );
        mCardNumberEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                validateCardNumberEditText();
                mLockToCheckmarkAnimationDelegate.onFocusChanged();
            }
        });
        mCardNumberEditText.addTextChangedListener(new SimpleTextWatcher() {
            private boolean mDeleted;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDeleted = count == 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateCardNumberEditText();
                updatePayButtonState();

                if (!mDeleted && isCardNumberValidForUser()) {
                    KeyboardUtil.showAndSelect(mExpiryDateEditText);
                }

                mLockToCheckmarkAnimationDelegate.onTextChanged();
            }
        });
        Cards.FORMATTER.attachAsYouTypeNumberFormatter(mCardNumberEditText);

        if (requestFocus) {
            mCardNumberEditText.requestFocus();
        }
    }

    private boolean isCardNumberValidForUser() {
        CardValidator.NumberValidationResult validationResult = Cards.VALIDATOR.validateNumber(mCardNumberEditText.getText().toString());
        boolean valid = validationResult.getValidity() == CardValidator.Validity.VALID;

        if (valid && mCardNumberEditText.hasFocus()) {
            String number = validationResult.getNumber();
            //noinspection ConstantConditions
            int length = number.length();

            valid = mCardNumberEditText.getSelectionEnd() == mCardNumberEditText.length() && (length == GENERAL_CARD_NUMBER_SIZE
                    || length == AMEX_NUMBER_SIZE && CardType.estimate(number).contains(CardType.AMERICAN_EXPRESS));
        }

        return valid;
    }

    private void setupCardLogoViews() {
        final RecyclerView cardLogosRecyclerView = findViewById(R.id.recyclerView_cardTypes);

        if (mAllowedPaymentMethods.size() == 1) {
            cardLogosRecyclerView.setVisibility(View.GONE);
            Application application = getApplication();
            PaymentMethod paymentMethod = mAllowedPaymentMethods.get(0);
            Callable<Drawable> logoCallable = getLogoApi().newBuilder(paymentMethod).buildCallable();
            Rembrandt.createDefaultLogoRequestArgs(application, logoCallable).into(this, new Target() {
                private int mInsetRight = getResources().getDimensionPixelSize(R.dimen.standard_half_margin);

                @Override
                public void setImageDrawable(@Nullable Drawable drawable) {
                    Drawable drawableLeft = new InsetDrawable(drawable, 0, 0, mInsetRight, 0);
                    Resources resources = getResources();
                    int width = resources.getDimensionPixelSize(R.dimen.payment_method_logo_width) + mInsetRight;
                    int height = resources.getDimensionPixelSize(R.dimen.payment_method_logo_height);
                    drawableLeft.setBounds(0, 0, width, height);
                    TextViewUtil.setCompoundDrawableLeft(mCardNumberEditText, drawableLeft);
                }
            });
        } else {
            final LogoAdapter logoAdapter = new LogoAdapter(this, cardLogosRecyclerView, getLogoApi());
            cardLogosRecyclerView.setItemAnimator(new CheckoutItemAnimator(getResources()));
            cardLogosRecyclerView.setAdapter(logoAdapter);
            int spacing = getResources().getDimensionPixelSize(R.dimen.standard_half_margin);
            cardLogosRecyclerView.addItemDecoration(new SpacingItemDecoration(spacing));

            mCardNumberEditText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    updateCardLogosRecyclerView(cardLogosRecyclerView, logoAdapter);
                }
            });
            updateCardLogosRecyclerView(cardLogosRecyclerView, logoAdapter);
        }
    }

    private void updateCardLogosRecyclerView(@NonNull RecyclerView cardLogosRecyclerView, @NonNull LogoAdapter logoAdapter) {
        List<CardType> cardTypesToDisplay = getCardTypesToDisplay();
        logoAdapter.setTxVariantProviders(cardTypesToDisplay);
        cardLogosRecyclerView.setVisibility(cardTypesToDisplay.size() > 0 ? View.VISIBLE : View.GONE);
    }

    private void setupExpiryDateEditText() {
        mExpiryDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                validateExpiryDateEditText();
            }
        });
        mExpiryDateEditText.addTextChangedListener(new SimpleTextWatcher() {
            private boolean mDeleted;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDeleted = count == 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateExpiryDateEditText();
                updatePayButtonState();

                PaymentMethodUtil.Requirement securityCodeRequirement = PaymentMethodUtil
                        .getRequirementForInputDetail(CardDetails.KEY_ENCRYPTED_SECURITY_CODE, mAllowedPaymentMethods);

                if (!mDeleted && securityCodeRequirement != PaymentMethodUtil.Requirement.NONE) {
                    if (Cards.VALIDATOR.validateExpiryDate(mExpiryDateEditText.getText().toString()).getValidity() == CardValidator.Validity.VALID) {
                        KeyboardUtil.showAndSelect(mSecurityCodeEditText);
                    }
                }
            }
        });
        Cards.FORMATTER.attachAsYouTypeExpiryDateFormatter(mExpiryDateEditText);
    }

    private void setupSecurityCodeViews() {
        CustomTextInputLayout securityCodeLayout = findViewById(R.id.customTextInputLayout_securityCode);
        mSecurityCodeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                validateSecurityCodeEditText();
            }
        });
        mSecurityCodeEditText.addTextChangedListener(new SimpleTextWatcher() {
            private boolean mDeleted;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDeleted = count == 0;
            }

            @Override
            public void afterTextChanged(Editable s) {
                validateSecurityCodeEditText();
                updatePayButtonState();

                if (mDeleted || mAllowedPaymentMethods.size() != 1) {
                    return;
                }

                CardValidator.SecurityCodeValidationResult securityCodeValidationResult = getSecurityCodeValidationResult();

                if (securityCodeValidationResult.getValidity() == CardValidator.Validity.VALID
                        && PaymentMethodUtil.getRequirementForInputDetail(CardDetails.KEY_PHONE_NUMBER, mAllowedPaymentMethods)
                        != PaymentMethodUtil.Requirement.NONE) {
                    KeyboardUtil.showAndSelect(mPhoneNumberEditText);
                }
            }
        });

        if (PaymentMethodUtil.getRequirementForInputDetail(CardDetails.KEY_ENCRYPTED_SECURITY_CODE, mAllowedPaymentMethods)
                == PaymentMethodUtil.Requirement.NONE) {
            securityCodeLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setupPhoneNumberViews() {
        CustomTextInputLayout phoneNumberLayout = findViewById(R.id.customTextInputLayout_phoneNumber);
        mPhoneNumberEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                updatePayButtonState();
            }
        });

        if (PaymentMethodUtil
                .getRequirementForInputDetail(CardDetails.KEY_PHONE_NUMBER, mAllowedPaymentMethods) == PaymentMethodUtil.Requirement.NONE) {
            phoneNumberLayout.setVisibility(View.GONE);
        }
    }

    private void setupInstallmentViews() {
        Adapter<Item> installmentsAdapter = Adapter.forSpinner(new Adapter.TextDelegate<Item>() {
            @NonNull
            @Override
            public String getText(@NonNull Item item) {
                String[] parts = item.getName().split("\\s");
                parts = replaceCurrencyCodeWithSymbol(parts);

                return parts != null ? TextUtils.join(" ", parts) : item.getName();
            }
        });
        mInstallmentsSpinner = findViewById(R.id.spinner_installments);

        Iterator<PaymentMethod> paymentMethodIterator = mAllowedPaymentMethods.iterator();
        InputDetail installmentsInputDetail = null;

        if (paymentMethodIterator.hasNext()) {
            installmentsInputDetail = InputDetailImpl.findByKey(paymentMethodIterator.next().getInputDetails(), CardDetails.KEY_INSTALLMENTS);

            if (installmentsInputDetail != null) {
                // Ensure all payment methods have the same installment options.
                while (paymentMethodIterator.hasNext()) {
                    InputDetail nextInputDetail = InputDetailImpl
                            .findByKey(paymentMethodIterator.next().getInputDetails(), CardDetails.KEY_INSTALLMENTS);

                    if (!installmentsInputDetail.equals(nextInputDetail)) {
                        installmentsInputDetail = null;
                        break;
                    }
                }
            }
        }

        if (installmentsInputDetail == null) {
            mInstallmentsContainer.setVisibility(View.GONE);
            mInstallmentsSpinner.setAdapter(null);
            installmentsAdapter.setItems(null);
        } else {
            mInstallmentsContainer.setVisibility(View.VISIBLE);
            mInstallmentsSpinner.setAdapter(installmentsAdapter);
            installmentsAdapter.setItems(installmentsInputDetail.getItems());
        }
    }

    private void setupStoreDetailsSwitchCompat() {
        List<PaymentMethod> paymentMethods = mAllowedPaymentMethods;
        boolean isStoreDetailsSupported = true;

        for (PaymentMethod paymentMethod : paymentMethods) {
            InputDetail storeDetailsInputDetail = InputDetailImpl.findByKey(paymentMethod.getInputDetails(), CardDetails.KEY_STORE_DETAILS);

            if (storeDetailsInputDetail == null) {
                isStoreDetailsSupported = false;
                break;
            }
        }

        if (isStoreDetailsSupported) {
            mStoreDetailsSwitchCompat.setVisibility(View.VISIBLE);
        } else {
            mStoreDetailsSwitchCompat.setVisibility(View.GONE);
        }
    }

    private void setupPayButton() {
        PayButtonUtil.setPayButtonText(this, mTargetPaymentMethod, mPayButton, mSurchargeTextView);
        mPayButton.setOnClickListener(this);
    }

    @NonNull
    private List<PaymentMethod> getAllowedPaymentMethods(@NonNull PaymentSession paymentSession) {
        final List<PaymentMethod> allowedPaymentMethods = new ArrayList<>();

        if (PaymentMethodTypes.CARD.equals(mTargetPaymentMethod.getType())) {
            allowedPaymentMethods.addAll(getPaymentMethodsWithGroup(paymentSession, mTargetPaymentMethod));
        } else {
            allowedPaymentMethods.add(mTargetPaymentMethod);
        }

        return allowedPaymentMethods;
    }

    @NonNull
    private List<PaymentMethod> getPaymentMethodsWithGroup(@NonNull PaymentSession paymentSession, @NonNull PaymentMethod targetGroup) {
        List<PaymentMethod> result = new ArrayList<>();

        for (PaymentMethod paymentMethod : paymentSession.getPaymentMethods()) {
            PaymentMethod currentGroup = paymentMethod.getGroup();
            String currentType = currentGroup != null ? currentGroup.getType() : null;

            if (targetGroup.getType().equals(currentType)) {
                result.add(paymentMethod);
            }
        }

        return result;
    }

    private void fillInputFieldsWithCard(@NonNull Card card) {
        mCardNumberEditText.setText(card.getNumber());
        Integer expiryMonth = card.getExpiryMonth();
        Integer expiryYear = card.getExpiryYear();

        if (expiryMonth != null && expiryYear != null) {
            mExpiryDateEditText.setText(Cards.FORMATTER.formatExpiryDate(expiryMonth, expiryYear));
        }

        mSecurityCodeEditText.setText(card.getSecurityCode());

        updatePayButtonState();

        if (Cards.VALIDATOR.validateNumber(mCardNumberEditText.getText().toString()).getValidity() != CardValidator.Validity.VALID) {
            KeyboardUtil.showAndSelect(mCardNumberEditText);
        } else if (Cards.VALIDATOR.validateExpiryDate(mExpiryDateEditText.getText().toString()).getValidity() != CardValidator.Validity.VALID) {
            KeyboardUtil.showAndSelect(mExpiryDateEditText);
        } else {
            CardValidator.SecurityCodeValidationResult securityCodeValidationResult = getSecurityCodeValidationResult();

            if (securityCodeValidationResult.getValidity() != CardValidator.Validity.VALID) {
                KeyboardUtil.showAndSelect(mSecurityCodeEditText);
            } else if (securityCodeValidationResult.getSecurityCode() != null) {
                mSecurityCodeEditText.requestFocus();
                mSecurityCodeEditText.setSelection(mSecurityCodeEditText.length());
                KeyboardUtil.hide(mSecurityCodeEditText);
            }
        }
    }

    private void validateHolderNameEditText() {
        CardValidator.HolderNameValidationResult validationResult = getHolderNameValidationResult();

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            TextViewUtil.setDefaultTextColor(mHolderNameEditText);
        } else if (mHolderNameEditText.hasFocus() && validationResult.getValidity() == CardValidator.Validity.PARTIAL) {
            TextViewUtil.setDefaultTextColor(mHolderNameEditText);
        } else {
            TextViewUtil.setErrorTextColor(mHolderNameEditText);
        }
    }

    @NonNull
    private CardValidator.HolderNameValidationResult getHolderNameValidationResult() {
        String holderName = mHolderNameEditText.getText().toString();
        PaymentMethodUtil.Requirement holderNameRequirement = PaymentMethodUtil
                .getRequirementForInputDetail(CardDetails.KEY_HOLDER_NAME, mAllowedPaymentMethods);

        return Cards.VALIDATOR.validateHolderName(holderName, holderNameRequirement == PaymentMethodUtil.Requirement.REQUIRED);
    }

    private void validateCardNumberEditText() {
        String cardNumber = mCardNumberEditText.getText().toString();
        CardValidator.NumberValidationResult validationResult = Cards.VALIDATOR.validateNumber(cardNumber);

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            TextViewUtil.setDefaultTextColor(mCardNumberEditText);
        } else if (mCardNumberEditText.hasFocus() && validationResult.getValidity() == CardValidator.Validity.PARTIAL) {
            TextViewUtil.setDefaultTextColor(mCardNumberEditText);
        } else {
            TextViewUtil.setErrorTextColor(mCardNumberEditText);
        }
    }

    private void validateExpiryDateEditText() {
        String expiryDate = mExpiryDateEditText.getText().toString();
        CardValidator.ExpiryDateValidationResult validationResult = Cards.VALIDATOR.validateExpiryDate(expiryDate);

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            TextViewUtil.setDefaultTextColor(mExpiryDateEditText);
        } else if (mExpiryDateEditText.hasFocus() && validationResult.getValidity() == CardValidator.Validity.PARTIAL) {
            TextViewUtil.setDefaultTextColor(mExpiryDateEditText);
        } else {
            TextViewUtil.setErrorTextColor(mExpiryDateEditText);
        }
    }

    private void validateSecurityCodeEditText() {
        CardValidator.SecurityCodeValidationResult validationResult = getSecurityCodeValidationResult();

        if (validationResult.getValidity() == CardValidator.Validity.VALID) {
            TextViewUtil.setDefaultTextColor(mSecurityCodeEditText);
        } else if (mSecurityCodeEditText.hasFocus() && validationResult.getValidity() == CardValidator.Validity.PARTIAL) {
            TextViewUtil.setDefaultTextColor(mSecurityCodeEditText);
        } else {
            TextViewUtil.setErrorTextColor(mSecurityCodeEditText);
        }
    }

    @NonNull
    private CardValidator.SecurityCodeValidationResult getSecurityCodeValidationResult() {
        String securityCode = mSecurityCodeEditText.getText().toString();
        boolean isRequired;
        CardType cardType;

        if (mAllowedPaymentMethods.size() == 1) {
            PaymentMethod paymentMethod = mAllowedPaymentMethods.get(0);
            isRequired = PaymentMethodUtil
                    .getRequirementForInputDetail(CardDetails.KEY_ENCRYPTED_SECURITY_CODE, paymentMethod) == PaymentMethodUtil.Requirement.REQUIRED;
            cardType = CardType.forTxVariantProvider(paymentMethod);
        } else {
            List<CardType> estimatedCardTypes = getCardTypesToDisplay();

            if (estimatedCardTypes.size() == 1 && estimatedCardTypes.get(0) == CardType.AMERICAN_EXPRESS) {
                isRequired = true;
                cardType = CardType.AMERICAN_EXPRESS;
            } else {
                isRequired = PaymentMethodUtil.getRequirementForInputDetail(CardDetails.KEY_ENCRYPTED_SECURITY_CODE, mAllowedPaymentMethods)
                        == PaymentMethodUtil.Requirement.REQUIRED;
                cardType = null;
            }
        }

        return Cards.VALIDATOR.validateSecurityCode(securityCode, isRequired, cardType);
    }

    @NonNull
    private PhoneNumberUtil.ValidationResult getPhoneNumberValidationResult() {
        PaymentMethodUtil.Requirement phoneNumberRequirement = PaymentMethodUtil
                .getRequirementForInputDetail(CardDetails.KEY_PHONE_NUMBER, mAllowedPaymentMethods);

        return PhoneNumberUtil.validate(mPhoneNumberEditText.getText().toString(), phoneNumberRequirement == PaymentMethodUtil.Requirement.REQUIRED);
    }

    @NonNull
    private List<CardType> getCardTypesToDisplay() {
        List<CardType> cardTypesToDisplay = new ArrayList<>();
        String cardNumber = Cards.VALIDATOR.validateNumber(mCardNumberEditText.getText().toString()).getNumber();

        if (cardNumber != null) {
            for (PaymentMethod allowedPaymentMethod : mAllowedPaymentMethods) {
                CardType cardType = CardType.forTxVariantProvider(allowedPaymentMethod);

                if (cardType != null && cardType.isEstimateFor(cardNumber)) {
                    cardTypesToDisplay.add(cardType);
                }
            }
        }

        return cardTypesToDisplay;
    }

    private void updatePayButtonState() {
        mPayButton.setEnabled(mConnectivityDelegate.isConnectedOrConnecting() && isValidCardDetails());
    }

    public boolean isValidCardDetails() {
        return getPaymentSession() != null
                && getHolderNameValidationResult().getValidity() == CardValidator.Validity.VALID
                && getPhoneNumberValidationResult().getValidity() == PhoneNumberUtil.Validity.VALID
                && parseCardFromInput() != null;
    }

    @Nullable
    public CardDetails buildCardDetails() throws EncryptionException {
        if (!isValidCardDetails()) {
            return null;
        }

        CardValidator.HolderNameValidationResult holderNameValidationResult = getHolderNameValidationResult();
        PhoneNumberUtil.ValidationResult phoneNumberValidationResult = getPhoneNumberValidationResult();
        Card card = parseCardFromInput();

        PaymentSession paymentSession = getPaymentSession();

        if (paymentSession == null) {
            return null;
        }

        Date generationTime = paymentSession.getGenerationTime();
        String publicKey = Objects.requireNonNull(paymentSession.getPublicKey(), CardHandler.ERROR_MESSAGE_PUBLIC_KEY_NULL);
        EncryptedCard encryptedCard;

        try {
            //noinspection ConstantConditions, validated by isValidCardDetails()
            encryptedCard = Cards.ENCRYPTOR.encryptFields(card, generationTime, publicKey).call();
        } catch (EncryptionException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt card.", e);
        }

        return new CardDetails.Builder()
                .setHolderName(holderNameValidationResult.getHolderName())
                .setEncryptedCardNumber(encryptedCard.getEncryptedNumber())
                .setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth())
                .setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear())
                .setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode())
                .setPhoneNumber(phoneNumberValidationResult.getPhoneNumber())
                .setStoreDetails(getStoreDetails())
                .setInstallments(getInstallments())
                .build();
    }

    @Nullable
    private Card parseCardFromInput() {
        String cardNumber = mCardNumberEditText.getText().toString();
        CardValidator.NumberValidationResult numberValidationResult = Cards.VALIDATOR.validateNumber(cardNumber);

        if (numberValidationResult.getValidity() != CardValidator.Validity.VALID) {
            return null;
        }

        String expiryDate = mExpiryDateEditText.getText().toString();
        CardValidator.ExpiryDateValidationResult expiryDateValidationResult = Cards.VALIDATOR.validateExpiryDate(expiryDate);

        if (expiryDateValidationResult.getValidity() != CardValidator.Validity.VALID) {
            return null;
        }

        CardValidator.SecurityCodeValidationResult securityCodeValidationResult = getSecurityCodeValidationResult();

        if (securityCodeValidationResult.getValidity() != CardValidator.Validity.VALID) {
            return null;
        }

        //noinspection ConstantConditions
        return new Card.Builder()
                .setNumber(numberValidationResult.getNumber())
                .setExpiryDate(expiryDateValidationResult.getExpiryMonth(), expiryDateValidationResult.getExpiryYear())
                .setSecurityCode(securityCodeValidationResult.getSecurityCode())
                .build();
    }

    @Nullable
    private Integer getInstallments() {
        if (mInstallmentsContainer.getVisibility() == View.VISIBLE) {
            Object selectedItem = mInstallmentsSpinner.getSelectedItem();

            if (selectedItem instanceof Item) {
                try {
                    return Integer.parseInt(((Item) selectedItem).getId());
                } catch (NumberFormatException e) {
                    // Ignore.
                }
            }
        }

        return null;
    }

    @Nullable
    private Boolean getStoreDetails() {
        if (PaymentMethodUtil
                .getRequirementForInputDetail(CardDetails.KEY_STORE_DETAILS, mAllowedPaymentMethods) == PaymentMethodUtil.Requirement.NONE) {
            return null;
        }

        return mStoreDetailsSwitchCompat.isChecked() ? true : null;
    }

    @Nullable
    private String[] replaceCurrencyCodeWithSymbol(@NonNull String[] parts) {
        String[] result = Arrays.copyOf(parts, parts.length);
        boolean replaced = false;

        for (int i = 0; i < result.length; i++) {
            try {
                Currency currency = Currency.getInstance(result[i]);

                if (currency != null) {
                    if (replaced) {
                        return null;
                    } else {
                        result[i] = currency.getSymbol();
                        replaced = true;
                    }
                }
            } catch (Exception e) {
                // Ignore.
            }
        }

        return result;
    }

    private void showCardReaderTutorialFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions();

        if (fragmentManager.findFragmentByTag(NfcCardReaderTutorialFragment.TAG) != null) {
            return;
        }

        NfcCardReaderTutorialFragment
                .newInstance()
                .show(fragmentManager, NfcCardReaderTutorialFragment.TAG);
    }

    private void hideCardReaderTutorialFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.executePendingTransactions();

        Fragment cardReaderTutorialFragment = fragmentManager.findFragmentByTag(NfcCardReaderTutorialFragment.TAG);

        if (cardReaderTutorialFragment != null) {
            fragmentManager
                    .beginTransaction()
                    .remove(cardReaderTutorialFragment)
                    .commitAllowingStateLoss();
        }
    }

    private final class NfcCardReaderListener implements NfcCardReader.Listener {
        @Override
        public void onChipDiscovered(boolean supported) {
            if (!supported) {
                Toast.makeText(getApplicationContext(), R.string.checkout_card_nfc_error_chip_unsupported, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCardDiscovered() {
            ProgressDialogFragment.show(CardDetailsActivity.this);
        }

        @Override
        public void onCardRead(@NonNull Card card) {
            ProgressDialogFragment.hide(CardDetailsActivity.this);
            hideCardReaderTutorialFragment();
            fillInputFieldsWithCard(card);
        }

        @Override
        public void onError(@NonNull NfcCardReader.Error error) {
            ProgressDialogFragment.hide(CardDetailsActivity.this);

            switch (error) {
                case CARD_UNSUPPORTED:
                    Toast.makeText(getApplicationContext(), R.string.checkout_card_nfc_error_card_unsupported, Toast.LENGTH_SHORT).show();
                    break;
                case CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), R.string.checkout_card_nfc_error_connection_lost, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    // Ignore.
            }
        }
    }
}
