/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 13/10/2017.
 */

package com.adyen.example.androidtest.auto;

import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.adyen.checkout.ui.internal.card.CardDetailsActivity;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;
import com.adyen.checkout.ui.internal.sepadirectdebit.SddDetailsActivity;
import com.adyen.example.MainActivity;
import com.adyen.example.R;
import com.adyen.example.androidtest.EspressoTestUtils;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static com.adyen.example.androidtest.PaymentAppTestUtils.confirmPaymentAndWaitForResult;
import static com.adyen.example.androidtest.PaymentAppTestUtils.goToPaymentMethodsOverview;
import static com.adyen.example.androidtest.PaymentAppTestUtils.selectPaymentMethodByName;

@RunWith(AndroidJUnit4.class)
public class AutoPaymentAppTest {
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @After
    public void tearDown() throws Exception {
        EspressoTestUtils.closeAllActivities(getInstrumentation());
    }

    @Test
    public void testCardPayment() throws Exception {
        goToPaymentMethodsOverview();

        selectPaymentMethodByName("Credit Card");

        onView(withId(com.adyen.checkout.ui.R.id.editText_cardNumber)).perform(clearText(), typeText("5555444433331111"));
        onView(withId(com.adyen.checkout.ui.R.id.editText_expiryDate)).perform(clearText(), typeText("1020"));
        onView(withId(com.adyen.checkout.ui.R.id.editText_securityCode)).perform(clearText(), typeText("737"));
        KeyboardUtil.hide(EspressoTestUtils.waitForActivity(CardDetailsActivity.class).findViewById(R.id.button_pay));
        Thread.sleep(500);
        onView(withId(com.adyen.checkout.ui.R.id.button_pay)).check(ViewAssertions.matches(isClickable()));
        onView(withId(com.adyen.checkout.ui.R.id.button_pay)).perform(click());

        confirmPaymentAndWaitForResult();
    }

    @Test
    public void testSddPayment() throws Exception {
        goToPaymentMethodsOverview();

        selectPaymentMethodByName("SEPA Direct Debit");

        onView(withId(com.adyen.checkout.ui.R.id.editText_iban)).perform(clearText(), typeText("NL13TEST0123456789"));
        onView(withId(com.adyen.checkout.ui.R.id.editText_accountHolderName)).perform(clearText(), typeText("T Ester"));
        onView(withId(com.adyen.checkout.ui.R.id.switchCompat_consent)).perform(click());
        KeyboardUtil.hide(EspressoTestUtils.waitForActivity(SddDetailsActivity.class).findViewById(R.id.button_continue));
        Thread.sleep(500);
        onView(withId(com.adyen.checkout.ui.R.id.button_continue)).check(ViewAssertions.matches(isClickable()));
        onView(withId(com.adyen.checkout.ui.R.id.button_continue)).perform(click());

        confirmPaymentAndWaitForResult();
    }
}
