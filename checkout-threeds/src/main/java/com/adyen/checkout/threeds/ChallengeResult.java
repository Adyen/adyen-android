/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by Ran Haveshush on 20/11/2018.
 */

package com.adyen.checkout.threeds;

import android.support.annotation.NonNull;

public interface ChallengeResult {
    /**
     * @return <code>true</code> if the shopper is authenticated by the 3DS challenge,
     * <code>false</code> otherwise.
     */
    boolean isAuthenticated();

    /**
     * @return The 3DS challenge result payload.
     */
    @NonNull
    String getPayload();
}
