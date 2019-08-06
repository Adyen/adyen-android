/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.card;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.InputDetail;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.paymentmethods.RecurringDetail;
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.util.DateUtils;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.card.data.input.CardInputData;
import com.adyen.checkout.card.data.output.CardNumberField;
import com.adyen.checkout.card.data.output.CardOutputData;
import com.adyen.checkout.card.data.output.ExpiryDateField;
import com.adyen.checkout.card.data.output.HolderNameField;
import com.adyen.checkout.card.data.output.SecurityCodeField;
import com.adyen.checkout.card.data.validator.CardValidator;
import com.adyen.checkout.card.data.validator.ExpiryDateValidator;
import com.adyen.checkout.card.model.CardType;
import com.adyen.checkout.core.util.StringUtil;
import com.adyen.checkout.cse.Card;
import com.adyen.checkout.cse.CardEncryptor;
import com.adyen.checkout.cse.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;
import com.adyen.checkout.cse.internal.CardEncryptorImpl;

import java.util.ArrayList;
import java.util.List;

public final class CardComponent extends BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData> {

    public static final PaymentComponentProvider<CardComponent, CardConfiguration> PROVIDER = new CardComponentProvider();


    private final CardEncryptor mCardEncryption = new CardEncryptorImpl();
    private final CardValidator mCardValidator = new CardValidator.Builder().build();

    private final List<CardType> mFilteredSupportedCards = new ArrayList<>();
    private CardInputData mStoredPaymentInputData;

    /**
     * Constructs a {@link CardComponent} object.
     *
     * @param paymentMethod {@link PaymentMethod} represents card payment method.
     * @param configuration {@link CardConfiguration}.
     */
    public CardComponent(@NonNull RecurringDetail paymentMethod, @NonNull CardConfiguration configuration) {
        super(paymentMethod, configuration);

        mStoredPaymentInputData = new CardInputData();
        mStoredPaymentInputData.setCardNumber(paymentMethod.getLastFour());
        mStoredPaymentInputData.setExpiryDate(paymentMethod.getExpiryMonth()
                + DateUtils.removeFirstTwoDigitFromYear(paymentMethod.getExpiryYear()));

        final CardType cardType = CardType.getCardTypeByTxVarient(paymentMethod.getBrand());
        if (cardType != null) {
            mFilteredSupportedCards.add(cardType);
        }
    }

    public boolean isStoredPaymentMethod() {
        return mStoredPaymentInputData != null;
    }

    @Nullable
    public CardInputData getStoredPaymentInputData() {
        return mStoredPaymentInputData;
    }

    /**
     * Constructs a {@link CardComponent} object.
     *
     * @param paymentMethod {@link PaymentMethod} represents card payment method.
     * @param configuration {@link CardConfiguration}.
     */
    public CardComponent(@NonNull PaymentMethod paymentMethod, @NonNull CardConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    /**
     * Return false when {@link #isStoredPaymentMethod()} is true.
     */
    public boolean isHolderNameRequire() {
        if (isStoredPaymentMethod()) {
            return false;
        }
        return getConfiguration().isHolderNameRequire();
    }

    public boolean showStorePaymentField() {
        return getConfiguration().isShowStorePaymentFieldEnable();
    }

    @NonNull
    @Override
    protected CardConfiguration getConfiguration() {
        return super.getConfiguration();
    }

    @NonNull
    @Override
    protected CardOutputData onInputDataChanged(@NonNull CardInputData inputData) {
        return new CardOutputData(
                new CardNumberField(mCardValidator.validateNumber(inputData.getCardNumber(), isStoredPaymentMethod())),
                new ExpiryDateField(mCardValidator.validateExpiryDate(inputData.getExpiryDate())),
                new SecurityCodeField(getSecurityCodeValidationResult(inputData.getSecurityCode())),
                new HolderNameField(mCardValidator.validateHolderName(inputData.getHolderName(), isHolderNameRequire())),
                inputData.isStorePaymentEnable());
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

        final Card.Builder card = new Card.Builder();
        final CardOutputData outputData = getOutputData();

        final EncryptedCard encryptedCard;
        try {
            if (!isStoredPaymentMethod()) {
                card.setNumber(outputData.getCardNumberField().getValidationResult().getNumber());
            }

            card.setSecurityCode(outputData.getSecurityCodeField().getValidationResult().getSecurityCode());

            final ExpiryDateValidator.ExpiryDateValidationResult expiryDateResult = outputData.getExpiryDateField().getValidationResult();
            if (expiryDateResult.getExpiryYear() != null && expiryDateResult.getExpiryMonth() != null) {
                card.setExpiryDate(expiryDateResult.getExpiryMonth(), expiryDateResult.getExpiryYear());
            }

            encryptedCard = mCardEncryption.encryptFields(card.build(), getConfiguration().getPublicKey());
        } catch (EncryptionException e) {
            notifyException(e);
            final PaymentComponentData<CardPaymentMethod> paymentComponentData = new PaymentComponentData<>();
            paymentComponentData.setPaymentMethod(cardPaymentMethod);
            return new PaymentComponentState<>(paymentComponentData, false);
        }

        if (!isStoredPaymentMethod()) {
            cardPaymentMethod.setEncryptedCardNumber(encryptedCard.getEncryptedNumber());
            cardPaymentMethod.setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth());
            cardPaymentMethod.setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear());
        } else {
            cardPaymentMethod.setRecurringDetailReference(((RecurringDetail) getPaymentMethod()).getId());
        }

        cardPaymentMethod.setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode());

        if (isHolderNameRequire() && getOutputData().getHolderNameField() != null) {
            cardPaymentMethod.setHolderName(outputData.getHolderNameField().getValidationResult().getHolderName());
        }

        final PaymentComponentData<CardPaymentMethod> paymentComponentData = new PaymentComponentData<>();
        paymentComponentData.setPaymentMethod(cardPaymentMethod);
        paymentComponentData.setStorePaymentMethod(outputData.isStoredPaymentMethodEnable());
        paymentComponentData.setShopperReference(getConfiguration().getShopperReference());

        return new PaymentComponentState<>(paymentComponentData, getOutputData().isValid());
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

    @NonNull
    protected List<CardType> getSupportedFilterCards(@Nullable String cardNumber) {

        if (isStoredPaymentMethod()) {
            return mFilteredSupportedCards;
        }

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
}
