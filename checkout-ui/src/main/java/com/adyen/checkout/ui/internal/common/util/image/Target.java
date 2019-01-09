/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/05/2018.
 */

package com.adyen.checkout.ui.internal.common.util.image;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface Target {
    void setImageDrawable(@Nullable Drawable drawable);

    final class ImageView implements Target {
        private final android.widget.ImageView mImageView;

        public ImageView(@NonNull android.widget.ImageView imageView) {
            mImageView = imageView;
        }

        @Override
        public void setImageDrawable(@Nullable Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }
    }
}
