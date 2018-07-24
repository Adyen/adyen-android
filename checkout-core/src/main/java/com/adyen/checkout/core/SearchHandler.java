package com.adyen.checkout.core;

import android.app.Application;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.GiroPayIssuerSearchHandler;
import com.adyen.checkout.core.model.GiroPayIssuer;
import com.adyen.checkout.core.model.PaymentMethod;

import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 19/07/2018.
 */
public interface SearchHandler<R> {
    /**
     * Set the search string that should be used to search for results.
     *
     * @param value The search string.
     */
    void setSearchString(@NonNull String value);

    /**
     * @return An {@link Observable} that provides {@link NetworkingState} information.
     */
    @NonNull
    Observable<NetworkingState> getNetworkInfoObservable();

    /**
     * @return An {@link Observable} that provides search results based on the search string.
     * @see #setSearchString(String)
     */
    @NonNull
    Observable<R> getSearchResultsObservable();

    /**
     * @return An {@link Observable} that provides errors that occurred while searching.
     */
    @NonNull
    Observable<CheckoutException> getErrorObservable();

    /**
     * Factory class for available {@link SearchHandler SearchHandlers}.
     */
    final class Factory {
        /**
         * Create a {@link SearchHandler} that provides {@link GiroPayIssuer GiroPayIssuers}.
         *
         * @param application The current {@link Application}.
         * @param paymentMethod The GiroPay {@link PaymentMethod}.
         * @return The {@link SearchHandler} that provides {@link GiroPayIssuer GiroPayIssuers}.
         */
        @NonNull
        public static SearchHandler<List<GiroPayIssuer>> createGiroPayIssuerSearchHandler(
                @NonNull Application application,
                @NonNull PaymentMethod paymentMethod
        ) {
            return new GiroPayIssuerSearchHandler(application, paymentMethod);
        }

        private Factory() {
            throw new IllegalStateException("No instances.");
        }
    }
}
