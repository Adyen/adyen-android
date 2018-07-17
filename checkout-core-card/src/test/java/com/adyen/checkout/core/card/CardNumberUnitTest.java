package com.adyen.checkout.core.card;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 27/10/2017.
 */
public class CardNumberUnitTest {
    @Test
    public void testTypeDetection() {
        for (Map.Entry<CardType, List<String>> entry : CardNumbers.TYPE_NUMBERS_MAPPING.entrySet()) {
            CardType cardType = entry.getKey();
            List<String> cardNumbers = entry.getValue();

            for (String cardNumber : cardNumbers) {
                System.out.println(String.format("Testing credit card type estimation for %s (%s)", cardType, cardNumber));
                List<CardType> estimatedTypes = CardType.estimate(cardNumber);
                assertTrue(
                        String.format("Expected type (%s) not estimated for number %s. Detected: %s", cardType, cardNumber, estimatedTypes),
                        estimatedTypes.contains(cardType)
                );
            }
        }
    }
}
