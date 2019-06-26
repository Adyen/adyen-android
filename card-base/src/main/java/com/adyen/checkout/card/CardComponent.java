/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.card;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.ComponentError;
import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.api.LogoApi;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.component.PaymentComponentProviderImpl;
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.base.model.paymentmethods.InputDetail;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.card.data.formatter.CardFormatter;
import com.adyen.checkout.card.data.input.CardInputData;
import com.adyen.checkout.card.data.output.CardOutputData;
import com.adyen.checkout.card.data.output.ExpiryDateField;
import com.adyen.checkout.card.data.output.HolderNameField;
import com.adyen.checkout.card.data.output.NumberField;
import com.adyen.checkout.card.data.output.SecurityCodeField;
import com.adyen.checkout.card.data.validator.CardValidator;
import com.adyen.checkout.card.model.CardType;
import com.adyen.checkout.card.model.EncryptedCard;
import com.adyen.checkout.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import adyen.com.adyencse.encrypter.exception.EncrypterException;

public final class CardComponent extends BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData, CardPaymentMethod> implements
        CardLogoCallback.DrawableFetchedCallback {

    public static final PaymentComponentProvider<CardComponent, CardConfiguration> PROVIDER = new PaymentComponentProviderImpl<>(CardComponent.class);

    private final CardFormatter mCardFormatter;
    private final CardValidator mCardValidator;
    private final CardEncryption mCardEncryption;

    private final MutableLiveData<HashMap<String, Drawable>> mCardLogoImages = new MutableLiveData<>();
    private final List<CardType> mFilteredSupportedCards = new ArrayList<>();

    private final LogoApi mLogoApi;

    /**
     * Constructs a {@link BasePaymentComponent} object.
     *
     * @param paymentMethod {@link PaymentMethod} represents card payment method.
     * @param configuration {@link CardConfiguration}.
     */
    public CardComponent(@NonNull PaymentMethod paymentMethod, @NonNull CardConfiguration configuration) {
        super(paymentMethod, configuration);

        // TODO: 18/03/2019 add filtering by supported schemes configuration.
        // TODO: 18/03/2019 pass the card validator in. for better unit test? ask from ran!

        mCardFormatter = new CardFormatter.Builder().build();
        mCardValidator = new CardValidator.Builder().build();
        mCardEncryption = new CardEncryptionImpl();

        mLogoApi = LogoApi.getInstance(configuration.getEnvironment(), configuration.getDisplayMetrics());
    }

    protected boolean isHolderNameRequire() {
        return getConfiguration().isHolderNameRequire();
    }

    @NonNull
    @Override
    protected CardOutputData onInputDataChanged(@NonNull CardInputData inputData) {
        // TODO: 2019-05-03 Let's talk about how to handle output data with validation and formatter! @caio @arman talked about it
        onNumberChanged(inputData.getCardNumber());
        onExpiryDateChanged(inputData.getExpiryDate());
        onSecurityCodeChanged(inputData.getSecurityCode());
        onHolderNameChanged(inputData.getHolderName());

        if (isHolderNameRequire()) {
            return new CardOutputData(getOutputData().getNumber(),
                    getOutputData().getExpiryDate(),
                    getOutputData().getSecurityCode(),
                    getOutputData().getHolderNameField());
        } else {
            return new CardOutputData(getOutputData().getNumber(),
                    getOutputData().getExpiryDate(),
                    getOutputData().getSecurityCode());
        }
    }

    @NonNull
    @Override
    public String getPaymentMethodType() {
        return PaymentMethodTypes.SCHEME;
    }

    @NonNull
    @Override
    protected PaymentComponentState<CardPaymentMethod> createComponentState() {

        final CardPaymentMethod cardPaymentMethod = new CardPaymentMethod();
        cardPaymentMethod.setType(CardPaymentMethod.PAYMENT_METHOD_TYPE);

        final EncryptedCard encryptedCard;
        try {
            encryptedCard = mCardEncryption.encryptCardOutput(getOutputData(),
                    getConfiguration().getPublicKey(),
                    new Date());
        } catch (EncrypterException e) {
            notifyError(new ComponentError("An error occurred during encryption."));
            return new PaymentComponentState<>(cardPaymentMethod, false);
        }

        cardPaymentMethod.setEncryptedCardNumber(encryptedCard.getEncryptedNumber());
        cardPaymentMethod.setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth());
        cardPaymentMethod.setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear());
        cardPaymentMethod.setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode());

        if (isHolderNameRequire() && getOutputData().getHolderNameField() != null) {
            cardPaymentMethod.setHolderName(getOutputData().getHolderNameField().getValue());
        }

        return new PaymentComponentState<>(cardPaymentMethod, getOutputData().isValid());
    }

    @Override
    protected void observeOutputData(@NonNull LifecycleOwner lifecycleOwner, @NonNull Observer<CardOutputData> observer) {
        super.observeOutputData(lifecycleOwner, observer);
    }

    @NonNull
    @Override
    protected CardOutputData getOutputData() {
        return super.getOutputData();
    }

    @NonNull
    @Override
    protected CardOutputData createOutputData(@NonNull PaymentMethod paymentMethod) {
        return new CardOutputData();
    }

    protected void getCardTypeImage(@NonNull String txVariant) {
        final CardLogoCallback callback = new CardLogoCallback(txVariant, this);
        mLogoApi.getLogo(txVariant, null, null, callback);
    }

    @NonNull
    protected List<CardType> getSupportedFilterCards(@Nullable String cardNumber) {
        final List<CardType> supportedCardTypes = getConfiguration().getSupportedCardTypes();

        if (StringUtil.hasContent(cardNumber)) {
            final List<CardType> estimateCardTypes = CardType.estimate(cardNumber);
            mFilteredSupportedCards.clear();

            for (CardType supportedCard : supportedCardTypes) {
                if (estimateCardTypes.contains(supportedCard)) {
                    mFilteredSupportedCards.add(supportedCard);
                }
            }

            return mFilteredSupportedCards;
        }

        return getConfiguration().getSupportedCardTypes();
    }

    private void onNumberChanged(@Nullable String number) {
        if (number != null) {
            final String unformattedNumber = mCardFormatter.unformatNumber(number);

            final NumberField numberField = getOutputData().getNumber();
            numberField.setValue(unformattedNumber);

            final String formattedNumber = mCardFormatter.formatNumber(numberField.getValue());
            numberField.setDisplayValue(formattedNumber);

            final CardValidator.NumberValidationResult validationResult = mCardValidator.validateNumber(numberField.getValue());
            numberField.setValidationResult(validationResult);
        }
    }

    private void onExpiryDateChanged(@Nullable String expiryDate) {
        if (expiryDate != null) {
            final ExpiryDateField expiryDateField = getOutputData().getExpiryDate();

            final String formattedExpiryDate = mCardFormatter.formatExpiryDate(expiryDate, expiryDateField.getValue());

            expiryDateField.setDisplayValue(formattedExpiryDate);
            expiryDateField.setValue(expiryDate);

            final CardValidator.ExpiryDateValidationResult validationResult = mCardValidator.validateExpiryDate(expiryDate);
            expiryDateField.setValidationResult(validationResult);
        }
    }

    private void onSecurityCodeChanged(@Nullable String securityCode) {
        if (securityCode != null) {
            final SecurityCodeField securityCodeField = getOutputData().getSecurityCode();
            securityCodeField.setValue(securityCode);

            final String formattedSecurityCode = mCardFormatter.formatSecurityCode(securityCode);
            securityCodeField.setDisplayValue(formattedSecurityCode);

            final CardValidator.SecurityCodeValidationResult validationResult = getSecurityCodeValidationResult(securityCode);
            securityCodeField.setValidationResult(validationResult);
        }
    }

    private void onHolderNameChanged(@Nullable String holderName) {
        if (holderName != null) {
            final HolderNameField holderNameField = getOutputData().getHolderNameField();
            holderNameField.setValue(holderName);

            holderNameField.setDisplayValue(holderName);

            final CardValidator.HolderNameValidationResult validationResult = mCardValidator.validateHolderName(holderName, true);
            holderNameField.setValidationResult(validationResult);
        }
    }

    private CardValidator.SecurityCodeValidationResult getSecurityCodeValidationResult(@NonNull String securityCode) {
        final InputDetail securityCodeInputDetail = getInputDetail("cvc");
        final boolean isRequired = securityCodeInputDetail == null || !securityCodeInputDetail.isOptional();

        return mCardValidator.validateSecurityCode(securityCode, isRequired, null);
    }

    @Nullable
    private InputDetail getInputDetail(@NonNull String key) {
        final List<InputDetail> details = getPaymentMethod().getDetails();
        if (details != null) {
            for (InputDetail inputDetail : getPaymentMethod().getDetails()) {
                if (key.equals(inputDetail.getKey())) {
                    return inputDetail;
                }
            }
        }

        return null;
    }

    @Override
    public void onDrawableFetched(@NonNull String id, @Nullable Drawable drawable) {
        final HashMap<String, Drawable> cardLogoList = (mCardLogoImages.getValue() == null) ? new HashMap<>() : mCardLogoImages.getValue();
        cardLogoList.put(id, drawable);
        mCardLogoImages.postValue(cardLogoList);
    }

    @NonNull
    public MutableLiveData<HashMap<String, Drawable>> getCardLogoImages() {
        return mCardLogoImages;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        mLogoApi.cancellAll();
    }
}
