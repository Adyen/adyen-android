/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 19/07/2018.
 */

package com.adyen.checkout.core;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.adyen.checkout.core.internal.GiroPayIssuerSearchHandler;
import com.adyen.checkout.core.internal.SsnLookupSearchHandler;
import com.adyen.checkout.core.model.GiroPayIssuer;
import com.adyen.checkout.core.model.KlarnaConfiguration;
import com.adyen.checkout.core.model.PaymentMethod;
import com.adyen.checkout.core.model.PaymentSession;
import com.adyen.checkout.core.model.KlarnaSsnLookupResponse;

import java.util.List;

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

        /**
         * Create a {@link SearchHandler} that provides {@link KlarnaSsnLookupResponse} based on a Social Security Number.
         * The expected SSN format for the search is a number of "YY MM DD NNNN" with or without separating spaces.
         * This is currently only available for Klarna PaymentMethod and in Sweden.
         * If the search response is empty (i.e. the SSN was not found) the ErrorObservable will the called.
         *
         * @param application The current {@link Application}.
         * @param paymentMethod The {@link PaymentMethod} for Klarna.
         * @param paymentSession The {@link PaymentSession}
         * @return The {@link SearchHandler} that provides {@link KlarnaSsnLookupResponse}. Returns null if it's not available.
         */
        @Nullable
        public static SearchHandler<KlarnaSsnLookupResponse> createKlarnaSsnLookupSearchHandler(
                @NonNull Application application,
                @NonNull PaymentSession paymentSession,
                @NonNull PaymentMethod paymentMethod) {

            String ssnLookupUrl;
            try {
                KlarnaConfiguration configuration = paymentMethod.getConfiguration(KlarnaConfiguration.class);
                ssnLookupUrl = configuration.getShopperInfoSsnLookupUrl();
            } catch (CheckoutException e) {
                //configuration might not be present, which means SSN lookup is not available
                return null;
            }
            if (TextUtils.isEmpty(ssnLookupUrl)) {
                return null;
            } else {
                return new SsnLookupSearchHandler(application, paymentMethod, paymentSession, ssnLookupUrl);
            }
        }

        private Factory() {
            throw new IllegalStateException("No instances.");
        }
    }
}
