/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 28/5/2019.
 */

package com.adyen.checkout.card;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adyen.checkout.base.api.ImageLoader;
import com.adyen.checkout.base.ui.view.RoundCornerImageView;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.card.ui.R;

import java.util.Collections;
import java.util.List;

public class CardListAdapter extends RecyclerView.Adapter<CardListAdapter.ImageViewHolder> {

    private final List<CardType> mSupportedCards;
    private List<CardType> mFilteredCards = Collections.emptyList();
    private final ImageLoader mImageLoader;

    private static final float ACTIVE = 1f;
    private static final float NOT_ACTIVE = 0.2f;

    CardListAdapter(ImageLoader imageLoader, List<CardType> supportedCards) {
        mImageLoader = imageLoader;
        mSupportedCards = supportedCards;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        final RoundCornerImageView imageView =
                (RoundCornerImageView) LayoutInflater.from(parent.getContext()).inflate(R.layout.brand_logo, parent, false);

        return new ImageViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        final CardType card = mSupportedCards.get(i);
        imageViewHolder.mCardLogo.setAlpha(mFilteredCards.isEmpty() || mFilteredCards.contains(card) ? ACTIVE : NOT_ACTIVE);
        mImageLoader.load(card.getTxVariant(), imageViewHolder.mCardLogo);
    }

    @Override
    public int getItemCount() {
        return mSupportedCards.size();
    }


    void setFilteredCard(@NonNull List<CardType> filteredCards) {
        this.mFilteredCards = filteredCards;
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
