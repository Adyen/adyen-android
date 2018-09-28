package com.adyen.checkout.ui.internal.card;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.Animatable2Compat;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.support.v7.content.res.AppCompatResources;
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
// TODO: 25/06/2018 Fix animation on Build.VERSION.SDK_INT <= Build.VERSION_CODES.M.
public class NfcCardReaderTutorialFragment extends AppCompatDialogFragment {
    public static final String TAG = NfcCardReaderTutorialFragment.class.getSimpleName();

    private final AnimationCallbackDelegate mAnimationCallbackDelegate = new AnimationCallbackDelegate();

    private final ReverseAnimationCallbackDelegate mReverseAnimationCallbackDelegate = new ReverseAnimationCallbackDelegate();

    private Button mDismissButton;

    private View mEnableNfcButton;

    private ImageView mTutorialAnimImageView;

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

        mTutorialAnimImageView = view.findViewById(R.id.imageView_tutorialAnim);

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        startAnimation();
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
    public void onDestroyView() {
        super.onDestroyView();

        Drawable drawable = mTutorialAnimImageView.getDrawable();

        if (drawable instanceof Animatable2Compat) {
            ((Animatable2Compat) drawable).clearAnimationCallbacks();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && drawable instanceof Animatable2) {
            ((Animatable2) drawable).clearAnimationCallbacks();
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

    private void startAnimation() {
        Context context = getContext();

        if (context == null) {
            return;
        }

        Drawable tutorialAnimDrawable = AppCompatResources.getDrawable(context, R.drawable.card_reader_tutorial_animation);
        mTutorialAnimImageView.setImageDrawable(tutorialAnimDrawable);
        //noinspection ConstantConditions
        mAnimationCallbackDelegate.register(tutorialAnimDrawable);
        ((Animatable) tutorialAnimDrawable).start();
    }

    private void startReverseAnimation() {
        Context context = getContext();

        if (context == null) {
            return;
        }

        Drawable tutorialAnimDrawableReversed = AppCompatResources.getDrawable(context, R.drawable.card_reader_tutorial_animation_reverse);
        mTutorialAnimImageView.setImageDrawable(tutorialAnimDrawableReversed);
        //noinspection ConstantConditions
        mReverseAnimationCallbackDelegate.register(tutorialAnimDrawableReversed);
        ((Animatable) tutorialAnimDrawableReversed).start();
    }

    private void registerAnimationCallback(
            @NonNull Drawable drawable,
            @NonNull Animatable2Compat.AnimationCallback compatCallback,
            @NonNull Animatable2.AnimationCallback callback
    ) {
        if (drawable instanceof Animatable2Compat) {
            ((Animatable2Compat) drawable).registerAnimationCallback(compatCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && drawable instanceof Animatable2) {
            ((Animatable2) drawable).registerAnimationCallback(callback);
        } else {
            throw new RuntimeException("Invalid drawable.");
        }
    }

    private final class AnimationCallbackDelegate {
        private final Animatable2.AnimationCallback mAnimationCallback;

        private final Animatable2Compat.AnimationCallback mAnimationCallbackCompat = new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                startReverseAnimation();
            }
        };

        private AnimationCallbackDelegate() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAnimationCallback = new Animatable2.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        startReverseAnimation();
                    }
                };
            } else {
                mAnimationCallback = null;
            }
        }

        private void register(@NonNull Drawable drawable) {
            registerAnimationCallback(drawable, mAnimationCallbackCompat, mAnimationCallback);
        }
    }

    private final class ReverseAnimationCallbackDelegate {
        private final Animatable2.AnimationCallback mAnimationCallback;

        private final Animatable2Compat.AnimationCallback mAnimationCallbackCompat = new Animatable2Compat.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                startAnimation();
            }
        };

        private ReverseAnimationCallbackDelegate() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mAnimationCallback = new Animatable2.AnimationCallback() {
                    @Override
                    public void onAnimationEnd(Drawable drawable) {
                        startAnimation();
                    }
                };
            } else {
                mAnimationCallback = null;
            }
        }

        private void register(@NonNull Drawable drawable) {
            registerAnimationCallback(drawable, mAnimationCallbackCompat, mAnimationCallback);
        }
    }
}
