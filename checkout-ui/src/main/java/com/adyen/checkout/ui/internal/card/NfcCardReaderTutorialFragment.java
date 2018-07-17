package com.adyen.checkout.ui.internal.card;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.adyen.checkout.ui.R;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by ran on 19/04/2017.
 */
// TODO: 25/06/2018 Fix animation on Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP.
public class NfcCardReaderTutorialFragment extends AppCompatDialogFragment {
    public static final String TAG = NfcCardReaderTutorialFragment.class.getSimpleName();

    private Button mDismissButton;

    private View mEnableNfcButton;

    private ImageView mTutorialAnimImageView;

    private AnimatedVectorDrawableCompat mTutorialAnimDrawable;

    private AnimatedVectorDrawableCompat mTutorialAnimDrawableReversed;

    private Animatable2Compat.AnimationCallback mAnimationCallback = new Animatable2Compat.AnimationCallback() {
        @Override
        public void onAnimationEnd(Drawable drawable) {
            mTutorialAnimImageView.setImageDrawable(mTutorialAnimDrawableReversed);
            mTutorialAnimDrawableReversed.start();
        }
    };

    private Animatable2Compat.AnimationCallback mReverseAnimationCallback = new Animatable2Compat.AnimationCallback() {
        @Override
        public void onAnimationEnd(Drawable drawable) {
            mTutorialAnimImageView.setImageDrawable(mTutorialAnimDrawable);
            mTutorialAnimDrawable.start();
        }
    };

    private Listener mListener;

    interface Listener {
        boolean isNfcEnabledOnDevice();
    }

    @NonNull
    public static NfcCardReaderTutorialFragment newInstance() {
        return new NfcCardReaderTutorialFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof Listener) {
            mListener = (Listener) context;
        } else {
            throw new IllegalStateException(context.getClass().getName() + " must implement " + Listener.class.getName());
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppCompatDialog dialog = (AppCompatDialog) super.onCreateDialog(savedInstanceState);
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nfc_card_reader_tutorial, container, false);

        mTutorialAnimDrawable = AnimatedVectorDrawableCompat.create(inflater.getContext(), R.drawable.card_reader_tutorial_animation);
        //noinspection ConstantConditions
        mTutorialAnimDrawable.registerAnimationCallback(mAnimationCallback);

        mTutorialAnimDrawableReversed = AnimatedVectorDrawableCompat.create(inflater.getContext(), R.drawable.card_reader_tutorial_animation_reverse);
        //noinspection ConstantConditions
        mTutorialAnimDrawableReversed.registerAnimationCallback(mReverseAnimationCallback);

        mTutorialAnimImageView = view.findViewById(R.id.imageView_tutorialAnim);
        mTutorialAnimImageView.setImageDrawable(mTutorialAnimDrawable);

        mDismissButton = view.findViewById(R.id.button_dismiss);
        mDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissAllowingStateLoss();
            }
        });

        mEnableNfcButton = view.findViewById(R.id.button_enableNfc);
        mEnableNfcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEnableNfc(v.getContext());
            }
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mTutorialAnimDrawable != null && !mTutorialAnimDrawable.isRunning() && !mTutorialAnimDrawableReversed.isRunning()) {
            mTutorialAnimDrawable.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mListener.isNfcEnabledOnDevice()) {
            mDismissButton.setText(R.string.checkout_ok);
            mEnableNfcButton.setVisibility(View.GONE);
        } else {
            mDismissButton.setText(R.string.checkout_skip);
            mEnableNfcButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mTutorialAnimDrawable != null && mTutorialAnimDrawable.isRunning()) {
            mTutorialAnimDrawable.stop();
        }

        if (mReverseAnimationCallback != null && mTutorialAnimDrawableReversed.isRunning()) {
            mTutorialAnimDrawableReversed.stop();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    private void onEnableNfc(@NonNull Context context) {
        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), R.string.checkout_nfc_settings_redirect_failed_toast, Toast.LENGTH_LONG).show();
        }
    }
}
