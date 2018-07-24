package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.TypedValue;

import com.adyen.checkout.ui.R;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 18/10/2017.
 */
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
        PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);

        for (Drawable drawable : drawables) {
            if (drawable != null) {
                drawable.setColorFilter(colorFilter);
            }
        }
    }

    private ThemeUtil() {
        throw new IllegalStateException("No instances.");
    }
}
