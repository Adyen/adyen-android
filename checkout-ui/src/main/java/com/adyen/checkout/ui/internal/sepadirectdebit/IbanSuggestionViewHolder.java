package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 15/08/2017.
 */
public final class IbanSuggestionViewHolder extends RecyclerView.ViewHolder {
    private TextView mTextView;

    @NonNull
    public static IbanSuggestionViewHolder create(@NonNull ViewGroup parent) {
        View itemView = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.item_iban_suggestion, parent, false);

        return new IbanSuggestionViewHolder(itemView);
    }

    private IbanSuggestionViewHolder(@NonNull View itemView) {
        super(itemView);

        mTextView = itemView.findViewById(R.id.textView_ibanSuggestion);
        ThemeUtil.applyPrimaryThemeColor(itemView.getContext(), mTextView.getBackground());
    }

    @NonNull
    public TextView getTextView() {
        return mTextView;
    }
}
