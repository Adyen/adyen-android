/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 10/1/2022.
 */

package com.adyen.checkout.components.ui.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.ViewCompat
import androidx.core.view.children
import androidx.customview.widget.ViewDragHelper
import com.adyen.checkout.core.exception.CheckoutException
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val CHILD_COUNT = 2

@Suppress("TooManyFunctions")
class AdyenSwipeToRevealLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private lateinit var mainView: View
    private lateinit var underlayView: View

    @Volatile private var isDragging = false
    @Volatile private var isDragLocked = false

    private val rectMainDragged = Rect()
    private val rectMainNotDragged = Rect()

    private val rectUnderlayDragged = Rect()
    private val rectUnderlayNotDragged = Rect()

    private var dragDistance = 0f
    private var previousX = -1f

    private lateinit var dragHelper: ViewDragHelper
    private var gestureDetector: GestureDetectorCompat
    private var underlayListener: UnderlayListener? = null
    private var onClickListener: OnClickListener? = null

    private val viewDragHelperCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return if (!isDragLocked) {
                dragHelper.captureChildView(mainView, pointerId)
                true
            } else {
                false
            }
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return max(
                min(left, rectMainNotDragged.left),
                rectMainNotDragged.left - underlayView.width
            )
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val pivotPoint = rectMainNotDragged.right - underlayView.width / 2
            if (mainView.right < pivotPoint) {
                expandUnderlay()
            } else {
                collapseUnderlay()
            }
        }

        override fun onEdgeDragStarted(edgeFlags: Int, pointerId: Int) {
            super.onEdgeDragStarted(edgeFlags, pointerId)

            if (isDragLocked) return

            if (edgeFlags == ViewDragHelper.EDGE_LEFT) {
                dragHelper.captureChildView(mainView, pointerId)
            }
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            super.onViewPositionChanged(changedView, left, top, dx, dy)
            ViewCompat.postInvalidateOnAnimation(this@AdyenSwipeToRevealLayout)
        }
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean {
            isDragging = false
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent?,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            isDragging = true
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true)
            }
            return false
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent?,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            isDragging = true
            return false
        }

        override fun onLongPress(e: MotionEvent) {
            val isUnderlayHidden = mainView.right == rectMainNotDragged.right
            val didHitMainView = e.x >= mainView.left && e.x <= mainView.right &&
                e.y >= mainView.top && e.y <= mainView.bottom
            if (isUnderlayHidden && didHitMainView && !isDragLocked) {
                expandUnderlay()
            }
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val didHitMainView = e.x >= mainView.left && e.x <= mainView.right &&
                e.y >= mainView.top && e.y <= mainView.bottom
            return if (didHitMainView) {
                onClickListener?.onClick()
                true
            } else {
                super.onSingleTapConfirmed(e)
            }
        }
    }

    init {
        dragHelper = ViewDragHelper.create(this, 1f, viewDragHelperCallback)
        dragHelper.setEdgeTrackingEnabled(ViewDragHelper.EDGE_ALL)
        gestureDetector = GestureDetectorCompat(context, gestureListener)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)

        var largestChildWidth = 0
        var largestChildHeight = 0

        children.forEach { childView ->
            measureChild(childView, widthMeasureSpec, heightMeasureSpec)
            if (largestChildHeight < childView.measuredHeight) largestChildHeight = childView.measuredHeight
            if (largestChildWidth < childView.measuredWidth) largestChildWidth = childView.measuredWidth
        }

        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(largestChildHeight, heightSpecMode)
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(largestChildWidth, widthSpecMode)

        val rootMeasuredHeight = MeasureSpec.getSize(newHeightMeasureSpec)
        val rootMeasuredWidth = MeasureSpec.getSize(newWidthMeasureSpec)

        children.forEach { childView ->
            val childViewLayoutParams = childView.layoutParams

            if (childViewLayoutParams != null) {
                if (childViewLayoutParams.height == LayoutParams.MATCH_PARENT) {
                    childView.minimumHeight = rootMeasuredHeight
                }

                if (childViewLayoutParams.width == LayoutParams.MATCH_PARENT) {
                    childView.minimumWidth = rootMeasuredWidth
                }
            }

            measureChild(childView, newWidthMeasureSpec, newHeightMeasureSpec)
            largestChildWidth = max(childView.measuredWidth, largestChildWidth)
            largestChildHeight = max(childView.measuredHeight, largestChildHeight)
        }

        largestChildHeight += paddingTop + paddingBottom
        largestChildWidth += paddingLeft + paddingRight

        if (widthSpecMode == MeasureSpec.EXACTLY || layoutParams.width == LayoutParams.MATCH_PARENT) {
            largestChildWidth = rootMeasuredWidth
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            min(rootMeasuredWidth, largestChildWidth)
        }

        if (heightSpecMode == MeasureSpec.EXACTLY || layoutParams.height == LayoutParams.MATCH_PARENT) {
            largestChildHeight = rootMeasuredHeight
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            min(rootMeasuredHeight, largestChildHeight)
        }

        setMeasuredDimension(largestChildWidth, largestChildHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        children.forEach { childView ->
            val childViewLayoutParams = childView.layoutParams

            var childWidth = childView.measuredWidth
            var childHeight = childView.measuredHeight

            if (childViewLayoutParams.height == LayoutParams.MATCH_PARENT) {
                childHeight = b - t - paddingTop - paddingBottom
                childViewLayoutParams.height = childHeight
            }

            if (childViewLayoutParams.width == LayoutParams.MATCH_PARENT) {
                childWidth = r - l - paddingLeft - paddingRight
                childViewLayoutParams.width = childWidth
            }

            val left = max(r - childWidth - paddingRight - l, paddingLeft)
            val right = max(r - l - paddingRight, paddingLeft)
            val bottom = max(childHeight + paddingTop, max(b - t - paddingBottom, 0))
            val top = min(paddingTop, bottom)

            childView.layout(left, top, right, bottom)
        }

        rectMainNotDragged.set(
            mainView.left,
            mainView.top,
            mainView.right,
            mainView.bottom
        )

        rectUnderlayNotDragged.set(
            underlayView.left,
            underlayView.top,
            underlayView.right,
            underlayView.bottom
        )

        rectMainDragged.set(
            rectMainNotDragged.left - underlayView.width,
            rectMainNotDragged.top,
            rectMainNotDragged.left + mainView.width - underlayView.width,
            rectMainNotDragged.top + mainView.height
        )

        rectUnderlayDragged.set(
            rectUnderlayNotDragged.left,
            rectUnderlayNotDragged.top,
            rectUnderlayNotDragged.left + underlayView.width,
            rectUnderlayNotDragged.top + underlayView.height
        )
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        when (childCount) {
            CHILD_COUNT -> {
                mainView = getChildAt(1)
                underlayView = getChildAt(0)
            }
            else -> throw CheckoutException("${this.javaClass.simpleName} must contain two children.")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        dragHelper.processTouchEvent(event)
        return true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        dragHelper.processTouchEvent(ev)

        calculateDragDistance(ev)

        val isIdle = dragHelper.viewDragState == ViewDragHelper.STATE_IDLE && isDragging
        val canPerformClickOnUnderlay = ev.x >= mainView.right && ev.x <= mainView.left &&
            ev.y >= mainView.top && ev.y <= mainView.bottom &&
            dragDistance < dragHelper.touchSlop
        val isSettling = dragHelper.viewDragState == ViewDragHelper.STATE_SETTLING

        previousX = ev.x

        return !canPerformClickOnUnderlay && (isSettling || isIdle)
    }

    override fun computeScroll() {
        if (dragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    fun setDragLocked(isDragLocked: Boolean) {
        this.isDragLocked = isDragLocked
    }

    fun setUnderlayListener(underlayListener: UnderlayListener) {
        this.underlayListener = underlayListener
    }

    fun removeUnderlayListener() {
        this.underlayListener = null
    }

    fun setClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    private fun calculateDragDistance(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_DOWN) {
            dragDistance = 0f
            return
        }

        dragDistance += abs(event.x - previousX)
    }

    private fun expandUnderlay() {
        dragHelper.smoothSlideViewTo(mainView, rectMainDragged.left, rectMainDragged.top)
        ViewCompat.postInvalidateOnAnimation(this)
        underlayListener?.onUnderlayDragged(this)
    }

    fun collapseUnderlay() {
        dragHelper.smoothSlideViewTo(mainView, rectMainNotDragged.left, rectMainNotDragged.top)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    fun interface UnderlayListener {
        fun onUnderlayDragged(view: AdyenSwipeToRevealLayout)
    }

    fun interface OnClickListener {
        fun onClick()
    }
}
