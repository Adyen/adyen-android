/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */

package com.adyen.checkout.card;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.card.model.CardType;
import com.adyen.checkout.card.ui.R;

import java.util.Collections;
import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ImageViewHolder> {

    private List<CardType> mCards = Collections.emptyList();
    private final ImageLoader mImageLoader;

    CardListAdapter(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        final Resources resources = parent.getResources();
        final int width = resources.getDimensionPixelSize(R.dimen.payment_method_logo_width);
        final int height = resources.getDimensionPixelSize(R.dimen.payment_method_logo_height);
        final int leftMargin = resources.getDimensionPixelSize(R.dimen.standard_quarter_margin);
        final RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(width, height);
        layoutParams.leftMargin = leftMargin;

        final RoundCornerImageView imageView = new RoundCornerImageView(parent.getContext());
        imageView.setStrokeColor(ContextCompat.getColor(parent.getContext(), R.color.stroke_color));
        imageView.setLayoutParams(layoutParams);

        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        mImageLoader.load(mCards.get(i).getTxVariant(), imageViewHolder.mCardLogo);
    }

    @Override
    public int getItemCount() {
        return mCards.size();
    }


    void setCards(@NonNull List<CardType> cards) {
        this.mCards = cards;
        notifyDataSetChanged();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        RoundCornerImageView mCardLogo;

        ImageViewHolder(View view) {
            super(view);
            mCardLogo = (RoundCornerImageView) view;
        }
    }
}
