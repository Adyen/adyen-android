/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/3/2019.
 */

package com.adyen.checkout.base.api;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.api.Connection;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.io.IOException;

/**
 * Connection that gets a Logo {@link BitmapDrawable} from a URL.
 */
public final class LogoConnection extends Connection<BitmapDrawable> {
    private static final String TAG = LogUtil.getTag();

    LogoConnection(@NonNull String logoUrl) {
        super(logoUrl);
    }

    @NonNull
    @Override
    public BitmapDrawable call() throws IOException {
        Logger.v(TAG, "call - " + getUrl().hashCode());
        final byte[] bytes = get();
        final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return new BitmapDrawable(Resources.getSystem(), bitmap);
    }
}
