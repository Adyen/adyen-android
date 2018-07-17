package com.adyen.checkout.ui.internal.common.util.image;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;

import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 01/05/2018.
 */
public final class RequestArgs {
    private final Rembrandt mRembrandt;

    private final Callable<Drawable> mImageCallable;

    private int mPlaceholderResId;

    private int mErrorResId;

    @NonNull
    public static RequestArgs.Builder newBuilder(@NonNull Rembrandt rembrandt, @NonNull Callable<Drawable> imageCallable) {
        return new RequestArgs(rembrandt, imageCallable).new Builder();
    }

    private RequestArgs(@NonNull Rembrandt rembrandt, @NonNull Callable<Drawable> imageCallable) {
        mRembrandt = rembrandt;
        mImageCallable = imageCallable;
    }

    public void into(@NonNull LifecycleOwner lifecycleOwner, @NonNull ImageView target) {
        into(lifecycleOwner, new Target.ImageView(target));
    }

    public void into(@NonNull LifecycleOwner lifecycleOwner, @NonNull Target target) {
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        LifecycleAwareTargetRequest request = new LifecycleAwareTargetRequest(mRembrandt, this, lifecycle, target);
        mRembrandt.startRequest(request);
    }

    public void into(@NonNull LifecycleOwner lifecycleOwner, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull ImageView target) {
        into(lifecycleOwner, viewHolder, new Target.ImageView(target));
    }

    public void into(@NonNull LifecycleOwner lifecycleOwner, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull Target target) {
        Lifecycle lifecycle = lifecycleOwner.getLifecycle();
        ViewHolderRequest request = new ViewHolderRequest(mRembrandt, this, lifecycle, viewHolder, target);
        mRembrandt.startRequest(request);
    }

    @NonNull
    public Callable<Drawable> getImageCallable() {
        return mImageCallable;
    }

    @DrawableRes
    public int getPlaceholderResId() {
        return mPlaceholderResId;
    }

    @DrawableRes
    public int getErrorResId() {
        return mErrorResId;
    }

    public final class Builder {
        @NonNull
        public Builder placeholder(@DrawableRes int placeholderResId) {
            mPlaceholderResId = placeholderResId;

            return this;
        }

        @NonNull
        public Builder error(@DrawableRes int errorResId) {
            mErrorResId = errorResId;

            return this;
        }

        @NonNull
        public RequestArgs build() {
            return RequestArgs.this;
        }
    }
}
