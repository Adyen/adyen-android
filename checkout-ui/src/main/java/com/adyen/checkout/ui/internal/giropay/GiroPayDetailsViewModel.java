package com.adyen.checkout.ui.internal.giropay;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.internal.CheckoutApi;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.internal.model.GiroPayIssuersResponse;
import com.adyen.checkout.ui.internal.common.model.Operation;

import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 03/04/2018.
 */
class GiroPayDetailsViewModel extends AndroidViewModel {
    private static final int MINIMUM_SEARCH_STRING_LENGTH = 4;

    private final ExecutorService mBackgroundExecutorService = Executors.newSingleThreadExecutor();

    private final PaymentMethod mPaymentMethod;

    private final MutableLiveData<String> mSearchStringLiveData;

    private final MutableLiveData<Operation<String, GiroPayIssuersResponse>> mQueryIssuersOperationLiveData;

    GiroPayDetailsViewModel(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        super(application);

        mPaymentMethod = paymentMethod;
        mSearchStringLiveData = new MutableLiveData<>();
        mQueryIssuersOperationLiveData = new MutableLiveData<>();

        mSearchStringLiveData.observeForever(new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                String trimmedSearchString = s != null ? s.trim() : null;

                if (trimmedSearchString != null) {
                    checkShouldExecuteSearch(trimmedSearchString);
                }
            }
        });
    }

    @NonNull
    public MutableLiveData<String> getSearchStringLiveData() {
        return mSearchStringLiveData;
    }

    @NonNull
    public LiveData<Operation<String, GiroPayIssuersResponse>> getQueryIssuersOperationLiveData() {
        return mQueryIssuersOperationLiveData;
    }

    private void checkShouldExecuteSearch(@NonNull final String trimmedSearchString) {
        int searchStringLength = trimmedSearchString.length();

        if (searchStringLength < MINIMUM_SEARCH_STRING_LENGTH) {
            return;
        }

        final Operation<String, GiroPayIssuersResponse> operation = mQueryIssuersOperationLiveData.getValue();

        if (operation == null) {
            executeSearch(trimmedSearchString);
        } else {
            operation.dispatchCurrentState(new Operation.Listener<String, GiroPayIssuersResponse>() {
                @Override
                public void onRunning(@NonNull String input) {
                    // We are already searching, wait for operation to complete.
                }

                @Override
                public void onComplete(@NonNull String input, @NonNull GiroPayIssuersResponse output) {
                    String previousSearchString = operation.getInput();

                    if (newSearchStringRequiresSearch(previousSearchString, trimmedSearchString)) {
                        executeSearch(trimmedSearchString);
                    }
                }

                @Override
                public void onError(@NonNull String input, @Nullable GiroPayIssuersResponse output, @NonNull Throwable error) {
                    executeSearch(trimmedSearchString);
                }
            });
        }
    }

    private boolean newSearchStringRequiresSearch(@NonNull String previousSearchString, @NonNull String newSearchString) {
        int previousLength = previousSearchString.length();
        int newLength = newSearchString.length();

        return newLength < previousLength
                || (newLength == previousLength && !newSearchString.equalsIgnoreCase(previousSearchString))
                || !newSearchString.toLowerCase(Locale.US).startsWith(previousSearchString.toLowerCase(Locale.US));
    }

    private void executeSearch(@NonNull final String trimmedSearchString) {
        mQueryIssuersOperationLiveData.setValue(Operation.<String, GiroPayIssuersResponse>running(trimmedSearchString));
        final Callable<GiroPayIssuersResponse> callable = CheckoutApi.getInstance(getApplication())
                .getGiroPayIssuers(mPaymentMethod, trimmedSearchString);
        mBackgroundExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    GiroPayIssuersResponse response = callable.call();
                    mQueryIssuersOperationLiveData.postValue(Operation.complete(trimmedSearchString, response));
                } catch (Exception e) {
                    mQueryIssuersOperationLiveData.postValue(Operation.<String, GiroPayIssuersResponse>error(trimmedSearchString, null, e));
                }
            }
        });
    }

    static final class Factory implements ViewModelProvider.Factory {
        private final Application mApplication;

        private final PaymentMethod mPaymentMethod;

        Factory(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
            mApplication = application;
            mPaymentMethod = paymentMethod;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new GiroPayDetailsViewModel(mApplication, mPaymentMethod);
        }
    }
}
