/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */
package com.adyen.checkout.components.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.adyen.checkout.components.ui.R

/**
 * ImageView that adds a corner to the loaded drawable.
 */
class RoundCornerImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatImageView(context, attrs, defStyle) {

    private val strokePaint = Paint()

    var radius = 0f
        set(value) {
            field = value
            invalidate()
        }
    var strokeWidth = 0f
        set(value) {
            field = value
            invalidate()
        }
    var strokeColor = 0
        set(value) {
            field = value
            invalidate()
        }
    var borderEnabled = true
        set(value) {
            field = value
            invalidate()
        }

    init {
        val typedArrayAttrs = context.theme.obtainStyledAttributes(
            attrs, R.styleable.RoundCornerImageView,
            0, 0
        )
        applyAttrs(typedArrayAttrs)
    }

    private fun applyAttrs(typedArrayAttrs: TypedArray) {
        try {
            strokeColor = typedArrayAttrs.getColor(R.styleable.RoundCornerImageView_strokeColor, DEFAULT_STROKE_COLOR)
            strokeWidth =
                typedArrayAttrs.getDimension(R.styleable.RoundCornerImageView_strokeWidth, DEFAULT_STROKE_WIDTH)
            radius = typedArrayAttrs.getDimension(R.styleable.RoundCornerImageView_radius, DEFAULT_RADIUS)
        } finally {
            typedArrayAttrs.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec + strokeWidth.toInt() * 2, heightMeasureSpec + strokeWidth.toInt() * 2)
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        if (!borderEnabled) {
            super.onDraw(canvas)
            return
        }
        val rect = RectF(
            strokeWidth / 2, strokeWidth / 2,
            width - strokeWidth / 2,
            height - strokeWidth / 2
        )
        strokePaint.reset()
        if (strokeWidth > 0) {
            strokePaint.style = Paint.Style.STROKE
            strokePaint.isAntiAlias = true
            strokePaint.color = strokeColor
            strokePaint.strokeWidth = strokeWidth
            canvas.drawRoundRect(rect, radius, radius, strokePaint)
        }
        val path = Path()
        path.addRoundRect(rect, radius, radius, Path.Direction.CW)
        canvas.clipPath(path)
        super.onDraw(canvas)
    }

    companion object {
        const val DEFAULT_RADIUS = 9.0f
        const val DEFAULT_STROKE_COLOR = Color.BLACK
        const val DEFAULT_STROKE_WIDTH = 4f
    }
}
