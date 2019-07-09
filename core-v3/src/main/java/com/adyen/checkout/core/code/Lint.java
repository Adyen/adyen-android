/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/3/2019.
 */

package com.adyen.checkout.core.code;

import com.adyen.checkout.core.exeption.NoConstructorException;

/**
 * Utility class for constants related to Lint.
 */
public final class Lint {

    public static final String WEAKER_ACCESS = "WeakerAccess";
    // Same as WEAKER_ACCESS but lets us know is kept public for merchant visibility.
    public static final String MERCHANT_VISIBLE = WEAKER_ACCESS;
    // Same as WEAKER_ACCESS but lets us know is specifically to avoid Synthetic Accessor.
    public static final String SYNTHETIC = WEAKER_ACCESS;
    // Also used to avoid Synthetic Accessor warnings in Kotlin
    public static final String PROTECTED_IN_FINAL = "ProtectedInFinal";

    private Lint() {
        throw new NoConstructorException();
    }
}
