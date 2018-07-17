package com.adyen.checkout.ui.internal.sepadirectdebit;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.adyen.checkout.util.internal.SimpleTextWatcher;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 15/08/2017.
 */
public class IbanSuggestionsAdapter extends RecyclerView.Adapter<IbanSuggestionViewHolder> {
    private final Map<String, CountryAdapter> mAdapters = new HashMap<String, CountryAdapter>() {
        {
            put("NL", new NetherlandsAdapter());
            put("DE", new GermanyAdapter());
        }
    };

    private SimpleTextWatcher mIbanTextWatcher = new SimpleTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            String normalizedInput = s.toString().replaceAll("\\s", "").toUpperCase(Locale.US);
            String countryCode = normalizedInput.length() >= 2 ? normalizedInput.substring(0, 2) : null;
            CountryAdapter newAdapter = mAdapters.get(countryCode);
            boolean adapterChanged = false;

            if (newAdapter != mCurrentCountryAdapter) {
                mCurrentCountryAdapter = newAdapter;
                adapterChanged = true;
            }

            if (adapterChanged) {
                notifyDataSetChanged();
            } else if (mCurrentCountryAdapter != null) {
                mCurrentCountryAdapter.onInputChanged(mIbanEditText, IbanSuggestionsAdapter.this);
            }
        }
    };

    private CountryAdapter mCurrentCountryAdapter;

    private EditText mIbanEditText;

    private Listener mListener;

    public IbanSuggestionsAdapter(@NonNull EditText ibanEditText, @NonNull Listener listener) {
        mIbanEditText = ibanEditText;
        mIbanEditText.addTextChangedListener(mIbanTextWatcher);
        mListener = listener;
    }

    @Override
    public IbanSuggestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final IbanSuggestionViewHolder viewHolder = mCurrentCountryAdapter.onCreateViewHolder(parent, viewType);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = viewHolder.getAdapterPosition();
                Suggestion suggestion = position != RecyclerView.NO_POSITION ? mCurrentCountryAdapter.getSuggestions().get(position) : null;

                if (suggestion != null) {
                    mListener.onSuggestionClick(suggestion);
                }
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(IbanSuggestionViewHolder holder, int position) {
        mCurrentCountryAdapter.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return mCurrentCountryAdapter != null ? mCurrentCountryAdapter.getItemCount() : 0;
    }

    public interface Listener {
        void onSuggestionClick(@NonNull Suggestion suggestion);
    }
}
