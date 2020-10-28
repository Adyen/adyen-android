/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.PaymentComponentProvider;
import com.adyen.checkout.base.PaymentComponentState;
import com.adyen.checkout.base.component.BasePaymentComponent;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.base.model.payments.request.PaymentComponentData;
import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.card.CardValidationUtils;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.cse.Card;
import com.adyen.checkout.cse.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;
import com.adyen.checkout.cse.Encryptor;

public final class BcmcComponent extends BasePaymentComponent<BcmcConfiguration, BcmcInputData, BcmcOutputData, PaymentComponentState> {
    private static final String TAG = LogUtil.getTag();

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.BCMC};

    public static final PaymentComponentProvider<BcmcComponent, BcmcConfiguration> PROVIDER = new BcmcComponentProvider();
    public static final CardType SUPPORTED_CARD_TYPE = CardType.BCMC;

    /**
     * Constructs a {@link BcmcComponent} object.
     *
     * @param paymentMethod {@link PaymentMethod} represents card payment method.
     * @param configuration {@link BcmcConfiguration}.
     */
    public BcmcComponent(@NonNull PaymentMethod paymentMethod, @NonNull BcmcConfiguration configuration) {
        super(paymentMethod, configuration);
    }

    @NonNull
    @Override
    protected BcmcOutputData onInputDataChanged(@NonNull BcmcInputData inputData) {
        Logger.v(TAG, "onInputDataChanged");
        return new BcmcOutputData(
                validateCardNumber(inputData.getCardNumber()),
                validateExpiryDate(inputData.getExpiryDate())
        );
    }

    @NonNull
    @Override
    public String getPaymentMethodType() {
        return PaymentMethodTypes.BCMC;
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

        // BCMC payment method is scheme type.
        final CardPaymentMethod cardPaymentMethod = new CardPaymentMethod();
        cardPaymentMethod.setType(CardPaymentMethod.PAYMENT_METHOD_TYPE);

        final Card.Builder card = new Card.Builder();
        final BcmcOutputData outputData = getOutputData();
        final PaymentComponentData<CardPaymentMethod> paymentComponentData = new PaymentComponentData<>();

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (outputData == null || !outputData.isValid()) {
            return new PaymentComponentState<>(paymentComponentData, false);
        }

        final EncryptedCard encryptedCard;
        try {
            card.setNumber(outputData.getCardNumberField().getValue());

            final ExpiryDate expiryDateResult = outputData.getExpiryDateField().getValue();

            if (expiryDateResult.getExpiryYear() != ExpiryDate.EMPTY_VALUE && expiryDateResult.getExpiryMonth() != ExpiryDate.EMPTY_VALUE) {
                card.setExpiryDate(expiryDateResult.getExpiryMonth(), expiryDateResult.getExpiryYear());
            }

            encryptedCard = Encryptor.INSTANCE.encryptFields(card.build(), getConfiguration().getPublicKey());
        } catch (EncryptionException e) {
            notifyException(e);
            return new PaymentComponentState<>(paymentComponentData, false);
        }

        cardPaymentMethod.setEncryptedCardNumber(encryptedCard.getEncryptedNumber());
        cardPaymentMethod.setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth());
        cardPaymentMethod.setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear());

        paymentComponentData.setPaymentMethod(cardPaymentMethod);

        return new PaymentComponentState<>(paymentComponentData, outputData.isValid());
    }

    protected boolean isCardNumberSupported(@Nullable String cardNumber) {

        if (cardNumber == null || cardNumber.isEmpty()) {
            return false;
        }

        return CardType.estimate(cardNumber).contains(SUPPORTED_CARD_TYPE);
    }

    private ValidatedField<String> validateCardNumber(@NonNull String cardNumber) {
        return CardValidationUtils.validateCardNumber(cardNumber);
    }

    private ValidatedField<ExpiryDate> validateExpiryDate(@NonNull ExpiryDate expiryDate) {
        return CardValidationUtils.validateExpiryDate(expiryDate);
    }
}
