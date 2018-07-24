package com.adyen.checkout.ui.internal.common.util.image;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Px;
import android.support.v4.content.ContextCompat;

import com.adyen.checkout.ui.R;

import java.util.concurrent.Callable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/03/2018.
 */
public final class DrawablePreProcessor {
    private static final float CORNER_RADIUS_FACTOR = 1f / 8f;

    @SuppressLint("StaticFieldLeak")
    private static DrawablePreProcessor sInstance;

    private final Application mApplication;

    @NonNull
    public static synchronized DrawablePreProcessor getInstance(@NonNull Application application) {
        if (sInstance == null) {
            sInstance = new DrawablePreProcessor(application);
        }

        return sInstance;
    }

    @NonNull
    public static Callable<Drawable> wrapCallable(@NonNull Application application, @NonNull final Callable<Drawable> callable) {
        final DrawablePreProcessor instance = getInstance(application);

        return new Callable<Drawable>() {
            @Override
            public Drawable call() throws Exception {
                Drawable drawable = callable.call();

                if (drawable != null) {
                    drawable = instance.preProcess(drawable);
                }

                return drawable;
            }
        };
    }

    private DrawablePreProcessor(@NonNull Application application) {
        mApplication = application;
    }

    @NonNull
    private Drawable preProcess(@NonNull Drawable drawable) {
        Bitmap bitmap = convertToBitmap(drawable);
        int cornerRadius = (int) (bitmap.getHeight() * CORNER_RADIUS_FACTOR);
        int color = ContextCompat.getColor(mApplication, R.color.drawable_border);

        return makeRoundCornersAndAddBorder(mApplication, bitmap, cornerRadius, color);
    }

    @NonNull
    private Bitmap convertToBitmap(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            int width = drawable.getIntrinsicWidth();
            int height = drawable.getIntrinsicHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            bitmap.setDensity(Resources.getSystem().getDisplayMetrics().densityDpi);

            return bitmap;
        }
    }

    @NonNull
    private Drawable makeRoundCornersAndAddBorder(
            @NonNull Context context,
            @NonNull Bitmap bitmap,
            @Px int cornerRadius,
            @ColorInt int borderColor
    ) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), makeRoundCorners(bitmap, cornerRadius));
        bitmapDrawable.setAntiAlias(true);

        return addBorder(bitmapDrawable, cornerRadius, borderColor);
    }

    @NonNull
    private Bitmap makeRoundCorners(@NonNull Bitmap bitmap, @Px int cornerRadius) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        output.setDensity(Resources.getSystem().getDisplayMetrics().densityDpi);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        Rect rectTarget = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, rect, rectTarget, paint);

        return output;
    }

    @NonNull
    private Drawable addBorder(@NonNull Drawable drawable, @Px int cornerRadius, @ColorInt int borderColor) {
        Drawable[] drawables = new Drawable[2];
        drawables[0] = drawable;
        drawables[1] = getBorderDrawable(cornerRadius, borderColor);

        return new LayerDrawable(drawables);
    }

    @NonNull
    private Drawable getBorderDrawable(@Px int cornerRadius, @ColorInt int borderColor) {
        GradientDrawable borderDrawable = new GradientDrawable();
        borderDrawable.setCornerRadius(cornerRadius);
        borderDrawable.setStroke(1, borderColor);

        return borderDrawable;
    }
}
