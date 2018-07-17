package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 11/08/2017.
 */
public final class KeyboardUtil {
    public static boolean show(@NonNull View view) {
        return show(view, null);
    }

    public static void showAndSelect(@NonNull final EditText editText) {
        show(editText, new Runnable() {
            @Override
            public void run() {
                editText.setSelection(0, editText.getText().length());
            }
        });
    }

    public static void hide(@NonNull final View view) {
        final InputMethodManager inputMethodService = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isHidden = inputMethodService.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        if (!isHidden) {
            view.post(new Runnable() {
                @Override
                public void run() {
                    inputMethodService.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });
        }
    }

    private static boolean show(@NonNull final View view, @Nullable final Runnable onShownRunnable) {
        view.requestFocus();
        final InputMethodManager inputMethodService = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isShown = inputMethodService.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        if (!isShown) {
            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    show(view, onShownRunnable);
                }
            }, 100);
        } else if (onShownRunnable != null) {
            onShownRunnable.run();
        }

        return isShown;
    }

    private KeyboardUtil() {
        throw new IllegalStateException("No instances.");
    }
}
