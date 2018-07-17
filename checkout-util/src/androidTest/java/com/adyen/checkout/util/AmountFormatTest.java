package com.adyen.checkout.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Pair;

import com.adyen.checkout.util.internal.CheckoutCurrency;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 30/10/2017.
 */
@RunWith(AndroidJUnit4.class)
public class AmountFormatTest {
    private static final int AMOUNTS_TO_TEST_PER_CURRENCY = 4;

    @Test
    public void testAmountFormat() {
        Context context = InstrumentationRegistry.getContext();

        for (CheckoutCurrency checkoutCurrency : CheckoutCurrency.values()) {
            List<Pair<Long, String>> amounts = buildTestAmountsWithFractionDigits(checkoutCurrency);

            for (Pair<Long, String> amount : amounts) {
                CharSequence formattedAmount = AmountFormat.format(context, amount.first, amount.second);
                // Mainly ensure that formatting works without crashing.
                assertTrue("Formatted amount string is null or empty.", !TextUtils.isEmpty(formattedAmount));
            }
        }
    }

    @NonNull
    private List<Pair<Long, String>> buildTestAmountsWithFractionDigits(@NonNull CheckoutCurrency checkoutCurrency) {
        List<Pair<Long, String>> amounts = new ArrayList<>();

        String currencyCode = checkoutCurrency.name();
        int fractionDigits = checkoutCurrency.getFractionDigits();

        double scale = Math.pow(10, fractionDigits);

        double base = 0;
        double range = scale;

        for (int i = 0; i < AMOUNTS_TO_TEST_PER_CURRENCY; i++) {
            amounts.add(Pair.create((long) (base + Math.random() * range), currencyCode));
            base = range;
            range = base * scale;
        }

        return amounts;
    }
}
