package com.adyen.checkout.core.card;

import com.adyen.checkout.core.card.internal.CardValidatorImpl;

import org.junit.Test;

import java.text.ParseException;
import java.util.Calendar;

import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 30/10/2017.
 */
public class ExpiryDateUnitTest {
    private static final int START_OFFSET_IN_YEARS = 5;

    private static final int END_OFFSET_IN_YEARS = CardValidatorImpl.MAXIMUM_YEARS_IN_FUTURE + 5;

    @Test
    public void testExpiryDate() throws ParseException {
        Calendar calendar = Calendar.getInstance();
        int currentDateInMonth = calendar.get(Calendar.YEAR) * CardValidatorImpl.MONTHS_IN_YEAR + calendar.get(Calendar.MONTH);

        int startInMonth = currentDateInMonth - START_OFFSET_IN_YEARS * CardValidatorImpl.MONTHS_IN_YEAR;
        int endInMonth = currentDateInMonth + END_OFFSET_IN_YEARS * CardValidatorImpl.MONTHS_IN_YEAR;

        int expiryMonthThreshold = currentDateInMonth - CardValidatorImpl.MAXIMUM_EXPIRED_MONTHS;
        int futureMonthThreshold = currentDateInMonth + CardValidatorImpl.MAXIMUM_YEARS_IN_FUTURE * CardValidatorImpl.MONTHS_IN_YEAR;

        for (int i = startInMonth; i < endInMonth; i++) {
            int year = i / CardValidatorImpl.MONTHS_IN_YEAR;
            int month = (i % CardValidatorImpl.MONTHS_IN_YEAR) + 1;

            String expiryDateValue = String.format("%02d/%02d", month, year);

            System.out.print("Testing expiry date " + expiryDateValue);

            CardValidator.ExpiryDateValidationResult validationResult = Cards.VALIDATOR.validateExpiryDate(expiryDateValue);

            boolean isValid = validationResult.getValidity() == CardValidator.Validity.VALID;
            System.out.println(String.format(", is valid ? %s", isValid));

            assertTrue("Expiry date " + expiryDateValue + " should not be accepted for transaction.", isValid
                    ? i > expiryMonthThreshold && i <= futureMonthThreshold
                    : i <= expiryMonthThreshold || i > futureMonthThreshold
            );
        }
    }
}
