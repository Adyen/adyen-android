package com.adyen.core.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link Amount} class.
 */
public class AmountUnitTest {
    private final static int TEN = 10;
    private final static String EUR_CURRENCY = "EUR";

    @Test
    public void testCurrency() throws Exception {
        final Amount amount = new Amount(10, EUR_CURRENCY);
        assertEquals("Currency has not been set correctly", EUR_CURRENCY, amount.getCurrency());
    }

    @Test
    public void testValue() throws Exception {
        final Amount amount = new Amount(TEN, EUR_CURRENCY);
        assertEquals("Value has not been set correctly", TEN, amount.getValue());
    }

}
