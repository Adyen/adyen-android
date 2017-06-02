package com.adyen.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adyen.ui.R;

/**
 * Fragment to hold a loading animation during network requests.
 */

public class LoadingScreenFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.loading_screen_fragment, container, false);
        return fragmentView;
    }
}
