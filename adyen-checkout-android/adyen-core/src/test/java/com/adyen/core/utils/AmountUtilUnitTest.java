package com.adyen.core.utils;

import com.adyen.core.models.Amount;

import org.junit.Test;

import java.text.ParseException;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Unit tests for {@link AmountUtil} class.
 */
public class AmountUtilUnitTest {

    @Test
    public void testFormatAmountWithZeroExponent() throws Exception {
        final Amount amount = new Amount(100, "IDR");
        assertEquals("IDR 100", AmountUtil.format(amount, true));
    }

    @Test
    public void testFormatAmountWithOneExponent() throws Exception {
        final Amount amount = new Amount(100, "MRO");
        assertEquals("MRO 10.0", AmountUtil.format(amount, true));
    }

    @Test
    public void testFormatAmountWithTwoExponent() throws Exception {
        final Amount amount = new Amount(100, "EUR");
        assertEquals("EUR 1.00", AmountUtil.format(amount, true));
    }

    @Test
    public void testAmountUtilConstructor() throws Exception {
        final Amount amount = new Amount(1000, "JPY");
        assertEquals("JPY 1000", AmountUtil.format(amount, true));
    }

    @Test
    public void testAmountUtilCurrencyValidity() throws Exception {
        assertTrue(AmountUtil.isValidCurrencyCode("USD"));
        assertFalse(AmountUtil.isValidCurrencyCode("UUU"));
    }

    @Test
    public void testAmountUtilFormatter() throws Exception {
        final Amount amount = new Amount(20000, "USD");
        assertEquals("$200,00", AmountUtil.format(amount, true, Locale.FRENCH));
    }

    @Test
    public void testParsingMajorUnits() throws Exception {
        assertEquals(100, AmountUtil.parseMajorAmount("EUR", "1.00"));
        assertEquals(100, AmountUtil.parseMajorAmount("EUR", "1,00"));
        assertEquals(1000005, AmountUtil.parseMajorAmount("EUR", "10,000.05"));
        assertEquals(1000005, AmountUtil.parseMajorAmount("EUR", "10.000,05"));
        assertEquals(1, AmountUtil.parseMajorAmount("IDR", "1,00"));
        assertEquals(1, AmountUtil.parseMajorAmount("JPY", "1,00"));
    }

    @Test
    public void testCurrencySymbolFormatting() throws Exception {
        final long amountValue = AmountUtil.parseMajorAmount("USD", "100");
        final Amount amount = new Amount(amountValue, "USD");
        assertEquals("$100.00", AmountUtil.toString(amount));
    }

    @Test
    public void testCurrencyCodeFormatting() throws Exception {
        final long amountValue = AmountUtil.parseMajorAmount("IDR", "100");
        final Amount amount = new Amount(amountValue, "IDR");
        assertEquals("IDR 100", AmountUtil.toString(amount));
    }

    @Test
    public void testAmountUtilLocaleTest() throws Exception {
        final Amount amount = new Amount(20000, "USD");
        assertEquals("$200,00", AmountUtil.format(amount, true, Locale.FRENCH));
        assertEquals("$200.00", AmountUtil.format(amount, true, Locale.ENGLISH));
    }

    @Test
    public void testAmountParsingWithoutCurrencyCode() throws Exception {
        final Amount amount = new Amount(20000, "USD");
        assertEquals("200.00", AmountUtil.format(amount, false, null));
    }

    @Test
    public void testAmountParsingWithCustomExponent() throws Exception {
        assertEquals("200", AmountUtil.format(200, 0, Locale.ENGLISH));
    }

    @Test
    public void testEmptyValueString() throws Exception {
        try {
            AmountUtil.parseMajorAmount("EUR", "");
            fail("Empty value is not supposed to be parsed correctly.");
        } catch (final ParseException expected) {
            // expected
        }
    }

    @Test
    public void testNegativeValueString() throws Exception {
        assertEquals(-100, AmountUtil.parseMajorAmount("EUR", "-1.00"));
        assertEquals(-100, AmountUtil.parseMajorAmount("EUR", "1.00-"));

    }

    @Test
    public void testWhitespaceCharsInValueString() throws Exception {
        assertEquals(-10000, AmountUtil.parseMajorAmount("EUR", "  -10 0.0 0  "));
    }

    @Test
    public void testInvalidCurrencyString() throws Exception {
        assertEquals(100, AmountUtil.parseMajorAmount("ADY", "100"));
    }

    @Test
    public void testInvalidMinorUnits() throws Exception {
        try {
            AmountUtil.parseMajorAmount("EUR", "100.5");
            fail("Invalid number of minor digits should have failed.");
        } catch (final ParseException expected) {
            // expected
        }
    }

}
