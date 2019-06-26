/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */

package com.adyen.checkout.card;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.api.LogoConnectionTask;

public class CardLogoCallback implements LogoConnectionTask.LogoCallback {

    private final String mIssuerId;
    private final DrawableFetchedCallback mDrawableFetchedCallback;

    CardLogoCallback(@NonNull String issuerId, @NonNull DrawableFetchedCallback drawableFetchedCallback) {
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

    interface DrawableFetchedCallback {
        void onDrawableFetched(@NonNull String id, @Nullable Drawable drawable);
    }
}
