package com.adyen.checkout.ui.internal.common.util.image;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.DrawableRes;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.content.res.AppCompatResources;

import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 01/05/2018.
 */
public abstract class Request implements Runnable {
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private final Rembrandt mRembrandt;

    private final RequestArgs mRequestArgs;

    Request(@NonNull Rembrandt rembrandt, @NonNull RequestArgs requestArgs) {
        mRembrandt = rembrandt;
        mRequestArgs = requestArgs;
    }

    @Override
    public final void run() {
        if (isCancelled()) {
            return;
        }

        final Drawable placeholderDrawable = loadDrawableResource(mRequestArgs.getPlaceholderResId());

        MAIN_HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (isCancelled()) {
                    release();
                } else {
                    onDrawableLoaded(placeholderDrawable);
                }
            }
        });

        if (isCancelled()) {
            MAIN_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    release();
                }
            });
            return;
        }

        try {
            final Drawable resultDrawable = mRequestArgs.getImageCallable().call();

            MAIN_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (isCancelled()) {
                        release();
                    } else {
                        onDrawableLoaded(resultDrawable);
                        release();
                    }
                }
            });
        } catch (Exception e) {
            final Drawable errorDrawable = loadDrawableResource(mRequestArgs.getErrorResId());

            MAIN_HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    if (isCancelled()) {
                        release();
                    } else {
                        onDrawableLoaded(errorDrawable);
                        release();
                    }
                }
            });
        }
    }

    @AnyThread
    abstract boolean isCancelled();

    @MainThread
    abstract void onDrawableLoaded(@Nullable Drawable drawable);

    @MainThread
    abstract void release();

    @Nullable
    private Drawable loadDrawableResource(@DrawableRes int drawableResId) {
        if (drawableResId != 0) {
            Application application = mRembrandt.getApplication();
            Drawable drawable = AppCompatResources.getDrawable(application, drawableResId);

            if (drawable != null) {
                ThemeUtil.setTintFromAttributeColor(application, drawable, R.attr.colorIconActive);
            }

            return drawable;
        } else {
            return null;
        }
    }
}
