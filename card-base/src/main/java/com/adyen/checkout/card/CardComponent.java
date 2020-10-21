/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 13/3/2019.
 */

package com.adyen.checkout.card;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.InputDetail;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod;
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.cse.Card;
import com.adyen.checkout.cse.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;
import com.adyen.checkout.cse.Encryptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CardComponent extends BasePaymentComponent<
        CardConfiguration,
        CardInputData,
        CardOutputData,
        CardComponentState> {
    private static final String TAG = LogUtil.getTag();

    public static final PaymentComponentProvider<CardComponent, CardConfiguration> PROVIDER = new CardComponentProvider();

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.SCHEME};
    private static final int BIN_VALUE_LENGTH = 6;

    private List<CardType> mFilteredSupportedCards = Collections.emptyList();
    private CardInputData mStoredPaymentInputData;

    /**
     * Constructs a {@link CardComponent} object.
     *
     * @param paymentMethod {@link PaymentMethod} represents card payment method.
     * @param configuration {@link CardConfiguration}.
     */
    public CardComponent(@NonNull StoredPaymentMethod paymentMethod, @NonNull CardConfiguration configuration) {
        super(paymentMethod, configuration);

        mStoredPaymentInputData = new CardInputData();
        mStoredPaymentInputData.setCardNumber(paymentMethod.getLastFour());

        try {
            final ExpiryDate storedDate = new ExpiryDate(
                    Integer.parseInt(paymentMethod.getExpiryMonth()),
                    Integer.parseInt(paymentMethod.getExpiryYear())
            );
            mStoredPaymentInputData.setExpiryDate(storedDate);
        } catch (NumberFormatException e) {
            Logger.e(TAG, "Failed to parse stored Date", e);
            mStoredPaymentInputData.setExpiryDate(ExpiryDate.EMPTY_DATE);
        }

        final CardType cardType = CardType.getCardTypeByTxVariant(paymentMethod.getBrand());
        if (cardType != null) {
            final List<CardType> storedCardType = new ArrayList<>();
            storedCardType.add(cardType);
            mFilteredSupportedCards = Collections.unmodifiableList(storedCardType);
        }
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

    public boolean isStoredPaymentMethod() {
        return mStoredPaymentInputData != null;
    }

    @Nullable
    public CardInputData getStoredPaymentInputData() {
        return mStoredPaymentInputData;
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
    protected CardOutputData onInputDataChanged(@NonNull CardInputData inputData) {
        Logger.v(TAG, "onInputDataChanged");
        return new CardOutputData(
                validateCardNumber(inputData.getCardNumber()),
                validateExpiryDate(inputData.getExpiryDate()),
                validateSecurityCode(inputData.getSecurityCode()),
                validateHolderName(inputData.getHolderName()),
                inputData.isStorePaymentEnable()
        );
    }

    @NonNull
    @Override
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    @NonNull
    @Override
    protected CardComponentState createComponentState() {
        Logger.v(TAG, "createComponentState");

        final CardPaymentMethod cardPaymentMethod = new CardPaymentMethod();
        cardPaymentMethod.setType(CardPaymentMethod.PAYMENT_METHOD_TYPE);

        final Card.Builder card = new Card.Builder();
        final CardOutputData outputData = getOutputData();
        final PaymentComponentData<CardPaymentMethod> paymentComponentData = new PaymentComponentData<>();

        final String cardNumber = outputData.getCardNumberField().getValue();

        final CardType firstCardType = !mFilteredSupportedCards.isEmpty() ? mFilteredSupportedCards.get(0) : null;

        final String binValue = getBinValueFromCardNumber(cardNumber);


        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid()) {
            return new CardComponentState(paymentComponentData, false, firstCardType, binValue);
        }

        final EncryptedCard encryptedCard;
        try {
            if (!isStoredPaymentMethod()) {
                card.setNumber(outputData.getCardNumberField().getValue());
            }

            card.setSecurityCode(outputData.getSecurityCodeField().getValue());

            final ExpiryDate expiryDateResult = outputData.getExpiryDateField().getValue();

            if (expiryDateResult.getExpiryYear() != ExpiryDate.EMPTY_VALUE && expiryDateResult.getExpiryMonth() != ExpiryDate.EMPTY_VALUE) {
                card.setExpiryDate(expiryDateResult.getExpiryMonth(), expiryDateResult.getExpiryYear());
            }

            encryptedCard = Encryptor.INSTANCE.encryptFields(card.build(), getConfiguration().getPublicKey());
        } catch (EncryptionException e) {
            notifyException(e);
            return new CardComponentState(paymentComponentData, false, firstCardType, binValue);
        }

        if (!isStoredPaymentMethod()) {
            cardPaymentMethod.setEncryptedCardNumber(encryptedCard.getEncryptedNumber());
            cardPaymentMethod.setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth());
            cardPaymentMethod.setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear());
        } else {
            cardPaymentMethod.setStoredPaymentMethodId(((StoredPaymentMethod) getPaymentMethod()).getId());
        }

        cardPaymentMethod.setEncryptedSecurityCode(encryptedCard.getEncryptedSecurityCode());

        if (isHolderNameRequire()) {
            cardPaymentMethod.setHolderName(outputData.getHolderNameField().getValue());
        }

        paymentComponentData.setPaymentMethod(cardPaymentMethod);
        paymentComponentData.setStorePaymentMethod(outputData.isStoredPaymentMethodEnable());
        paymentComponentData.setShopperReference(getConfiguration().getShopperReference());

        return new CardComponentState(paymentComponentData, outputData.isValid(), firstCardType, binValue);
    }

    @NonNull
    protected List<CardType> getSupportedFilterCards() {
        return mFilteredSupportedCards;
    }

    private List<CardType> updateSupportedFilterCards(@Nullable String cardNumber) {
        Logger.d(TAG, "updateSupportedFilterCards");

        if (TextUtils.isEmpty(cardNumber)) {
            return Collections.emptyList();
        }

        final List<CardType> supportedCardTypes = getConfiguration().getSupportedCardTypes();


        final List<CardType> estimateCardTypes = CardType.estimate(cardNumber);
        final List<CardType> filteredCards = new ArrayList<>();

        for (CardType supportedCard : supportedCardTypes) {
            if (estimateCardTypes.contains(supportedCard)) {
                filteredCards.add(supportedCard);
            }
        }

        return Collections.unmodifiableList(filteredCards);
    }

    private ValidatedField<String> validateCardNumber(@NonNull String cardNumber) {
        if (isStoredPaymentMethod()) {
            return new ValidatedField<>(cardNumber, ValidatedField.Validation.VALID);
        } else {
            mFilteredSupportedCards = updateSupportedFilterCards(cardNumber);
            return CardValidationUtils.validateCardNumber(cardNumber);
        }
    }

    private ValidatedField<ExpiryDate> validateExpiryDate(@NonNull ExpiryDate expiryDate) {
        if (isStoredPaymentMethod()) {
            return new ValidatedField<>(expiryDate, ValidatedField.Validation.VALID);
        } else {
            return CardValidationUtils.validateExpiryDate(expiryDate);
        }
    }

    private ValidatedField<String> validateSecurityCode(@NonNull String securityCode) {
        final InputDetail securityCodeInputDetail = getInputDetail("cvc");
        final boolean isRequired = securityCodeInputDetail == null || !securityCodeInputDetail.isOptional();
        if (isRequired) {
            final CardType firstCardType = !mFilteredSupportedCards.isEmpty() ? mFilteredSupportedCards.get(0) : null;
            return CardValidationUtils.validateSecurityCode(securityCode, firstCardType);
        } else {
            return new ValidatedField<>(securityCode, ValidatedField.Validation.VALID);
        }
    }

    private ValidatedField<String> validateHolderName(@NonNull String holderName) {
        if (isHolderNameRequire() && TextUtils.isEmpty(holderName)) {
            return new ValidatedField<>(holderName, ValidatedField.Validation.INVALID);
        } else {
            return new ValidatedField<>(holderName, ValidatedField.Validation.VALID);
        }
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

    private String getBinValueFromCardNumber(String cardNumber) {
        return cardNumber.length() < BIN_VALUE_LENGTH ? cardNumber : cardNumber.substring(0, BIN_VALUE_LENGTH);
    }
}
