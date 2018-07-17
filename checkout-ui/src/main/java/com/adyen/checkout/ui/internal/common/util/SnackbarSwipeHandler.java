package com.adyen.checkout.ui.internal.common.util;

import android.animation.Animator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 17/08/2017.
 */
public final class SnackbarSwipeHandler extends GestureDetector.SimpleOnGestureListener implements View.OnTouchListener {
    private static final int SWIPE_MIN_DISTANCE_HORIZONTAL = 120;

    private static final int SWIPE_MIN_DISTANCE_VERTICAL = 50;

    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private static final int DURATION_FACTOR_HORIZONTAL = 100;

    private GestureDetectorCompat mGestureDetector;

    private Snackbar mSnackbar;

    private SimpleAnimatorListener mAnimatorListener = new SimpleAnimatorListener() {
        @Override
        public void onAnimationEnd(Animator animator) {
            mSnackbar.dismiss();
        }
    };

    public static void attach(@NonNull Context context, @NonNull Snackbar snackbar) {
        snackbar.getView().setOnTouchListener(new SnackbarSwipeHandler(context, snackbar));
    }

    private SnackbarSwipeHandler(@NonNull Context context, @NonNull Snackbar snackbar) {
        mGestureDetector = new GestureDetectorCompat(context, this);
        mSnackbar = snackbar;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE_HORIZONTAL && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            onSwipeDismissX(velocityX, -1);

            return true; // Right to left
        } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE_HORIZONTAL && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
            onSwipeDismissX(velocityX, 1);

            return true; // Left to right
        } else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE_VERTICAL && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            mSnackbar.dismiss();

            return true; // Top to bottom
        }

        return false;
    }

    private void onSwipeDismissX(float velocityX, int factor) {
        View view = mSnackbar.getView();
        int width = view.getWidth();
        view
                .animate()
                .translationX(factor * width)
                .setDuration(Math.abs(DURATION_FACTOR_HORIZONTAL * (long) (velocityX / width)))
                .setListener(mAnimatorListener)
                .start();
    }
}
