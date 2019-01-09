/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/05/2018.
 */

package com.adyen.checkout.core;

import android.net.Uri;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * The {@link RedirectDetails} class describes all required parameters for a redirect.
 */
public interface RedirectDetails extends Parcelable {
    /**
     * @return The {@link Uri} to redirect to.
     */
    @NonNull
    Uri getUri();
}
