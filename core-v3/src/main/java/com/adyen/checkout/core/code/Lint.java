/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/3/2019.
 */

package com.adyen.checkout.core.code;

import com.adyen.checkout.core.exception.NoConstructorException;

/**
 * Utility class for constants related to Lint.
 */
public final class Lint {

    // Same as WEAKER_ACCESS but lets us know is kept public for merchant visibility.
    public static final String MERCHANT_VISIBLE = "WeakerAccess";

    private Lint() {
        throw new NoConstructorException();
    }
}
