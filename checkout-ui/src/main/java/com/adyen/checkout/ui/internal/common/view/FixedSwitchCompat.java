/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/06/2018.
 */

package com.adyen.checkout.ui.internal.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

import com.adyen.checkout.ui.R;

public class FixedSwitchCompat extends SwitchCompat {
    public FixedSwitchCompat(@NonNull Context context) {
        super(context);

        fixFontFamily(context);
    }

    public FixedSwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        fixFontFamily(context);
    }

    public FixedSwitchCompat(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        fixFontFamily(context);
    }

    private void fixFontFamily(@NonNull Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(new int[]{R.attr.fontFamily, android.R.attr.fontFamily});

        int fontResourceId = typedArray.getResourceId(0, 0);
        int fontAndroidResourceId = typedArray.getResourceId(1, 0);

        if (fontResourceId != 0) {
            Typeface font = ResourcesCompat.getFont(context, fontResourceId);
            setTypeface(font);
        } else if (fontAndroidResourceId != 0) {
            Typeface font = ResourcesCompat.getFont(context, fontAndroidResourceId);
            setTypeface(font);
        }

        typedArray.recycle();
    }
}
