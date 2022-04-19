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
import com.google.android.material.elevation.ElevationOverlayProvider
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val CHILD_COUNT = 2

private const val ELEVATION_CORRECTION = 56f

/**
 * A swipeable view that contains two child views which are:
 *
 * Underlay View: A [View] instance which is gonna be placed in the background and will be shown
 * once user swipes the Main View from right to left. When defining your layout, your underlay view
 * needs to be placed before your main view within [AdyenSwipeToRevealLayout].
 *
 * Main View: A [View] instance which is gonna be shown in the foreground. When defining your layout,
 * your main view needs to be placed after your underlay view within [AdyenSwipeToRevealLayout].
 *
 * Example:
 *
 * <com.adyen.checkout.components.ui.view.AdyenSwipeToRevealLayout>
 *     <View android:id="@+id/yourUnderlayView" ... />
 *     <View android:id="@+id/yourMainView" ... />
 * </com.adyen.checkout.components.ui.view.AdyenSwipeToRevealLayout>
 *
 */
@Suppress("TooManyFunctions")
class AdyenSwipeToRevealLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    /**
     * Underlay View: Needs to be placed first in layout.
     */
    private lateinit var underlayView: View

    /**
     * Main View: Needs to be placed second in layout.
     */
    private lateinit var mainView: View

    /**
     * Flag to track if the view is being dragged at the moment.
     */
    @Volatile private var isDragging = false

    /**
     * Flag to track if the dragging is enabled or disabled.
     */
    @Volatile private var isDragLocked = false

    /**
     * [Rect] instances to store boundaries of the main view in both dragged and not dragged states.
     */
    private val rectMainDragged = Rect()
    private val rectMainNotDragged = Rect()

    /**
     * [Rect] instances to store boundaries of the underlay view in both dragged and not dragged states.
     */
    private val rectUnderlayDragged = Rect()
    private val rectUnderlayNotDragged = Rect()

    /**
     * Distance of the current drag gesture.
     */
    private var dragDistance = 0f

    /**
     * Previous x-coordinate of the main view that's being dragged.
     * It is used calculating the [dragDistance].
     */
    private var previousX = -1f

    /**
     * [ViewDragHelper] instance to capture the dragging of the view.
     */
    private lateinit var dragHelper: ViewDragHelper

    /**
     * [GestureDetectorCompat] instance to detect the gestures like swiping, long clicks and single
     * taps.
     */
    private var gestureDetector: GestureDetectorCompat

    private var underlayListener: UnderlayListener? = null
    private var onMainClickListener: OnMainClickListener? = null

    /**
     * [ViewDragHelper.Callback] implementation to define the behavior on capturing the dragging
     * of the view.
     */
    private val viewDragHelperCallback = object : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return if (!isDragLocked && child == mainView) {
                dragHelper.captureChildView(child, pointerId)
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

    /**
     * [GestureDetector.OnGestureListener] implementation to define the behavior when related
     * gestures are detected.
     */
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
            val isUnderlayHidden = mainView.right == rectMainNotDragged.right
            val didHitMainView = e.x >= mainView.left && e.x <= mainView.right &&
                e.y >= mainView.top && e.y <= mainView.bottom
            return if (didHitMainView) {
                if (isUnderlayHidden) {
                    onMainClickListener?.onClick()
                } else {
                    collapseUnderlay()
                }
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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        fixElevationOverlay()
    }

    private fun fixElevationOverlay() {
        val overlayProvider = ElevationOverlayProvider(context)
        if (!overlayProvider.isThemeElevationOverlayEnabled) return

        val elevation = overlayProvider.getParentAbsoluteElevation(this) - ELEVATION_CORRECTION
        val elevatedMainViewColor = overlayProvider.compositeOverlayWithThemeSurfaceColorIfNeeded(elevation)
        mainView.setBackgroundColor(elevatedMainViewColor)
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

    /**
     * Set drag locked state of this view.
     *
     * @param isDragLocked True if the dragging is disabled, false otherwise.
     */
    fun setDragLocked(isDragLocked: Boolean) {
        this.isDragLocked = isDragLocked
    }

    /**
     * Set [UnderlayListener] that will receive the notifications every time [underlayView] gets
     * expanded.
     *
     * @param underlayListener the underlay listener
     */
    fun setUnderlayListener(underlayListener: UnderlayListener) {
        this.underlayListener = underlayListener
    }

    /**
     * Remove [UnderlayListener] that's been set using [setUnderlayListener].
     */
    fun removeUnderlayListener() {
        this.underlayListener = null
    }

    /**
     * Register a listener to be invoked when [mainView] is clicked.
     *
     * @param onMainClickListener the click listener
     */
    fun setOnMainClickListener(onMainClickListener: OnMainClickListener) {
        this.onMainClickListener = onMainClickListener
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
        underlayListener?.onUnderlayExpanded(this)
    }

    /**
     * Collapse the [underlayView] with animation.
     */
    fun collapseUnderlay() {
        dragHelper.smoothSlideViewTo(mainView, rectMainNotDragged.left, rectMainNotDragged.top)
        ViewCompat.postInvalidateOnAnimation(this)
    }

    /**
     * A callback interface that gets triggered every time [underlayView] gets expanded.
     */
    fun interface UnderlayListener {
        /**
         * @param view Root view containing the [underlayView] that's being expanded.
         */
        fun onUnderlayExpanded(view: AdyenSwipeToRevealLayout)
    }

    /**
     * A callback that gets triggered every time [mainView] gets clicked if underlay is hidden.
     */
    fun interface OnMainClickListener {
        fun onClick()
    }
}
