/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 26/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.api.LogoConnectionTask;

public class IssuerLogoCallback implements LogoConnectionTask.LogoCallback {

    private final String mIssuerId;
    private final DrawableFetchedCallback mDrawableFetchedCallback;

    IssuerLogoCallback(@NonNull String issuerId, @NonNull DrawableFetchedCallback drawableFetchedCallback) {
        mIssuerId = issuerId;
        mDrawableFetchedCallback = drawableFetchedCallback;
    }

    @Override
    public void onLogoReceived(@NonNull BitmapDrawable drawable) {
        mDrawableFetchedCallback.onDrawableFetched(mIssuerId, drawable);
    }

    @Override
    public void onReceiveFailed() {
        mDrawableFetchedCallback.onDrawableFetched(mIssuerId, null);
    }

    /**
     * The callback for when the IssuerModel Drawable was fetched.
     * Drawable can be null if fetching failed.
     */
    interface DrawableFetchedCallback {
        void onDrawableFetched(@NonNull String id, @Nullable Drawable drawable);
    }
}
