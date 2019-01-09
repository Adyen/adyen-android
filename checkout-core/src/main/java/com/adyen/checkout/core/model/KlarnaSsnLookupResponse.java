/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/11/2018.
 */

package com.adyen.checkout.core.model;

import android.app.Application;
import android.support.annotation.NonNull;

/**
 * Result from a {@link com.adyen.checkout.core.SearchHandler} that looks for data of a person matching the corresponding Social Security Number.
 * See {@link com.adyen.checkout.core.SearchHandler.Factory#createKlarnaSsnLookupSearchHandler(Application, PaymentSession, PaymentMethod)}
 */
public interface KlarnaSsnLookupResponse {

    /**
     * @return The {@link Address} of the person with that SSN.
     */
    @NonNull
    Address getAddress();

    /**
     * @return The {@link Name} of the person with that SSN.
     */
    @NonNull
    Name getName();
}
