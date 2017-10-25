package com.adyen.customuiapplication;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;

import com.adyen.core.models.Payment;
import com.adyen.core.models.PaymentMethod;
import com.adyen.testutils.EspressoTestUtils;
import com.adyen.testutils.RetryTest;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.adyen.testutils.EspressoTestUtils.closeAllActivities;
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

    @Rule
    public ActivityTestRule<MainActivity> mMainActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public RetryTest retry = new RetryTest(5);

    @Before
    public void setUp() throws Throwable {
        UiDevice.getInstance(getInstrumentation()).wakeUp();
    }

    @After
    public void tearDown() throws Exception {
        closeAllActivities(getInstrumentation());
    }

    /**
     * Makes sure tHat visa Payment succeeds for EURO payment.
     */
    @Test
    public void testVISAPayment() throws Exception {
        payWithVISACard(AMOUNT, EUR);
    }

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
        goToPaymentListFragment(amountValue, amountCurrency);

        onData(paymentMethodWithName(cardType))
                .inAdapterView(withId(android.R.id.list))
                .perform(click());
        onView(withId(R.id.credit_card_no)).perform(clearText(), typeText(cardNumber),
                closeSoftKeyboard());
        onView(withId(R.id.credit_card_exp_date)).perform(typeText(cardExpiryDate),
                closeSoftKeyboard());
        onView(withId(R.id.credit_card_cvc)).perform(typeText(cardCVC),
                closeSoftKeyboard());
        onView(withId(R.id.collectCreditCardData)).perform(click());
        EspressoTestUtils.waitForView(R.id.verificationTextView);
        onView(withId(R.id.verificationTextView)).check(matches(withText(expectedResult)));
    }

    private void goToPaymentListFragment(final String amount, final String currency) throws Exception {
        EspressoTestUtils.waitForView(R.id.orderAmountEntry);
        onView(withId(R.id.orderAmountEntry)).perform(clearText(), typeText(amount),
                closeSoftKeyboard());
        onView(withId(R.id.orderCurrencyEntry)).perform(clearText(), typeText(currency),
                closeSoftKeyboard());
        onView(withId(R.id.proceed_button)).perform(scrollTo(), click());

        EspressoTestUtils.waitForView(R.id.activity_payment_method_selection);
    }

    public static Matcher<Object> paymentMethodWithName(final String name) {
        final Matcher matcher = equalToIgnoringCase(name);

        return new BoundedMatcher<Object, PaymentMethod>(PaymentMethod.class) {
            @Override
            public void describeTo(org.hamcrest.Description description) {
                matcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(PaymentMethod paymentMethod) {
                return matcher.matches(paymentMethod.getName());
            }
        };
    }
}
