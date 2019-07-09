/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/5/2019.
 */

package com.adyen.checkout.card;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.data.output.BaseField;
import com.adyen.checkout.card.data.output.CardOutputData;
import com.adyen.checkout.card.data.output.NumberField;
import com.adyen.checkout.card.data.output.SecurityCodeField;
import com.adyen.checkout.card.data.validator.ExpiryDateValidator;
import com.adyen.checkout.card.model.EncryptedCard;
import com.adyen.checkout.cse.EncryptionException;

import java.util.Date;

import adyen.com.adyencse.encrypter.exception.EncrypterException;
import adyen.com.adyencse.pojo.Card;

public class CardEncryptionImpl implements CardEncryption {

    @Override
    @NonNull
    public EncryptedCard encryptCardOutput(@NonNull CardOutputData cardOutputData, @NonNull String publicKey,
            @NonNull Date generationTime) throws EncryptionException {
        final EncryptedCard.Builder builder = new EncryptedCard.Builder();

        try {
            builder.setEncryptedNumber(setFiledValue(cardOutputData.getNumber(), publicKey, generationTime));
            builder.setEncryptedSecurityCode(setFiledValue(cardOutputData.getSecurityCode(), publicKey, generationTime));

            final ExpiryDateValidator.ExpiryDateValidationResult expiryDateValidationResult =
                    (ExpiryDateValidator.ExpiryDateValidationResult) cardOutputData.getExpiryDate().getValidationResult();

            final String encryptedMonth = new Card.Builder().setExpiryMonth(
                    String.valueOf(expiryDateValidationResult.getExpiryMonth())).setGenerationTime(generationTime).build().serialize(publicKey);

            final String encryptedYear = new Card.Builder().setExpiryYear(
                    String.valueOf(expiryDateValidationResult.getExpiryYear())).setGenerationTime(generationTime).build().serialize(publicKey);

            builder.setEncryptedExpiryDate(encryptedMonth, encryptedYear);

            return builder.build();
        } catch (EncrypterException e) {
            throw new EncryptionException(e.getMessage(), e);
        }
    }

    private String setFiledValue(@NonNull BaseField<String> baseField, @NonNull String publicKey,
            @NonNull Date generationTime) throws EncrypterException {

        final Card.Builder cardBuilder = new Card.Builder();
        if (baseField instanceof NumberField) {
            cardBuilder.setNumber(baseField.getValue());
        } else if (baseField instanceof SecurityCodeField) {
            cardBuilder.setCvc(baseField.getValue());
        }
        cardBuilder.setGenerationTime(generationTime);

        // TODO: 2019-07-02 Create method to validate public key and put this Exception inside serialize()
        try {
            return cardBuilder.build().serialize(publicKey);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new EncrypterException("Unexpected publicKey format.", new IllegalArgumentException(e));
        }

    }

}
