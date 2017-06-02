package com.adyen.ui.views.loadinganimation;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.HashMap;

public class ThreeDotsLoadingAnimation extends Animation {

    private static final int ALPHA_FULL = 255; //no transparency
    private static final int ALPHA_TRANSPARENT = 60; /* Should be the same transparency used for color
    light_green_disabled */

    private static final int DURATION = 900; //Duration of one cycle of the animation
    private static final int ONE_THIRD_DURATION = ((int) (1.0f / 3.0f * DURATION));
    private static final int TWO_THIRD_DURATION = ((int) (2.0f / 3.0f * DURATION));

    private int[] alphas = new int[] {ALPHA_TRANSPARENT, ALPHA_TRANSPARENT, ALPHA_FULL};

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float circleSpacing = 14; //spacing between circles
        float radius = (getWidth() - circleSpacing * 2) / 6;
        float x = getWidth() / (float) 2 - (radius * 2 + circleSpacing);
        float y = getHeight() / (float) 2;
        for (int i = 0; i < 3; i++) {
            canvas.save();
            float translateX = x + (radius * 2) * i + circleSpacing * i;
            canvas.translate(translateX, y);
            paint.setAlpha(alphas[i]);
            canvas.drawCircle(0, 0, radius, paint);
            canvas.restore();
        }
    }

    @Override
    public HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> onCreateAnimators() {
        HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> animators = new HashMap<>();
        final int[] delays = new int[] {ONE_THIRD_DURATION, TWO_THIRD_DURATION, 0};
        for (int i = 0; i < 3; i++) {
            final int index = i;

            ValueAnimator alphaAnim = ValueAnimator.ofInt(ALPHA_FULL, ALPHA_TRANSPARENT, ALPHA_TRANSPARENT);
            alphaAnim.setDuration(DURATION);
            alphaAnim.setRepeatCount(-1);
            alphaAnim.setStartDelay(delays[i]);
            ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    alphas[index] = (int) animation.getAnimatedValue();
                    invalidateSelf();
                }
            };
            animators.put(alphaAnim, updateListener);
        }
        return animators;
    }


}
