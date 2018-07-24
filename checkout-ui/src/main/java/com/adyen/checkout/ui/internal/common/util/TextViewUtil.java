package com.adyen.checkout.ui.internal.common.util;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.InputFilter;
import android.widget.TextView;

import com.adyen.checkout.ui.R;

import java.util.Arrays;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 18/12/2017.
 */
public final class TextViewUtil {
    private static final int DRAWABLE_LEFT = 0;

    private static final int DRAWABLE_TOP = 1;

    private static final int DRAWABLE_RIGHT = 2;

    private static final int DRAWABLE_BOTTOM = 3;

    public static void addInputFilter(@NonNull TextView textView, @NonNull InputFilter... inputFilters) {
        InputFilter[] filters = textView.getFilters();

        if (filters == null) {
            filters = inputFilters;
        } else {
            int currentLength = filters.length;
            int toBeAddedLength = inputFilters.length;
            filters = Arrays.copyOf(filters, currentLength + toBeAddedLength);
            System.arraycopy(inputFilters, 0, filters, currentLength, toBeAddedLength);
        }

        textView.setFilters(filters);
    }

    public static void removeInputFilter(@NonNull TextView textView, @NonNull InputFilter... inputFilters) {
        InputFilter[] filters = textView.getFilters();

        if (filters != null) {
            for (InputFilter inputFilter : inputFilters) {
                filters = removeInputFilter(filters, inputFilter);
            }

            textView.setFilters(filters);
        }
    }

    public static void setDefaultTextColor(@NonNull TextView textView) {
        int color = ThemeUtil.getAttributeColor(textView.getContext(), android.R.attr.textColorPrimary);
        textView.setTextColor(color);
    }

    public static void setErrorTextColor(@NonNull TextView textView) {
        int color = ThemeUtil.getAttributeColor(textView.getContext(), R.attr.colorError);
        textView.setTextColor(color);
    }

    public static void setCompoundDrawableLeft(@NonNull TextView textView, @Nullable Drawable drawableLeft) {
        ensureBoundsSet(drawableLeft);
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        textView.setCompoundDrawables(
                drawableLeft,
                compoundDrawables[DRAWABLE_TOP],
                compoundDrawables[DRAWABLE_RIGHT],
                compoundDrawables[DRAWABLE_BOTTOM]
        );
    }

    public static void setCompoundDrawableRight(@NonNull TextView textView, @Nullable Drawable drawableRight) {
        ensureBoundsSet(drawableRight);
        Drawable[] compoundDrawables = textView.getCompoundDrawables();
        textView.setCompoundDrawables(
                compoundDrawables[DRAWABLE_LEFT],
                compoundDrawables[DRAWABLE_TOP],
                drawableRight,
                compoundDrawables[DRAWABLE_BOTTOM]
        );
    }

    private static void ensureBoundsSet(@Nullable Drawable drawable) {
        if (drawable != null && drawable.getBounds().isEmpty()) {
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }
    }

    @NonNull
    private static InputFilter[] removeInputFilter(@NonNull InputFilter[] inputFilters, @NonNull InputFilter inputFilter) {
        int index = -1;

        for (int i = 0; i < inputFilters.length; i++) {
            if (inputFilters[i] == inputFilter) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            int newLength = inputFilters.length - 1;
            InputFilter[] target = new InputFilter[newLength];
            System.arraycopy(inputFilters, index + 1, target, index, newLength - index);

            return target;
        } else {
            return inputFilters;
        }
    }

    private TextViewUtil() {
        throw new IllegalStateException("No instances.");
    }
}
