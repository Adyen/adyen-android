package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.EditText;

import com.adyen.checkout.ui.internal.common.util.recyclerview.SimpleDiffCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 15/08/2017.
 */
public abstract class CountryAdapter extends RecyclerView.Adapter<IbanSuggestionViewHolder> {
    private List<Suggestion> mSuggestions = new ArrayList<>();

    @Override
    public IbanSuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return IbanSuggestionViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(IbanSuggestionViewHolder holder, int position) {
        holder.getTextView().setText(mSuggestions.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    @NonNull
    protected List<Suggestion> getSuggestions() {
        return mSuggestions;
    }

    protected void onInputChanged(@NonNull EditText editText, @NonNull RecyclerView.Adapter targetAdapter) {
        String normalizedIban = editText.getText().toString().replaceAll("\\s", "").toUpperCase(Locale.US);
        List<Suggestion> newSuggestions = createSuggestions(editText, normalizedIban);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new SimpleDiffCallback<>(mSuggestions, newSuggestions));
        mSuggestions = newSuggestions;
        diffResult.dispatchUpdatesTo(targetAdapter);
    }

    @NonNull
    protected abstract List<Suggestion> createSuggestions(@NonNull EditText editText, @NonNull String normalizedIban);
}
