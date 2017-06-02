package com.adyen.ui.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertTrue;

/**
 * Card resolution.
 */

public class CardTypeUnitTest {
    private static final String DE_BIJENKORF_TEST_CARD = "5100081112223332";
    private static final String AMEX_TEST_CARD = "374251018720018";
    private static final String MASTERCARD_TEST_CARD = "5100290029002909";
    private static final String VISA_TEST_CARD = "4111111111111111";
    private static final String JCB_TEST_CARD = "3569990010095841";
    private static final String DINERS_TEST_CARD = "36006666333344";
    private static final String DISCOVER_TEST_CARD_GB = "6445644564456445";
    private static final String DISCOVER_TEST_CARD_US = "6011601160116611";
    private static final String MAESTRO_TEST_CARD = "6731012345678906";
    private static final String HIPERCARD_TEST_CARD = "6062828888666688";
    private static final String BRAZILIAN_TEST_CARD = "5066991111111118";
    private static final String DANKORT_TEST_CARD = "5019555544445555";
    private static final String CUP_TEST_CARD = "6222988812340000";

    @Test
    public void testBijenkorfCard() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "bijcard", "visa");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(2, CardType.mc);
        expectedCardTypePattern.put(7, CardType.bijcard);
        verifyCardTypeResolution(DE_BIJENKORF_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testAmex() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(2, CardType.amex);
        verifyCardTypeResolution(AMEX_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testVisa() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(1, CardType.visa);
        verifyCardTypeResolution(VISA_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testMasterCard() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(2, CardType.mc);
        verifyCardTypeResolution(MASTERCARD_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testMasterCardWhenItIsNotSupported() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("visa", "amex");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        verifyCardTypeResolution(MASTERCARD_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testJCB() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(3, CardType.jcb);
        verifyCardTypeResolution(JCB_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testDiners() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(2, CardType.diners);
        verifyCardTypeResolution(DINERS_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testDiscoverGB() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(3, CardType.discover);
        verifyCardTypeResolution(DISCOVER_TEST_CARD_GB, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testDiscoverUS() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(4, CardType.discover);
        verifyCardTypeResolution(DISCOVER_TEST_CARD_US, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testMaestro() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover", "maestro");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(1, CardType.maestro);
        verifyCardTypeResolution(MAESTRO_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testHipercard() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover", "maestro", "hipercard");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(1, CardType.maestro);
        expectedCardTypePattern.put(6, CardType.hipercard);
        verifyCardTypeResolution(HIPERCARD_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testBrazilianCard() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover", "maestro", "hipercard", "elo");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(6, CardType.elo);
        verifyCardTypeResolution(BRAZILIAN_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testDankort() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover", "maestro", "elo", "dankort");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(4, CardType.dankort);
        verifyCardTypeResolution(DANKORT_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    @Test
    public void testCup() throws Exception {
        final List<String> availablePaymentMethods = createPaymentMethodTypes("mc", "visa", "amex", "jcb", "diners",
                "discover", "maestro", "elo", "dankort", "cup");
        final Map<Integer, CardType> expectedCardTypePattern = new HashMap<>();
        expectedCardTypePattern.put(1, CardType.maestro);
        expectedCardTypePattern.put(2, CardType.cup);
        verifyCardTypeResolution(CUP_TEST_CARD, availablePaymentMethods, expectedCardTypePattern);
    }

    private List<String> createPaymentMethodTypes(final String... types) {
        final List<String> availablePaymentMethodTypes = new ArrayList<>();
        for (final String type : types) {
            availablePaymentMethodTypes.add(type);
        }
        return availablePaymentMethodTypes;
    }

    /**
     * Verifies that card type resolving pattern matches the expectation.
     * @param cardNumber Card number which will be used for verifying card type detection.
     * @param availablePaymentMethodTypes Allowed card types.
     * @param expectedTypePattern A hashmap that defines at which digit, the credit card number will be resolved
     *                            to which type.
     */
    private void verifyCardTypeResolution(final String cardNumber, final List<String> availablePaymentMethodTypes,
                                          final Map<Integer, CardType> expectedTypePattern) {
        final char[] numberArray = cardNumber.toCharArray();
        String currentNumber = "";
        final Map<Integer, CardType> resolvedTypePattern = new HashMap<>();
        CardType currentResolvedType = CardType.UNKNOWN;
        int currentDigit = 0;
        for (final char number : numberArray) {
            currentDigit++;
            currentNumber += number;
            final CardType resolvedType = CardType.detect(currentNumber, availablePaymentMethodTypes);
            if (resolvedType != currentResolvedType) {
                resolvedTypePattern.put(currentDigit, resolvedType);
                currentResolvedType = resolvedType;
            }
        }
        assertTrue("Expected pattern: " + expectedTypePattern.toString() + ", received pattern: "
                + resolvedTypePattern, expectedTypePattern.equals(resolvedTypePattern));
    }
}
