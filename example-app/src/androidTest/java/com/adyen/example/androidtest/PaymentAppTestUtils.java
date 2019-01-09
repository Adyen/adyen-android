/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 10/10/2018.
 */

package com.adyen.example.androidtest;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import com.adyen.checkout.ui.internal.common.activity.CheckoutActivity;
import com.adyen.checkout.wechatpay.internal.WeChatPayDetailsActivity;
import com.adyen.example.R;
import com.adyen.example.SuccessActivity;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnHolderItem;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToHolder;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.adyen.example.androidtest.EspressoTestUtils.waitForActivity;
import static com.adyen.example.androidtest.EspressoTestUtils.waitForView;

public class PaymentAppTestUtils {

    public static void changePaymentSetup(@NonNull final PaymentSetup paymentSetup) {
        goToConfigurationFragment();

        PaymentSetup.Amount amount = paymentSetup.getAmount();
        if (amount != null) {
            Long value = amount.getValue();
            if (value != null) {
                onView(withId(com.adyen.example.R.id.editText_amountValue)).perform(clearText(), typeText(String.valueOf(value)));
            }

            String currency = amount.getCurrency();
            if (!TextUtils.isEmpty(currency)) {
                onView(withId(com.adyen.example.R.id.editText_amountCurrency)).perform(clearText(), typeText(currency));
            }
        }

        String countryCode = paymentSetup.getCountryCode();
        if (!TextUtils.isEmpty(countryCode)) {
            onView(withId(com.adyen.example.R.id.editText_countryCode)).perform(clearText(), typeText(countryCode));
        }

        String shopperLocale = paymentSetup.getShopperLocale();
        if (!TextUtils.isEmpty(shopperLocale)) {
            onView(withId(com.adyen.example.R.id.editText_shopperLocale)).perform(clearText(), typeText(shopperLocale));
        }

        String merchantReference = paymentSetup.getReference();
        if (!TextUtils.isEmpty(merchantReference)) {
            onView(withId(com.adyen.example.R.id.editText_merchantReference)).perform(clearText(), typeText(merchantReference));
        }

        String shopperReference = paymentSetup.getShopperReference();
        if (!TextUtils.isEmpty(shopperReference)) {
            onView(withId(com.adyen.example.R.id.editText_shopperReference)).perform(clearText(), typeText(shopperReference));
        }

        String shopperEmail = paymentSetup.getShopperEmail();
        if (!TextUtils.isEmpty(shopperEmail)) {
            onView(withId(com.adyen.example.R.id.editText_shopperEmail)).perform(clearText(), typeText(shopperEmail));
        }

        PaymentSetup.Configuration configuration = paymentSetup.getConfiguration();
        if (configuration != null) {
            PaymentSetup.CardHolderNameRequirement cardHolderName = configuration.getCardHolderName();
            if (cardHolderName != null && !TextUtils.isEmpty(cardHolderName.name())) {
                onView(withId(com.adyen.example.R.id.editText_cardHolderName)).perform(clearText(), typeText(cardHolderName.name()));
            }

            PaymentSetup.Installments installments = configuration.getInstallments();
            if (installments != null) {
                onView(withId(com.adyen.example.R.id.editText_installments)).perform(clearText(), typeText(String.valueOf(installments.getMaxNumberOfInstallments())));
            }
        }
    }

    public static void goToPaymentMethodsOverview() throws Exception {
        onView(withId(com.adyen.example.R.id.button_checkout)).perform(click());
        waitForView(com.adyen.checkout.ui.R.id.coordinatorLayout_content);

        try {
            Thread.sleep(750);
            onView(withText(com.adyen.checkout.ui.R.string.checkout_select_other_payment_method)).perform(click());
            Thread.sleep(750);
        } catch (Exception e) {
            // Not displayed.
        }
    }

    public static void selectPaymentMethodByType(@NonNull final String paymentMethodType) throws Exception {
        Matcher<RecyclerView.ViewHolder> viewHolderMatcher = new BaseMatcher<RecyclerView.ViewHolder>() {
            @Override
            public boolean matches(Object item) {
                if (item instanceof RecyclerView.ViewHolder) {
                    String tag = (String) ((RecyclerView.ViewHolder) item).itemView.getTag();

                    return tag.equals(paymentMethodType);
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("TwoLineItemViewHolder with tag: " + paymentMethodType);
            }
        };

        selectPaymentMethod(viewHolderMatcher);
    }

    public static void selectPaymentMethodByName(@NonNull final String paymentMethodName) throws Exception {
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

        selectPaymentMethod(viewHolderMatcher);
    }

    public static void confirmPaymentAndWaitForResult() throws Exception {
        waitForActivity(SuccessActivity.class);
        Thread.sleep(500);
    }

    public static void confirmWeChatPayDetailsDisplayed() throws Exception {
        waitForActivity(WeChatPayDetailsActivity.class);
        Thread.sleep(2500);
    }

    private static void goToConfigurationFragment() {
        onView(withId(com.adyen.example.R.id.viewPager_tabs)).perform(swipeLeft());
    }

    private static void selectPaymentMethod(Matcher<RecyclerView.ViewHolder> viewHolderMatcher) throws Exception {
        CheckoutActivity checkoutActivity = waitForActivity(CheckoutActivity.class);
        checkoutActivity.mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        Thread.sleep(500);

        onView(withId(com.adyen.checkout.ui.R.id.recyclerView_checkoutMethods))
                .perform(
                        scrollToHolder(viewHolderMatcher),
                        actionOnHolderItem(viewHolderMatcher, click())
                );
    }

    private PaymentAppTestUtils() {
        throw new IllegalStateException("No instances.");
    }
}
