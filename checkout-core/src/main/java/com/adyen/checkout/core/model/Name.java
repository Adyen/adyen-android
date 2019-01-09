/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/12/2018.
 */

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

/**
 * Compound response object with the person's First and Last names.
 */
public interface Name {

    /**
     * @return The person's first name.
     */
    @NonNull
    String getFirstName();

    /**
     * @return The person's last name.
     */
    @NonNull
    String getLastName();

}
