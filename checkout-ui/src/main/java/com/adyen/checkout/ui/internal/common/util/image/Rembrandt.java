package com.adyen.checkout.ui.internal.common.util.image;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.adyen.checkout.ui.R;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 29/03/2018.
 */
public final class Rembrandt {
    @SuppressWarnings("StaticFieldLeak")
    private static Rembrandt sInstance;

    private final ExecutorService mBackgroundExecutorService = Executors.newFixedThreadPool(10);

    private final Application mApplication;

    @NonNull
    public static synchronized Rembrandt get(@NonNull Application application) {
        if (sInstance == null) {
            sInstance = new Rembrandt(application);
        }

        return sInstance;
    }

    @NonNull
    public static RequestArgs createDefaultLogoRequestArgs(@NonNull Application application, @NonNull Callable<Drawable> callable) {
        Callable<Drawable> wrappedCallable = DrawablePreProcessor.wrapCallable(application, callable);

        return get(application)
                .load(wrappedCallable)
                .placeholder(R.drawable.ic_image_black_24dp)
                .error(R.drawable.ic_broken_image_black_24dp)
                .build();
    }

    private Rembrandt(@NonNull Application application) {
        mApplication = application;
    }

    @NonNull
    public RequestArgs.Builder load(@NonNull Callable<Drawable> imageCallable) {
        return RequestArgs.newBuilder(this, imageCallable);
    }

    @NonNull
    Application getApplication() {
        return mApplication;
    }

    void startRequest(@NonNull Request request) {
        mBackgroundExecutorService.submit(request);
    }
}
