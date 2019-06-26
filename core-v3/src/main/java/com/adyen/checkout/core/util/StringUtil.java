/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/5/2019.
 */

package com.adyen.checkout.core.util;

import android.support.annotation.Nullable;

import com.adyen.checkout.core.exeption.NoConstructorException;

public final class StringUtil {

    public static boolean hasContent(@Nullable String string) {
        return string != null && !string.isEmpty();
    }

    private StringUtil() {
        throw new NoConstructorException();
    }
}
