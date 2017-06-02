package com.adyen.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import com.adyen.ui.utils.IconUtil;

public class AdyenIconImageView extends AppCompatImageView {

    public AdyenIconImageView(Context context) {
        super(context);
    }

    public AdyenIconImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AdyenIconImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageDrawable(IconUtil.resizeRoundCornersAndAddBorder(getContext(), bm, this.getHeight()));
    }

}
