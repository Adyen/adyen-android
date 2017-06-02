package com.adyen.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.adyen.ui.fragments.LoadingScreenFragment;

/**
 * Activity that holds a loading animation and is transparent over the activity it is started from.
 */

public class TranslucentLoadingScreenActivity extends FragmentActivity {

    private static final String TAG = TranslucentLoadingScreenActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final LoadingScreenFragment loadingScreenFragment = new LoadingScreenFragment();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(android.R.id.content, loadingScreenFragment);
        ft.commit();
    }

}
