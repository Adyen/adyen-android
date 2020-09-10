/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 8/1/2020.
 */

package com.adyen.checkout.base.util;

import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.NoConstructorException;

public final class BrowserUtils {

    /**
     * Creates the intent that will start the browsers.
     *
     * @param uri The Uri to redirect to.
     * @return And intent that targets either another app or a Web page.
     */
    @NonNull
    public static Intent createBrowserIntent(@NonNull Uri uri) {
        return new Intent(Intent.ACTION_VIEW, uri);
    }

    private BrowserUtils() {
        throw new NoConstructorException();
    }
}
