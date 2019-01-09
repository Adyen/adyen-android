/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.checkout.core.internal;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.NetworkingState;
import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.SearchHandler;
import com.adyen.checkout.core.internal.model.AddressAndNameResponse;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.core.model.KlarnaSsnLookupResponse;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SsnLookupSearchHandler implements SearchHandler<KlarnaSsnLookupResponse> {

    private static final int SSN_NUMBER_OF_DIGITS = 10;

    private final Application mApplication;
    private final PaymentMethod mPaymentMethod;
    private final PaymentSession mPaymentSession;
    private final String mCallUrl;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private final NetworkingStateImpl mNetworkingState = new NetworkingStateImpl();
    private final ObservableImpl<NetworkingState> mNetworkingStateObservable = new ObservableImpl<NetworkingState>(mNetworkingState);
    private final ObservableImpl<CheckoutException> mErrorObservable = new ObservableImpl<>(null);
    private final ObservableImpl<KlarnaSsnLookupResponse> mSearchResultsObservable = new ObservableImpl<>(null);

    private String mCachedSsnSearch;
    private KlarnaSsnLookupResponse mCachedSsnResponse;

    public SsnLookupSearchHandler(@NonNull Application application, @NonNull PaymentMethod paymentMethod,
                                  @NonNull PaymentSession paymentSession, @NonNull String callUrl) {
        mApplication = application;
        mPaymentMethod = paymentMethod;
        mPaymentSession = paymentSession;
        mCallUrl = callUrl;
    }


    @Override
    public void setSearchString(@NonNull String value) {
        if (value.equals(mCachedSsnSearch)) {
            mSearchResultsObservable.setValue(mCachedSsnResponse);
            return;
        }

        if (isSsnValueValid(value)) {
            executeSearch(value);
        } else {
            mErrorObservable.setValue(new CheckoutException.Builder("SSN format is not valid.", null).build());
        }
    }

    private boolean isSsnValueValid(String value) {
        final String emptySpace = " ";

        //remove empty spaces
        final String shortenedValue = value.replace(emptySpace, "");

        if (value.length() != SSN_NUMBER_OF_DIGITS) {
            return false;
        }

        try {
            Long.parseLong(shortenedValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @NonNull
    @Override
    public Observable<NetworkingState> getNetworkInfoObservable() {
        return mNetworkingStateObservable;
    }

    @NonNull
    @Override
    public Observable<KlarnaSsnLookupResponse> getSearchResultsObservable() {
        return mSearchResultsObservable;
    }

    @NonNull
    @Override
    public Observable<CheckoutException> getErrorObservable() {
        return mErrorObservable;
    }

    private void executeSearch(@NonNull final String ssn) {

        final Callable<AddressAndNameResponse> callable = CheckoutApi
                .getInstance(mApplication)
                .getSsnLookup(mPaymentMethod, mPaymentSession, mCallUrl, ssn);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mNetworkingState.onRequestStarted();
                            mNetworkingStateObservable.setValue(mNetworkingState);
                        }
                    });

                    final AddressAndNameResponse response = callable.call();

                    if (response != null && response.getAddressAndNameWrappers().size() > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCachedSsnResponse = response.getAddressAndNameWrappers().get(0).getAddressAndName();
                                mCachedSsnSearch = ssn;
                                mSearchResultsObservable.setValue(mCachedSsnResponse);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mErrorObservable.setValue(new CheckoutException.Builder("SsnLookup returned with an empty or invalid result.", null)
                                        .setFatal(false)
                                        .build()
                                );
                            }
                        });
                    }

                } catch (Exception e) {
                    final CheckoutException checkoutException = e instanceof CheckoutException
                            ? (CheckoutException) e
                            : new CheckoutException.Builder("An error occured while doing SSN Lookup.", e).build();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mErrorObservable.setValue(checkoutException);
                        }
                    });
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mNetworkingState.onRequestFinished();
                            mNetworkingStateObservable.setValue(mNetworkingState);
                        }
                    });
                }
            }
        });

    }

    private void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mMainHandler.post(runnable);
        }
    }
}
