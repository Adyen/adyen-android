/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 2/4/2019.
 */

package com.adyen.checkout.base.ui.util;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import android.util.TypedValue;

import com.adyen.checkout.base.ui.R;
import com.adyen.checkout.core.exception.NoConstructorException;

public final class ThemeUtil {

    @ColorInt
    public static int getPrimaryThemeColor(@NonNull Context context) {
        return getAttributeColor(context, R.attr.colorPrimary);
    }

    @ColorInt
    private static int getAttributeColor(@NonNull Context context, @AttrRes int attributeColor) {
        final TypedValue typedValue = new TypedValue();
        final TypedArray typedArray = context.obtainStyledAttributes(typedValue.data, new int[] {attributeColor});
        final int color = typedArray.getColor(0, 0);
        typedArray.recycle();

        return color;
    }

    private ThemeUtil() {
        throw new NoConstructorException();
    }
}
