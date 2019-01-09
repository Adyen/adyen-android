/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 15/08/2017.
 */

package com.adyen.checkout.ui.internal.common.util;

import android.animation.Animator;
import android.support.annotation.NonNull;

public class SimpleAnimatorListener implements Animator.AnimatorListener {
    @Override
    public void onAnimationStart(@NonNull Animator animator) {
        // Subclasses may override.
    }

    @Override
    public void onAnimationEnd(@NonNull Animator animator) {
        // Subclasses may override.
    }

    @Override
    public void onAnimationCancel(@NonNull Animator animator) {
        // Subclasses may override.
    }

    @Override
    public void onAnimationRepeat(@NonNull Animator animator) {
        // Subclasses may override.
    }
}
