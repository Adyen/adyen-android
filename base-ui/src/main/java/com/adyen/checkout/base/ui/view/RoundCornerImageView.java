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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.adyen.checkout.base.ui.R;

public class RoundCornerImageView extends AppCompatImageView {

    public static final float DEFAULT_RADIUS = 9.0f;
    public static final int DEFAULT_STROKE_COLOR = Color.BLACK;
    public static final float DEFAULT_STROKE_WIDTH = 4f;

    private float mRadius;
    private final Paint mStrokePaint = new Paint();
    private float mStrokeWidth;
    private int mStrokeColor;

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
        applyAttrs(typedArrayAttrs);
    }

    public void setRadius(float radius) {
        this.mRadius = radius;
        invalidate();
    }

    public void setStrokeColor(@ColorInt int color) {
        this.mStrokeColor = color;
        invalidate();
    }

    public void setStrokeWidth(float width) {
        this.mStrokeWidth = width;
        invalidate();
    }

    private void applyAttrs(TypedArray typedArrayAttrs) {
        try {
            mStrokeColor = typedArrayAttrs.getColor(R.styleable.RoundCornerImageView_strokeColor, DEFAULT_STROKE_COLOR);
            mStrokeWidth = typedArrayAttrs.getDimension(R.styleable.RoundCornerImageView_strokeWidth, DEFAULT_STROKE_WIDTH);
            mRadius = typedArrayAttrs.getDimension(R.styleable.RoundCornerImageView_radius, DEFAULT_RADIUS);
        } finally {
            typedArrayAttrs.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec + (int) mStrokeWidth * 2, heightMeasureSpec + (int) mStrokeWidth * 2);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        final RectF rect = new RectF(mStrokeWidth / 2, mStrokeWidth / 2,
                getWidth() - (mStrokeWidth / 2),
                getHeight() - (mStrokeWidth / 2));

        mStrokePaint.reset();

        if (mStrokeWidth > 0) {
            mStrokePaint.setStyle(Paint.Style.STROKE);
            mStrokePaint.setAntiAlias(true);
            mStrokePaint.setColor(mStrokeColor);
            mStrokePaint.setStrokeWidth(mStrokeWidth);

            canvas.drawRoundRect(rect, mRadius, mRadius, mStrokePaint);
        }

        final Path path = new Path();
        path.addRoundRect(rect, mRadius, mRadius, Path.Direction.CW);
        canvas.clipPath(path);

        super.onDraw(canvas);
    }
}
