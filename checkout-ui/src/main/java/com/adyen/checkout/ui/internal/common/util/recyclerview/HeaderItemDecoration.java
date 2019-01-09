/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 21/08/2017.
 */

package com.adyen.checkout.ui.internal.common.util.recyclerview;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class HeaderItemDecoration extends RecyclerView.ItemDecoration {
    private View mHeader;

    private Callback mCallback;

    public HeaderItemDecoration(@NonNull View header, @Nullable Callback callback) {
        mHeader = header;

        if (mHeader.getMeasuredHeight() == 0) {
            mHeader.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
        }

        mCallback = callback;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(child);

            if (mCallback == null || mCallback.isHeaderPosition(position)) {
                int measuredHeight = mHeader.getMeasuredHeight();
                mHeader.layout(parent.getLeft(), 0, parent.getRight(), measuredHeight);
                c.save();
                c.translate(0, child.getTop() + child.getTranslationY() - measuredHeight);
                mHeader.draw(c);
                c.restore();
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);

        if (mCallback == null || mCallback.isHeaderPosition(position)) {
            outRect.set(
                    view.getPaddingLeft(),
                    mHeader.getMeasuredHeight(),
                    view.getPaddingRight(),
                    view.getPaddingBottom()
            );
        }
    }

    public interface Callback {
        boolean isHeaderPosition(int position);
    }
}
