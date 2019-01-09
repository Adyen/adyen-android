/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 03/10/2017.
 */

package com.adyen.checkout.ui.internal.common.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.adyen.checkout.ui.R;

import java.net.UnknownHostException;

public class ErrorDialogFragment extends AppCompatDialogFragment {
    @NonNull
    public static final String TAG = ErrorDialogFragment.class.getName() + ".TAG";

    private static final String ARG_TITLE = "ARG_TITLE";

    private static final String ARG_MESSAGE = "ARG_MESSAGE";

    private TextView mTitleTextView;

    private TextView mMessageTextView;

    private Button mDismissButton;

    @NonNull
    public static ErrorDialogFragment newInstance(@NonNull Context context, @NonNull Throwable error) {
        return newInstance(null, getMessageForThrowable(context, error));
    }

    @NonNull
    public static ErrorDialogFragment newInstance(@Nullable String title, @Nullable String message) {
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_MESSAGE, message);
        ErrorDialogFragment fragment = new ErrorDialogFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    private static String getMessageForThrowable(@NonNull Context context, @NonNull Throwable error) {
        StringBuilder messageBuilder = new StringBuilder();

        if (error instanceof UnknownHostException) {
            messageBuilder.append(context.getString(R.string.checkout_error_message_default));
            messageBuilder.append("\n\n");
            messageBuilder.append(context.getString(R.string.checkout_error_message_network_hint));
        } else {
            messageBuilder.append(context.getString(R.string.checkout_error_message_default));

            String message = error.getLocalizedMessage();

            if (TextUtils.isEmpty(message)) {
                message = error.getMessage();
            }

            if (!TextUtils.isEmpty(message)) {
                messageBuilder.append("\n\n");
                messageBuilder.append(message);
            }
        }

        return messageBuilder.toString();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        AppCompatDialog dialog = (AppCompatDialog) super.onCreateDialog(savedInstanceState);
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_error, container);

        mTitleTextView = view.findViewById(R.id.textView_title);
        mMessageTextView = view.findViewById(R.id.textView_message);
        mDismissButton = view.findViewById(R.id.button_dismiss);

        Bundle arguments = getArguments();

        //noinspection ConstantConditions
        String title = arguments.getString(ARG_TITLE);
        String message = arguments.getString(ARG_MESSAGE);

        mTitleTextView.setText(TextUtils.isEmpty(title) ? getString(R.string.checkout_error_dialog_title) : title);
        mMessageTextView.setText(TextUtils.isEmpty(message) ? getString(R.string.checkout_error_message_default) : message);
        mDismissButton.setText(R.string.checkout_ok);
        mDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public void showIfNotShown(@NonNull FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null) {
            show(fragmentManager, TAG);
        }
    }
}
