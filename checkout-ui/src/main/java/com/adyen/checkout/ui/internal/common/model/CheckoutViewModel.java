package com.adyen.checkout.ui.internal.common.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.model.PaymentSession;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/04/2018.
 */
public class CheckoutViewModel extends AndroidViewModel {
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private final CheckoutMethodsLiveData mCheckoutMethodsLiveData;

    public CheckoutViewModel(@NonNull Application application) {
        super(application);

        mCheckoutMethodsLiveData = new CheckoutMethodsLiveData();
    }

    @NonNull
    public CheckoutMethodsLiveData getCheckoutMethodsLiveData() {
        return mCheckoutMethodsLiveData;
    }

    public void updateCheckoutMethodsViewModel(@NonNull PaymentSession paymentSession) {
        boolean includePreselectedMethod = !mCheckoutMethodsLiveData.isPreselectedCheckoutMethodCleared();
        final UpdateCheckoutMethodsCallable callable = new UpdateCheckoutMethodsCallable(getApplication(), paymentSession, includePreselectedMethod);
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                CheckoutMethodsModel model = callable.call();
                mCheckoutMethodsLiveData.postValue(model);
            }
        });
    }
}
