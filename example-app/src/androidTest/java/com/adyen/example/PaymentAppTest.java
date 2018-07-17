package com.adyen.example;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.adyen.checkout.ui.internal.sepadirectdebit.SddDetailsActivity;
import com.adyen.checkout.ui.internal.common.activity.CheckoutActivity;
import com.adyen.checkout.ui.internal.common.util.KeyboardUtil;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.adyen.example.EspressoTestUtils.waitForActivity;
import static com.adyen.example.EspressoTestUtils.waitForView;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 13/10/2017.
 */
@RunWith(AndroidJUnit4.class)
public class PaymentAppTest {
    @Rule
    public ActivityTestRule<MainActivity> mMainActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @After
    public void tearDown() throws Exception {
        EspressoTestUtils.closeAllActivities(getInstrumentation());
    }

    @Test
    public void testCardPayment() throws Exception {
        goToPaymentMethodsOverview();

        selectPaymentMethod("Credit Card");

        onView(withId(com.adyen.checkout.ui.R.id.editText_cardNumber)).perform(clearText(), typeText("5555444433331111"));
        onView(withId(com.adyen.checkout.ui.R.id.editText_expiryDate)).perform(clearText(), typeText("1020"));
        onView(withId(com.adyen.checkout.ui.R.id.editText_securityCode)).perform(clearText(), typeText("737"));
        onView(withId(com.adyen.checkout.ui.R.id.button_pay)).perform(click());

        confirmPaymentAndWaitForResult();
    }

    @Test
    public void testSddPayment() throws Exception {
        goToPaymentMethodsOverview();

        selectPaymentMethod("SEPA Direct Debit");

        onView(withId(com.adyen.checkout.ui.R.id.editText_iban)).perform(clearText(), typeText("NL13TEST0123456789"));
        onView(withId(com.adyen.checkout.ui.R.id.editText_accountHolderName)).perform(clearText(), typeText("T Ester"));
        onView(withId(com.adyen.checkout.ui.R.id.switchCompat_consent)).perform(click());
        KeyboardUtil.hide(EspressoTestUtils.waitForActivity(SddDetailsActivity.class).findViewById(R.id.button_continue));
        Thread.sleep(500);
        onView(withId(com.adyen.checkout.ui.R.id.button_continue)).perform(click());

        confirmPaymentAndWaitForResult();
    }

    private void goToPaymentMethodsOverview() throws Exception {
        onView(withId(R.id.button_checkout)).perform(click());
        waitForView(com.adyen.checkout.ui.R.id.coordinatorLayout_content);

        try {
            Thread.sleep(750);
            onView(withText(com.adyen.checkout.ui.R.string.checkout_select_other_payment_method)).perform(click());
            Thread.sleep(750);
        } catch (Exception e) {
            // Not displayed.
        }
    }

    private void selectPaymentMethod(@NonNull final String paymentMethodName) throws Exception {
        Matcher<RecyclerView.ViewHolder> viewHolderMatcher = new BaseMatcher<RecyclerView.ViewHolder>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof RecyclerView.ViewHolder) {
                    TextView primaryTextView = ((RecyclerView.ViewHolder) item).itemView.findViewById(R.id.textView_primary);
                    String primaryText = primaryTextView.getText().toString();

                    return primaryText.equals(paymentMethodName);
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("TwoLineItemViewHolder with primary text: " + paymentMethodName);
            }
        };

        CheckoutActivity checkoutActivity = waitForActivity(CheckoutActivity.class);
        checkoutActivity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        Thread.sleep(500);

        onView(withId(com.adyen.checkout.ui.R.id.recyclerView_checkoutMethods))
                .perform(
                        scrollToHolder(viewHolderMatcher),
                        actionOnHolderItem(viewHolderMatcher, click())
                );
    }

    private void confirmPaymentAndWaitForResult() throws Exception {
        waitForActivity(SuccessActivity.class);
        Thread.sleep(500);
    }
}
