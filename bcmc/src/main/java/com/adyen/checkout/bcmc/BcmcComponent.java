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

import com.adyen.checkout.card.CardValidationUtils;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.data.ExpiryDate;
import com.adyen.checkout.components.GenericComponentState;
import com.adyen.checkout.components.PaymentComponentProvider;
import com.adyen.checkout.components.base.BasePaymentComponent;
import com.adyen.checkout.components.base.GenericPaymentMethodDelegate;
import com.adyen.checkout.components.base.PaymentMethodDelegate;
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.components.validation.ValidatedField;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;
import com.adyen.checkout.cse.CardEncrypter;
import com.adyen.checkout.cse.EncryptedCard;
import com.adyen.checkout.cse.exception.EncryptionException;
import com.adyen.checkout.cse.UnencryptedCard;

public final class BcmcComponent
        extends BasePaymentComponent<BcmcConfiguration, BcmcInputData, BcmcOutputData, GenericComponentState<CardPaymentMethod>> {
    private static final String TAG = LogUtil.getTag();

    private static final String[] PAYMENT_METHOD_TYPES = {PaymentMethodTypes.BCMC};

    public static final PaymentComponentProvider<BcmcComponent, BcmcConfiguration> PROVIDER = new BcmcComponentProvider();
    public static final CardType SUPPORTED_CARD_TYPE = CardType.BCMC;

    /**
     * Constructs a {@link BcmcComponent} object.
     *
     * @param paymentMethodDelegate {@link PaymentMethodDelegate} represents payment method.
     * @param configuration {@link BcmcConfiguration}.
     */
    public BcmcComponent(@NonNull GenericPaymentMethodDelegate paymentMethodDelegate, @NonNull BcmcConfiguration configuration) {
        super(paymentMethodDelegate, configuration);
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
    public String[] getSupportedPaymentMethodTypes() {
        return PAYMENT_METHOD_TYPES;
    }

    @NonNull
    @Override
    protected GenericComponentState<CardPaymentMethod> createComponentState() {
        Logger.v(TAG, "createComponentState");

        // BCMC payment method is scheme type.
        final CardPaymentMethod cardPaymentMethod = new CardPaymentMethod();
        cardPaymentMethod.setType(CardPaymentMethod.PAYMENT_METHOD_TYPE);

        final UnencryptedCard.Builder card = new UnencryptedCard.Builder();
        final BcmcOutputData outputData = getOutputData();
        final PaymentComponentData<CardPaymentMethod> paymentComponentData = new PaymentComponentData<>();

        // If data is not valid we just return empty object, encryption would fail and we don't pass unencrypted data.
        if (outputData == null || !outputData.isValid()) {
            return new GenericComponentState<>(paymentComponentData, false);
        }

        final EncryptedCard encryptedCard;
        try {
            card.setNumber(outputData.getCardNumberField().getValue());

            final ExpiryDate expiryDateResult = outputData.getExpiryDateField().getValue();

            if (expiryDateResult.getExpiryYear() != ExpiryDate.EMPTY_VALUE && expiryDateResult.getExpiryMonth() != ExpiryDate.EMPTY_VALUE) {
                card.setExpiryMonth(String.valueOf(expiryDateResult.getExpiryMonth()));
                card.setExpiryYear(String.valueOf(expiryDateResult.getExpiryYear()));
            }

            encryptedCard = CardEncrypter.encryptFields(card.build(), getConfiguration().getPublicKey());
        } catch (EncryptionException e) {
            notifyException(e);
            return new GenericComponentState<>(paymentComponentData, false);
        }

        cardPaymentMethod.setEncryptedCardNumber(encryptedCard.getEncryptedCardNumber());
        cardPaymentMethod.setEncryptedExpiryMonth(encryptedCard.getEncryptedExpiryMonth());
        cardPaymentMethod.setEncryptedExpiryYear(encryptedCard.getEncryptedExpiryYear());

        paymentComponentData.setPaymentMethod(cardPaymentMethod);

        return new GenericComponentState<>(paymentComponentData, outputData.isValid());
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
