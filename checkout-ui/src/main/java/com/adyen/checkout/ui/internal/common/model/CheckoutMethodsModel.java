/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 17/04/2018.
 */

package com.adyen.checkout.ui.internal.common.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CheckoutMethodsModel {
    private final int mOneClickCheckoutMethodCount;

    private final int mCheckoutMethodCount;

    private final List<CheckoutMethod> mAllCheckoutMethods;

    private final CheckoutMethod mPreselectedCheckoutMethod;

    @Nullable
    static CheckoutMethodsModel build(
            @Nullable List<CheckoutMethod> oneClickCheckoutMethods,
            @Nullable List<CheckoutMethod> checkoutMethods,
            boolean includePreselectedCheckoutMethod
    ) {
        if (oneClickCheckoutMethods != null || checkoutMethods != null) {
            int oneClickCheckoutMethodCount = oneClickCheckoutMethods != null ? oneClickCheckoutMethods.size() : 0;
            int checkoutMethodCount = checkoutMethods != null ? checkoutMethods.size() : 0;

            List<CheckoutMethod> allCheckoutMethods = new ArrayList<>();

            if (oneClickCheckoutMethods != null) {
                allCheckoutMethods.addAll(oneClickCheckoutMethods);
            }

            if (checkoutMethods != null) {
                allCheckoutMethods.addAll(checkoutMethods);
            }

            CheckoutMethod preselectedCheckoutMethod = includePreselectedCheckoutMethod && oneClickCheckoutMethodCount > 0
                    ? oneClickCheckoutMethods.get(0)
                    : null;

            return new CheckoutMethodsModel(oneClickCheckoutMethodCount, checkoutMethodCount, allCheckoutMethods, preselectedCheckoutMethod);
        } else {
            return null;
        }
    }

    private CheckoutMethodsModel(
            int oneClickCheckoutMethodCount,
            int checkoutMethodCount,
            @NonNull List<CheckoutMethod> allCheckoutMethods,
            @Nullable CheckoutMethod preselectedCheckoutMethod
    ) {
        mOneClickCheckoutMethodCount = oneClickCheckoutMethodCount;
        mCheckoutMethodCount = checkoutMethodCount;
        mAllCheckoutMethods = allCheckoutMethods;
        mPreselectedCheckoutMethod = preselectedCheckoutMethod;
    }

    public int getOneClickCheckoutMethodCount() {
        return mOneClickCheckoutMethodCount;
    }

    public int getCheckoutMethodCount() {
        return mCheckoutMethodCount;
    }

    @Nullable
    public CheckoutMethod getPreselectedCheckoutMethod() {
        return mPreselectedCheckoutMethod;
    }

    @NonNull
    public List<CheckoutMethod> getAllCheckoutMethods() {
        return new ArrayList<>(mAllCheckoutMethods);
    }

    @NonNull
    public List<CheckoutMethod> getOneClickCheckoutMethods() {
        if (mOneClickCheckoutMethodCount == 0) {
            return Collections.emptyList();
        } else {
            //noinspection ConstantConditions
            return mAllCheckoutMethods.subList(0, mOneClickCheckoutMethodCount);
        }
    }

    @NonNull
    public List<CheckoutMethod> getCheckoutMethods() {
        if (mCheckoutMethodCount == 0) {
            return Collections.emptyList();
        } else {
            //noinspection ConstantConditions
            return mAllCheckoutMethods.subList(mOneClickCheckoutMethodCount, mAllCheckoutMethods.size());
        }
    }
}
