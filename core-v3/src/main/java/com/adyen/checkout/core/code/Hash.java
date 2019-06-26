/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/5/2019.
 */

package com.adyen.checkout.core.code;

import com.adyen.checkout.core.exeption.NoConstructorException;

public final class Hash {

    /**
     * Multiplier used for hashing.
     */
    public static final int MULT = 31;

    private Hash() {
        throw new NoConstructorException();
    }
}
