package com.adyen.checkout.ui.internal.common.util.recyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 21/08/2017.
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    private Callback mCallback;

    private final Rect mBounds = new Rect();

    public DividerItemDecoration(@NonNull Context context, @Nullable Callback callback) {
        TypedArray a = context.obtainStyledAttributes(new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
        mCallback = callback;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (parent.getLayoutManager() == null) {
            return;
        }

        c.save();
        int left;
        int right;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && parent.getClipToPadding()) {
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
            c.clipRect(left, parent.getPaddingTop(), right, parent.getHeight() - parent.getPaddingBottom());
        } else {
            left = 0;
            right = parent.getWidth();
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (isDividerPosition(parent.getChildAdapterPosition(child))) {
                parent.getDecoratedBoundsWithMargins(child, mBounds);
                int bottom = mBounds.bottom + Math.round(child.getTranslationY());
                int top = bottom - mDivider.getIntrinsicHeight();
                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (isDividerPosition(parent.getChildAdapterPosition(view))) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        }
    }

    private boolean isDividerPosition(int position) {
        return mCallback == null || mCallback.isDividerPosition(position);
    }

    public interface Callback {
        boolean isDividerPosition(int position);
    }
}
