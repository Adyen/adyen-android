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
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.data.CardInputData;
import com.adyen.checkout.card.data.CardOutputData;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.core.util.StringUtil;
import com.adyen.checkout.cse.Card;
import com.adyen.checkout.cse.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;
import com.adyen.checkout.cse.Encryptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CardComponent extends BasePaymentComponent<CardConfiguration, CardInputData, CardOutputData> {
    private static final String TAG = LogUtil.getTag();

    public static final PaymentComponentProvider<CardComponent, CardConfiguration> PROVIDER = new CardComponentProvider();

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.SCHEME};

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
            mFilteredSupportedCards.add(cardType);
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
    protected CardConfiguration getConfiguration() {
        return super.getConfiguration();
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
    protected PaymentComponentState<CardPaymentMethod> createComponentState() {
        Logger.v(TAG, "createComponentState");

        final CardPaymentMethod cardPaymentMethod = new CardPaymentMethod();
        cardPaymentMethod.setType(CardPaymentMethod.PAYMENT_METHOD_TYPE);

        final Card.Builder card = new Card.Builder();
        final CardOutputData outputData = getOutputData();
        final PaymentComponentData<CardPaymentMethod> paymentComponentData = new PaymentComponentData<>();

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (!outputData.isValid()) {
            return new PaymentComponentState<>(paymentComponentData, false);
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

        if (isHolderNameRequire()) {
            cardPaymentMethod.setHolderName(outputData.getHolderNameField().getValue());
        }

        paymentComponentData.setPaymentMethod(cardPaymentMethod);
        paymentComponentData.setStorePaymentMethod(outputData.isStoredPaymentMethodEnable());
        paymentComponentData.setShopperReference(getConfiguration().getShopperReference());

        return new PaymentComponentState<>(paymentComponentData, outputData.isValid());
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
    protected CardOutputData createEmptyOutputData() {
        return new CardOutputData();
    }

    @NonNull
    protected List<CardType> getSupportedFilterCards(@Nullable String cardNumber) {

        if (isStoredPaymentMethod()) {
            return mFilteredSupportedCards;
        }

        if (cardNumber == null || cardNumber.isEmpty()) {
            return Collections.emptyList();
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

    private ValidatedField<String> validateCardNumber(@NonNull String cardNumber) {
        if (isStoredPaymentMethod()) {
            return new ValidatedField<>(cardNumber, ValidatedField.Validation.VALID);
        } else {
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
            return CardValidationUtils.validateSecurityCode(securityCode, null);
        } else {
            return new ValidatedField<>(securityCode, ValidatedField.Validation.VALID);
        }
    }

    private ValidatedField<String> validateHolderName(@NonNull String holderName) {
        if (isHolderNameRequire() && !StringUtil.hasContent(holderName)) {
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
}
