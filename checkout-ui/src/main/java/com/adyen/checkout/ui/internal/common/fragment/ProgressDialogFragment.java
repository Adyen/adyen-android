/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/08/2017.
 */

package com.adyen.checkout.ui.internal.common.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.adyen.checkout.ui.R;
import com.adyen.checkout.ui.internal.common.util.ThemeUtil;

import java.lang.ref.WeakReference;

public class ProgressDialogFragment extends AppCompatDialogFragment {
    private static final String TAG = ProgressDialogFragment.class.getSimpleName();

    private static final int SHOW_DELAY = 250;

    private static final int MINIMUM_SHOW_DURATION = 500;

    private long mShowTimestamp;

    public static void show(@NonNull AppCompatActivity activity) {
        show(activity.getSupportFragmentManager());
    }

    public static void show(@NonNull Fragment fragment) {
        show(fragment.getChildFragmentManager());
    }

    private static void show(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        fragmentManager.executePendingTransactions();

        Fragment fragment = fragmentManager.findFragmentByTag(TAG);

        if (!(fragment instanceof ProgressDialogFragment)) {
            fragment = new ProgressDialogFragment();
            fragmentManager
                    .beginTransaction()
                    .add(fragment, TAG)
                    // ProgressDialogFragment doesn't have state.
                    .commitAllowingStateLoss();
        }
    }

    public static void hide(@NonNull AppCompatActivity activity) {
        hide(activity.getSupportFragmentManager());
    }

    public static void hide(@NonNull Fragment fragment) {
        hide(fragment.getChildFragmentManager());
    }

    private static void hide(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.isDestroyed()) {
            return;
        }

        fragmentManager.executePendingTransactions();

        Fragment fragment = fragmentManager.findFragmentByTag(TAG);

        if (fragment instanceof ProgressDialogFragment) {
            long hideDelayMillis = ((ProgressDialogFragment) fragment).getHideDelayMillis();
            Runnable hideRunnable = getHideRunnable(new WeakReference<>(fragmentManager), new WeakReference<>(fragment));

            if (hideDelayMillis > 0) {
                new Handler(Looper.getMainLooper()).postDelayed(hideRunnable, hideDelayMillis);
            } else {
                hideRunnable.run();
            }
        }
    }

    @NonNull
    private static Runnable getHideRunnable(
            @NonNull final WeakReference<FragmentManager> managerRef,
            @NonNull final WeakReference<Fragment> fragmentRef
    ) {
        return new Runnable() {
            @Override
            public void run() {
                FragmentManager fragmentManager = managerRef.get();
                Fragment fragment = fragmentRef.get();

                if (fragment != null) {
                    if (fragmentManager == null) {
                        fragmentManager = fragment.getFragmentManager();
                    }

                    if (fragmentManager == null) {
                        throw new RuntimeException();
                    }

                    fragmentManager
                            .beginTransaction()
                            .remove(fragment)
                            // ProgressDialogFragment doesn't have state.
                            .commitAllowingStateLoss();
                }
            }
        };
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        final View decorView;

        if (window != null) {
            window.setBackgroundDrawableResource(R.color.transparent);
            window.setWindowAnimations(R.style.ProgressDialogFragment_Window);
            decorView = window.getDecorView();
        } else {
            decorView = null;
        }

        Context context = inflater.getContext();

        // ProgressBar
        int size = getResources().getDimensionPixelSize(R.dimen.progress_dialog_size);
        LinearLayout.LayoutParams progressBarLayoutParams = new LinearLayout.LayoutParams(size, size);
        progressBarLayoutParams.gravity = Gravity.CENTER;

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setLayoutParams(progressBarLayoutParams);
        progressBar.setIndeterminate(true);

        if (decorView != null) {
            mShowTimestamp = System.currentTimeMillis() + SHOW_DELAY;
            decorView.setVisibility(View.GONE);
            decorView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    decorView.setVisibility(View.VISIBLE);
                }
            }, SHOW_DELAY);
        }

        ThemeUtil.applyPrimaryThemeColor(context, progressBar.getProgressDrawable(), progressBar.getIndeterminateDrawable());

        return progressBar;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        return dialog;
    }

    private long getHideDelayMillis() {
        long currentTime = System.currentTimeMillis();

        if (mShowTimestamp <= currentTime) {
            // We are visible, ensure we are visible to the user for a minimum amount of time.
            return mShowTimestamp + MINIMUM_SHOW_DURATION - currentTime;
        } else {
            // We are not visible (yet), hide immediately.
            return 0;
        }
    }
}
