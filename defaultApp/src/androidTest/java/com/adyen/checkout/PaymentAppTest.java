package com.adyen.checkout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.NoActivityResumedException;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.adyen.core.models.Payment;
import com.adyen.core.models.paymentdetails.InputDetail;
import com.adyen.testutils.EspressoTestUtils;
import com.adyen.testutils.RetryTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Collection;
import java.util.Random;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.adyen.testutils.EspressoTestUtils.closeAllActivities;
import static com.adyen.testutils.EspressoTestUtils.waitForText;
import static com.adyen.testutils.EspressoTestUtils.waitForView;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;

/**
 * Functional tests for checking the SDK integration.
 * The main goal is to test that SDK works fine in a merchant application.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PaymentAppTest {

    private static final String CARD_NUMBER = "4444333322221111";
    private static final String CARD_EXP_DATE = "0818";
    private static final String CARD_CVC_CODE = "737";
    private static final String AMOUNT = "50";
    private static final String EUR = "EUR";

    private static final String IBAN = "NL13TEST0123456789";
    private static final String ACCOUNT_OWNER_NAME = "A. Klassen";

    @Rule
    public ActivityTestRule<MainActivity> mainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public RetryTest retry = new RetryTest(3);

    @After
    public void tearDown() throws Exception {
        closeAllActivities(getInstrumentation());
    }

    @Before
    public void setUp() throws Throwable {
        UiDevice.getInstance(getInstrumentation()).wakeUp();
        Intent intent = new Intent(getTargetContext(), MainActivity.class);
        mainActivityRule.launchActivity(intent);
        mainActivityRule.getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Test
    public void testRefusedVisaPayment() throws Exception {
        checkCardPayment("13", "EUR", "Credit Card", "4444333322221111", "12/18", "722", Payment.PaymentStatus.REFUSED.toString());
    }

    @Test
    public void testMonkeyTyping() throws Exception {
        goToCreditCardFragment();

        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText(getRandomString(10)),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_exp_date)).perform(typeText(getRandomString(10)),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText(getRandomString(10)),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);
        cancelCreditCardPayment();
    }

    @Test
    public void testCCNrInputField() throws Exception {
        goToCreditCardFragment();

        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText(CARD_EXP_DATE),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);
        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText(CARD_CVC_CODE),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);

        for (int i = 0; i < 7; i++) {
            checkInputFieldEnablesButton(R.id.adyen_credit_card_no, R.id.collectCreditCardData, getRandomString(16), false);
        }

        checkInputFieldEnablesButton(R.id.adyen_credit_card_no, R.id.collectCreditCardData, CardNumberService.getMasterCardNumbers(), true);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_no, R.id.collectCreditCardData, CardNumberService.getVISANumbers(), true);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_no, R.id.collectCreditCardData, CardNumberService.getAmexNumbers(), true);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_no, R.id.collectCreditCardData, CardNumberService.getMaestroNumbers(), true);

        checkInputFieldEnablesButton(R.id.adyen_credit_card_no, R.id.collectCreditCardData, CARD_NUMBER, true);
        cancelCreditCardPayment();
    }

    @Test
    public void testCardExpDate() throws Exception {
        goToCreditCardFragment();

        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText(CARD_NUMBER),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);
        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText(CARD_CVC_CODE),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);

        checkInputFieldEnablesButton(R.id.adyen_credit_card_exp_date, R.id.collectCreditCardData, "02/18", true);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_exp_date, R.id.collectCreditCardData, "02/12", false);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_exp_date, R.id.collectCreditCardData, "222", true);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_exp_date, R.id.collectCreditCardData, "2/22", true);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_exp_date, R.id.collectCreditCardData, "02/22", true);

        checkInputFieldEnablesButton(R.id.adyen_credit_card_exp_date, R.id.collectCreditCardData, CARD_EXP_DATE, true);
        cancelCreditCardPayment();
    }

    @Test
    public void testCardExpiryExtended() throws Exception {
        goToCreditCardFragment();

        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_no)).perform(typeText("4111111111111111"));
        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText("737"));

        int startYear = Calendar.getInstance().get(Calendar.YEAR);
        int startMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;

        int start = startYear * 12 + startMonth - 3;
        int end = 2030 * 12;

        for (int i = start; i <= end; i++) {
            int month = i % 12;
            if (month == 0) {
                month = 12;
            }
            int year = i / 12 - 2000;

            if (month > 9) {
                String text = month + "" + year;
                onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText(text));
                checkCreditCardPayButtonIsEnabled(true);
            } else {
                String text = "0" + month + year;
                onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText(text));
                checkCreditCardPayButtonIsEnabled(true);

                if (month > 1) {
                    text = month + "" + year;
                    onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText(text));
                    checkCreditCardPayButtonIsEnabled(true);
                }
            }
        }
    }

    @Test
    public void testCVCField() throws Exception {
        goToCreditCardFragment();

        checkCreditCardPayButtonIsEnabled(false);

        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText(CARD_NUMBER),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);
        onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText(CARD_EXP_DATE),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);

        checkInputFieldEnablesButton(R.id.adyen_credit_card_cvc, R.id.collectCreditCardData, "1", false);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_cvc, R.id.collectCreditCardData, "11", false);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_cvc, R.id.collectCreditCardData, "XXX", false);
        checkInputFieldEnablesButton(R.id.adyen_credit_card_cvc, R.id.collectCreditCardData, getRandomString(3), false);

        checkInputFieldEnablesButton(R.id.adyen_credit_card_cvc, R.id.collectCreditCardData, CARD_EXP_DATE, true);
        cancelCreditCardPayment();
    }

    /**
     * Test if the client side iban number validation works correctly.
     * @throws Exception
     */
    @Test
    public void testSepaUI() throws Exception {
        goToSepaFragment();
        checkButtonIsEnabled(R.id.collect_direct_debit_data, false);

        onView(withId(R.id.adyen_bank_account_holder_name)).perform(typeText("TEST OWNER"),
                closeSoftKeyboard());
        onView(withId(R.id.consent_direct_debit_checkbox)).perform(click());

        checkInputFieldEnablesButton(R.id.adyen_sepa_iban_edit_text, R.id.collect_direct_debit_data, IbanNumberService.getIbanNumbers(), true);
    }

    /**
     * Makes sure tHat visa Payment succeeds for EURO payment.
     */
    @Test
    public void testVISAPayment() throws Exception {
        payWithVISACard("17", "EUR");
    }

    /**
     * Makes sure tHat visa Payment succeeds for JPY payment.
     */
    @Test
    public void testVISAPaymentJPY() throws Exception {
        payWithVISACard("23000", "JPY");
    }

    /**
     * Makes sure tHat visa Payment succeeds when the user enters the expiry date as "818" instead of "0818".
     * Expiry date formatter should automatically add 0 at the beginning.
     */
    @Test
    public void testSuccessfulPaymentWithExpiryDateFormatter() throws Exception {
        payWithCard("23000", "IDR", "Credit Card", CARD_NUMBER, "818", CARD_CVC_CODE);
    }

    /**
     * Makes sure tHat visa Payment fails if expiry date is wrong.
     */
    @Test
    public void testPaymentFailure() throws Exception {
        checkCardPayment("23000", "IDR", "Credit Card", CARD_NUMBER, "819", CARD_CVC_CODE, Payment.PaymentStatus.REFUSED.toString());
    }

    @Test
    public void testOrientationChangeOnCreditCardScreen() throws Exception {
        goToCreditCardFragment();
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText(CARD_NUMBER),
                closeSoftKeyboard());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.adyen_credit_card_exp_date)).perform(typeText(CARD_EXP_DATE),
                closeSoftKeyboard());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText(CARD_CVC_CODE),
                closeSoftKeyboard());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.collectCreditCardData)).perform(click());
        checkResultString(Payment.PaymentStatus.AUTHORISED.toString());
    }

    /**
     * This test needs additional work. The following passes in Google Pixel device. But since
     * it uses screen coordinates; this test cannot be relied on yet. Work in progress.
     * @throws Exception
     */
    @Ignore @Test
    public void testIdealPayment() throws Exception {
        startIdealPayment();
        waitForText("Test Issuer");
        onView(withText(equalToIgnoringCase("Test Issuer"))).perform(click());
        final UiDevice device = UiDevice.getInstance(getInstrumentation());
        Thread.sleep(5000);
        device.click(65, 535);

        checkResultString(Payment.PaymentStatus.AUTHORISED.toString());
    }

    @Test
    public void testSepaDirectDebitPayment() throws Exception {
        checkSepaPayment(AMOUNT, EUR, IBAN, ACCOUNT_OWNER_NAME, Payment.PaymentStatus.AUTHORISED.toString());
    }

    @Test
    public void testOrientationChangeOnSEPADirectDebitScreen() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        onView(withText(equalToIgnoringCase("Sepa Direct Debit"))).perform(scrollTo(), click());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.adyen_sepa_iban_edit_text)).perform(clearText(), typeText(IBAN),
                closeSoftKeyboard());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.adyen_bank_account_holder_name)).perform(typeText(ACCOUNT_OWNER_NAME),
                closeSoftKeyboard());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.consent_direct_debit_checkbox)).perform(click());
        EspressoTestUtils.rotateScreen();
        onView(withId(R.id.collect_direct_debit_data)).perform(click());
        checkResultString(Payment.PaymentStatus.AUTHORISED.toString());
    }

    @Test
    public void testOrientationChangeOnPaymentMethodList() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        EspressoTestUtils.rotateScreen();
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(scrollTo(), click());
        waitForView(R.id.adyen_credit_card_no);
        Espresso.pressBack(); // closes the keyboard
        Espresso.pressBack();
        EspressoTestUtils.rotateScreen();
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(scrollTo(), click());
        waitForView(R.id.adyen_credit_card_no);
        Espresso.pressBack();
        cancelCreditCardPayment();
    }

    @Test
    public void testLinearFlow() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(scrollTo(), click());
        Espresso.pressBack(); // closes the keyboard
        Espresso.pressBack();
        onView(withText(equalToIgnoringCase("iDEAL"))).perform(scrollTo(), click());
        Espresso.pressBack();
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(scrollTo(), click());
        // TODO: move following to a method
        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText(CARD_NUMBER),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_credit_card_exp_date)).perform(typeText(CARD_EXP_DATE),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText(CARD_CVC_CODE),
                closeSoftKeyboard());
        onView(withId(R.id.collectCreditCardData)).perform(click());
        checkResultString(Payment.PaymentStatus.AUTHORISED.toString());
    }

    @Test
    public void testCardInstallments() throws Exception {
        checkCardPaymentWithInstallments(AMOUNT, EUR, "Credit Card", CARD_NUMBER, CARD_EXP_DATE, CARD_CVC_CODE, 6, 2,
                Payment.PaymentStatus.AUTHORISED.toString());
    }

    @Test
    public void testActionBarTitle() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        waitForText("Payment Methods");
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(scrollTo(), click());
        waitForText("Card Details");
        Espresso.pressBack();
        Espresso.pressBack();
        waitForText("Payment Methods");
        onView(withText(equalToIgnoringCase("SEPA Direct Debit"))).perform(scrollTo(), click());
        waitForText("Cardholder Name");
        waitForText("SEPA Direct Debit");
        Espresso.pressBack();
        waitForText("Payment Methods");
        waitForText("Payment Methods");
        onView(withText(equalToIgnoringCase("iDEAL"))).perform(scrollTo(), click());
        waitForText("iDEAL");
        Espresso.pressBack();
        waitForText("Payment Methods");
    }

    @Test
    public void testNoCVC() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR, "BE", "1");
        onView(withText(equalToIgnoringCase("Bancontact card"))).perform(scrollTo(), click());
        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText("6703444444444449"),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText("818"),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(true);
    }

    @Test
    public void testOptionalCVC() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(scrollTo(), click());
        waitForText("CVC/CVV");
        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText("6731 0123 4567 8906"),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_credit_card_exp_date)).perform(clearText(), typeText("818"),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(true);
        onView(withId(R.id.adyen_credit_card_cvc)).perform(clearText(), typeText("7"),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);
        onView(withId(R.id.adyen_credit_card_cvc)).perform(clearText(), typeText("73"),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(false);
        onView(withId(R.id.adyen_credit_card_cvc)).perform(clearText(), typeText("737"),
                closeSoftKeyboard());
        checkCreditCardPayButtonIsEnabled(true);
    }

    // TODO: Add a test for returning from redirect and selecting another method.

    private void payWithVISACard(final String amountValue, final String amountCurrency) throws Exception {
        payWithCard(amountValue, amountCurrency, "Credit Card", CARD_NUMBER, CARD_EXP_DATE, CARD_CVC_CODE);
    }

    private void payWithCard(final String amountValue, final String amountCurrency, final String cardType,
                                   final String cardNumber, final String cardExpiryDate, final String cardCVC)
            throws Exception {
        checkCardPayment(amountValue, amountCurrency, cardType, cardNumber, cardExpiryDate, cardCVC, Payment.PaymentStatus.AUTHORISED.toString());
    }

    private void checkCardPayment(final String amountValue, final String amountCurrency, final String cardType,
                             final String cardNumber, final String cardExpiryDate,
                             final String cardCVC, final String expectedResult) throws Exception {
        checkCardPaymentWithInstallments(amountValue, amountCurrency, cardType, cardNumber, cardExpiryDate, cardCVC, 1, 1, expectedResult);
    }

    private void checkCardPaymentWithInstallments(final String amountValue, final String amountCurrency, final String cardType,
                                  final String cardNumber, final String cardExpiryDate,
                                  final String cardCVC, final int maxNumberOfInstallments, final int numberOfInstallments,
                                                  final String expectedResult) throws Exception {
        if (maxNumberOfInstallments < numberOfInstallments || numberOfInstallments < 1) {
            throw new IllegalArgumentException("Requested number of installments is invalid.");
        }
        goToPaymentListFragment(amountValue, amountCurrency, String.valueOf(maxNumberOfInstallments));

        onView(withText(equalToIgnoringCase(cardType))).perform(click());
        onView(withId(R.id.adyen_credit_card_no)).perform(clearText(), typeText(cardNumber),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_credit_card_exp_date)).perform(typeText(cardExpiryDate),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_credit_card_cvc)).perform(typeText(cardCVC),
                closeSoftKeyboard());

        if (numberOfInstallments > 1) {
            onView(withId(R.id.installments_spinner)).perform(click());
            onData(allOf(is(instanceOf(InputDetail.Item.class)))).atPosition(numberOfInstallments - 1).perform(click());
        }

        onView(withId(R.id.collectCreditCardData)).perform(click());
        checkResultString(expectedResult);
    }

    private void checkSepaPayment(final String amountValue, final String amountCurrency,
                                  final String ibanNumber, final String ownername, final String expectedResult)
            throws Exception {
        goToPaymentListFragment(amountValue, amountCurrency);

        onView(withText(equalToIgnoringCase("Sepa Direct Debit"))).perform(scrollTo(), click());
        onView(withId(R.id.adyen_sepa_iban_edit_text)).perform(clearText(), typeText(ibanNumber),
                closeSoftKeyboard());
        onView(withId(R.id.adyen_bank_account_holder_name)).perform(typeText(ownername),
                closeSoftKeyboard());
        onView(withId(R.id.consent_direct_debit_checkbox)).perform(click());
        onView(withId(R.id.collect_direct_debit_data)).perform(click());
        checkResultString(expectedResult);
    }

    private void goToPaymentListFragment(final String amount, final String currency) throws Exception {
        goToPaymentListFragment(amount, currency, "1");
    }


    private void goToPaymentListFragment(final String amount, final String currency, final String maxNumberOfInstallments) throws Exception {
        goToPaymentListFragment(amount, currency, "NL", maxNumberOfInstallments);
    }

    private void goToPaymentListFragment(final String amount, final String currency, final String countryCode, final String maxNumberOfInstallments)
            throws Exception {
        EspressoTestUtils.waitForView(R.id.orderAmountEntry);
        onView(withId(R.id.orderAmountEntry)).perform(clearText(), typeText(amount),
                closeSoftKeyboard());
        onView(withId(R.id.orderCurrencyEntry)).perform(clearText(), typeText(currency),
                closeSoftKeyboard());
        if (!"NL".equalsIgnoreCase(countryCode)) {
            onView(withId(R.id.countryEntry)).perform(clearText(), typeText(countryCode),
                    closeSoftKeyboard());
        }

        short installments = Short.parseShort(maxNumberOfInstallments);
        if (installments > 1) {
            onView(withId(R.id.installmentsEntry)).perform(click());
            onData(allOf(is(instanceOf(String.class)))).atPosition(installments - 1).perform(click());
        }

        onView(withId(R.id.proceed_button)).perform(scrollTo(), click());

        EspressoTestUtils.waitForView(com.adyen.ui.R.id.activity_payment_method_selection);
    }

    private void goToCreditCardFragment() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        onView(withText(equalToIgnoringCase("Credit Card"))).perform(click());
    }

    private void goToSepaFragment() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        onView(withText(equalToIgnoringCase("Sepa Direct Debit"))).perform(scrollTo(), click());
    }

    private void startIdealPayment() throws Exception {
        goToPaymentListFragment(AMOUNT, EUR);
        onView(withText(equalToIgnoringCase("iDeal"))).perform(click());
    }

    private void checkInputFieldEnablesButton(int fieldId, int buttonId, String input, boolean valid) {
        onView(withId(fieldId)).perform(typeText(input),
                closeSoftKeyboard());
        checkButtonIsEnabled(buttonId, valid);
        onView(withId(fieldId)).perform(clearText());
    }

    private void checkInputFieldEnablesButton(int fieldId, int buttonId, Collection<String> input, boolean valid) {
        for (String str : input) {
            onView(withId(fieldId)).perform(typeText(str),
                    closeSoftKeyboard());
            checkButtonIsEnabled(buttonId, valid);
            onView(withId(fieldId)).perform(clearText());
            checkButtonIsEnabled(buttonId, false);
        }

    }

    private void checkCreditCardPayButtonIsEnabled(boolean shouldBeEnabled) {
        if (shouldBeEnabled) {
            onView(withId(R.id.collectCreditCardData)).check(matches(isEnabled()));
        } else {
            onView(withId(R.id.collectCreditCardData)).check(matches(not(isEnabled())));
        }
    }

    private void checkButtonIsEnabled(final int buttonId, boolean shouldBeEnabled) {
        if (shouldBeEnabled) {
            onView(withId(buttonId)).check(matches(isEnabled()));
        } else {
            onView(withId(buttonId)).check(matches(not(isEnabled())));
        }
    }

    private String getRandomString(int len) {
        char[] chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray();
        StringBuilder sb1 = new StringBuilder();
        Random random1 = new Random();
        for (int i = 0; i < len; i++) {
            char c1 = chars1[random1.nextInt(chars1.length)];
            sb1.append(c1);
        }
        return sb1.toString();
    }

    private void cancelCreditCardPayment() throws Exception {
        Espresso.pressBack();
        EspressoTestUtils.waitForView(R.id.activity_payment_method_selection);
        Espresso.pressBack();
        EspressoTestUtils.waitForView(R.id.verificationTextView);
        try {
            // Actually this should not be required. However without pressing back one last time; the
            // activity cannot be started in the next test. To avoid it; we kill the PaymentResultActivity as well.
            Espresso.pressBack();
        } catch (final NoActivityResumedException expected) {
            // expected
        }
    }

    private void checkResultString(final String expectedResult) throws Exception {
        EspressoTestUtils.waitForView(R.id.verificationTextView);
        onView(withId(R.id.verificationTextView)).check(matches(withText(expectedResult)));
    }

}
