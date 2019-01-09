/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 18/10/2017.
 */

package com.adyen.checkout.ui.internal.common.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.graphics.drawable.TintAwareDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

import com.adyen.checkout.ui.R;

public final class ThemeUtil {
    @ColorInt
    public static int getPrimaryThemeColor(@NonNull Context context) {
        return getAttributeColor(context, R.attr.colorPrimary);
    }

    @ColorInt
    public static int getAttributeColor(@NonNull Context context, @AttrRes int attributeColor) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[] {attributeColor});
        int color = a.getColor(0, 0);
        a.recycle();

        return color;
    }

    public static void applyPrimaryThemeColor(@NonNull Context context, @NonNull Drawable... drawables) {
        int color = getPrimaryThemeColor(context);

        for (Drawable drawable : drawables) {
            if (drawable != null) {
                setTint(drawable, color);
            }
        }
    }

    /*
     * Setting the tint based on attribute colors is not supported on versions 23 and lower. This is necessary however to allow a dynamic UI based
     * on the current theme.
     */
    public static void setTintFromAttributeColor(@NonNull Context context, @NonNull Drawable drawable, @AttrRes int attributeColor) {
        int color = getAttributeColor(context, attributeColor);
        setTint(drawable, color);
    }

    @NonNull
    public static Context getThemedActionBarContext(@NonNull Activity activity) {
        if (activity instanceof AppCompatActivity) {
            ActionBar supportActionBar = ((AppCompatActivity) activity).getSupportActionBar();
            Context themedContext = supportActionBar != null ? supportActionBar.getThemedContext() : null;

            if (themedContext != null) {
                return themedContext;
            } else {
                return activity;
            }
        }

        android.app.ActionBar actionBar = activity.getActionBar();
        Context themedContext = actionBar != null ? actionBar.getThemedContext() : null;

        if (themedContext != null) {
            return themedContext;
        } else {
            return activity;
        }
    }

    private static void setTint(@NonNull Drawable drawable, @ColorInt int tint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || drawable instanceof TintAwareDrawable) {
            DrawableCompat.setTint(drawable, tint);
        } else {
            drawable.setColorFilter(tint, PorterDuff.Mode.SRC_IN);
        }
    }

    private ThemeUtil() {
        throw new IllegalStateException("No instances.");
    }
}
