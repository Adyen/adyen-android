/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */

package com.adyen.checkout.base.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.adyen.checkout.base.ui.R;

public class RoundCornerImageView extends AppCompatImageView {

    private static final float DEFAULT_RADIUS = 9.0f;

    private float mRadius;
    private final Path mPath = new Path();

    public RoundCornerImageView(@NonNull Context context) {
        this(context, null);
    }

    public RoundCornerImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * ImageView that add corner to loaded drawable.
     */
    public RoundCornerImageView(@NonNull Context context, @Nullable AttributeSet attrs, @Nullable int defStyle) {
        super(context, attrs, defStyle);

        final TypedArray typedArrayAttrs = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundCornerImageView,
                0, 0);
        applyRadius(typedArrayAttrs);
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
    }

    private void applyRadius(TypedArray typedArrayAttrs) {
        try {
            mRadius = typedArrayAttrs.getFloat(R.styleable.RoundCornerImageView_radius, DEFAULT_RADIUS);
        } finally {
            typedArrayAttrs.recycle();
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        final RectF rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        mPath.addRoundRect(rect, mRadius, mRadius, Path.Direction.CW);
        canvas.clipPath(mPath);
        super.onDraw(canvas);
    }
}
