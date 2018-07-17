package com.adyen.checkout.ui.internal.common.model;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/04/2018.
 */
public class CheckoutMethodsLiveData extends MutableLiveData<CheckoutMethodsModel> {
    private boolean mPreselectedCheckoutMethodCleared;

    public int getOneClickCheckoutMethodCount() {
        CheckoutMethodsModel model = getValue();

        return model != null ? model.getOneClickCheckoutMethodCount() : 0;
    }

    public int getCheckoutMethodCount() {
        CheckoutMethodsModel model = getValue();

        return model != null ? model.getCheckoutMethodCount() : 0;
    }

    @Nullable
    public CheckoutMethod getPreselectedCheckoutMethod() {
        CheckoutMethodsModel model = getValue();

        return model != null ? model.getPreselectedCheckoutMethod() : null;
    }

    public boolean isPreselectedCheckoutMethodCleared() {
        return mPreselectedCheckoutMethodCleared;
    }

    public void setPreselectedCheckoutMethodCleared() {
        CheckoutMethodsModel model = getValue();

        if (model != null) {
            mPreselectedCheckoutMethodCleared = true;
            CheckoutMethodsModel checkoutMethodsModel = CheckoutMethodsModel
                    .build(model.getOneClickCheckoutMethods(), model.getCheckoutMethods(), false);
            setValue(checkoutMethodsModel);
        }
    }

    @NonNull
    public List<CheckoutMethod> getAllCheckoutMethods() {
        CheckoutMethodsModel model = getValue();

        return model != null ? model.getAllCheckoutMethods() : Collections.<CheckoutMethod>emptyList();
    }

    @NonNull
    public List<CheckoutMethod> getOneClickCheckoutMethods() {
        CheckoutMethodsModel model = getValue();

        return model != null ? model.getOneClickCheckoutMethods() : Collections.<CheckoutMethod>emptyList();
    }

    @NonNull
    public List<CheckoutMethod> getCheckoutMethods() {
        CheckoutMethodsModel model = getValue();

        return model != null ? model.getCheckoutMethods() : Collections.<CheckoutMethod>emptyList();
    }

    public void observeOnce(@NonNull LifecycleOwner owner, @NonNull final Observer<CheckoutMethodsModel> observer) {
        super.observe(owner, new Observer<CheckoutMethodsModel>() {
            @Override
            public void onChanged(@Nullable CheckoutMethodsModel checkoutMethodsModel) {
                if (checkoutMethodsModel != null) {
                    observer.onChanged(checkoutMethodsModel);
                    removeObserver(this);
                }
            }
        });
    }
}
