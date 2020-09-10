/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 9/4/2019.
 */

package com.adyen.checkout.redirect;

import android.content.pm.ResolveInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

final class ResolveResult {

    private final Type mType;
    private final ResolveInfo mResolveInfo;

    ResolveResult(@NonNull Type type, @Nullable ResolveInfo resolveInfo) {
        mType = type;
        mResolveInfo = resolveInfo;
    }

    @NonNull
    Type getType() {
        return mType;
    }

    @Nullable
    ResolveInfo getResolveInfo() {
        return mResolveInfo;
    }

    public enum Type {
        RESOLVER_ACTIVITY,
        DEFAULT_BROWSER,
        APPLICATION,
        UNKNOWN
    }
}
