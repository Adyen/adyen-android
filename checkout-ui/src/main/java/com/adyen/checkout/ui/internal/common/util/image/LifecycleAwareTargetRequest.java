/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/05/2018.
 */

package com.adyen.checkout.ui.internal.common.util.image;

import android.arch.lifecycle.Lifecycle;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class LifecycleAwareTargetRequest extends LifecycleAwareRequest {
    private Target mTarget;

    LifecycleAwareTargetRequest(
            @NonNull Rembrandt rembrandt,
            @NonNull RequestArgs requestArgs,
            @NonNull Lifecycle lifecycle,
            @NonNull Target target
    ) {
        super(rembrandt, requestArgs, lifecycle);

        mTarget = target;
    }

    @Override
    void onDrawableLoaded(@Nullable Drawable drawable) {
        if (mTarget != null) {
            mTarget.setImageDrawable(drawable);
        }
    }

    @Override
    void release() {
        super.release();

        mTarget = null;
    }
}
