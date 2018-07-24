package com.adyen.checkout.core.internal;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;
import com.adyen.checkout.core.NetworkingState;
import com.adyen.checkout.core.Observable;
import com.adyen.checkout.core.SearchHandler;
import com.adyen.checkout.core.internal.model.GiroPayIssuerImpl;
import com.adyen.checkout.core.internal.model.GiroPayIssuersResponse;
import com.adyen.checkout.core.model.GiroPayIssuer;
import com.adyen.checkout.core.model.PaymentMethod;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 19/07/2018.
 */
public class GiroPayIssuerSearchHandler implements SearchHandler<List<GiroPayIssuer>> {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static final int MINIMUM_SEARCH_STRING_LENGTH = 4;

    private final ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

    private final NetworkingStateImpl mNetworkingState = new NetworkingStateImpl();

    private final ObservableImpl<NetworkingState> mNetworkingStateObservable = new ObservableImpl<NetworkingState>(mNetworkingState);

    private final ObservableImpl<CheckoutException> mErrorObservable = new ObservableImpl<>(null);

    private final ObservableImpl<List<GiroPayIssuer>> mSearchResultsObservable = new ObservableImpl<>(null);

    private final Application mApplication;

    private final PaymentMethod mPaymentMethod;

    private Map.Entry<String, GiroPayIssuersResponse> mCurrentSearch;

    private List<GiroPayIssuer> mCurrentSearchResults;

    public GiroPayIssuerSearchHandler(@NonNull Application application, @NonNull PaymentMethod paymentMethod) {
        mApplication = application;
        mPaymentMethod = paymentMethod;
    }

    @Override
    public void setSearchString(@NonNull String value) {
        String trimmedSearchString = value.trim();

        if (!checkShouldExecuteSearch(trimmedSearchString)) {
            performFiltering(trimmedSearchString);
        }
    }

    @Override
    @NonNull
    public Observable<NetworkingState> getNetworkInfoObservable() {
        return mNetworkingStateObservable;
    }

    @Override
    @NonNull
    public Observable<List<GiroPayIssuer>> getSearchResultsObservable() {
        return mSearchResultsObservable;
    }

    @Override
    @NonNull
    public Observable<CheckoutException> getErrorObservable() {
        return mErrorObservable;
    }

    private boolean checkShouldExecuteSearch(@NonNull String trimmedSearchString) {
        int searchStringLength = trimmedSearchString.length();

        if (searchStringLength < MINIMUM_SEARCH_STRING_LENGTH) {
            return false;
        }

        if (mNetworkingState.isExecutingRequests()) {
            return false;
        }

        if (mCurrentSearch == null || newSearchStringRequiresSearch(mCurrentSearch.getKey(), trimmedSearchString)) {
            mCurrentSearch = new AbstractMap.SimpleEntry<>(trimmedSearchString, null);
            executeSearch(trimmedSearchString);

            return true;
        }

        return false;
    }

    private boolean newSearchStringRequiresSearch(@NonNull String previousSearchString, @NonNull String newSearchString) {
        int previousLength = previousSearchString.length();
        int newLength = newSearchString.length();

        return newLength < previousLength
                || (newLength == previousLength && !newSearchString.equalsIgnoreCase(previousSearchString))
                || !newSearchString.toLowerCase(Locale.US).startsWith(previousSearchString.toLowerCase(Locale.US));
    }

    private void executeSearch(@NonNull final String trimmedSearchString) {
        final Callable<GiroPayIssuersResponse> callable = CheckoutApi
                .getInstance(mApplication)
                .getGiroPayIssuers(mPaymentMethod, trimmedSearchString);

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
                    final GiroPayIssuersResponse response = callable.call();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mCurrentSearch.getKey().startsWith(trimmedSearchString)) {
                                mCurrentSearch.setValue(response);
                                mCurrentSearchResults = new ArrayList<GiroPayIssuer>(response.getGiroPayIssuers());
                                mSearchResultsObservable.setValue(mCurrentSearchResults);
                            } else {
                                mCurrentSearch = null;
                            }
                        }
                    });
                } catch (Exception e) {
                    final CheckoutException checkoutException = e instanceof CheckoutException
                            ? (CheckoutException) e
                            : new CheckoutException.Builder("An error occured while searching for GiroPayIssuers.", e).build();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCurrentSearch = null;
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

    private void performFiltering(@NonNull String trimmedSearchString) {
        if (mCurrentSearch == null) {
            return;
        }

        GiroPayIssuersResponse giroPayIssuersResponse = mCurrentSearch.getValue();

        if (giroPayIssuersResponse == null) {
            return;
        }

        List<GiroPayIssuer> filteredGiroPayIssuers = new ArrayList<>();

        for (GiroPayIssuerImpl giroPayIssuer : giroPayIssuersResponse.getGiroPayIssuers()) {
            if (SearchUtil.anyMatches(trimmedSearchString, giroPayIssuer.getBankName(), giroPayIssuer.getBic(), giroPayIssuer.getBlz())) {
                filteredGiroPayIssuers.add(giroPayIssuer);
            }
        }

        if (!filteredGiroPayIssuers.equals(mCurrentSearchResults)) {
            mCurrentSearchResults = filteredGiroPayIssuers;
            mSearchResultsObservable.setValue(mCurrentSearchResults);
        }
    }

    private void runOnUiThread(@NonNull Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }
}
