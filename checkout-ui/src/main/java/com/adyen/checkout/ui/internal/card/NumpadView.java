/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 12/03/2018.
 */

package com.adyen.checkout.ui.internal.card;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.adyen.checkout.ui.R;

public class NumpadView extends LinearLayout {
    private KeyListener mKeyListener;

    public NumpadView(@NonNull Context context) {
        this(context, null);
    }

    public NumpadView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumpadView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.view_numpad, this);
        setBackgroundColor(ContextCompat.getColor(context, R.color.numpad_background));
        setOrientation(VERTICAL);

        findViewById(R.id.button1).setOnClickListener(new CharNotifier('1'));
        findViewById(R.id.button2).setOnClickListener(new CharNotifier('2'));
        findViewById(R.id.button3).setOnClickListener(new CharNotifier('3'));
        findViewById(R.id.button4).setOnClickListener(new CharNotifier('4'));
        findViewById(R.id.button5).setOnClickListener(new CharNotifier('5'));
        findViewById(R.id.button6).setOnClickListener(new CharNotifier('6'));
        findViewById(R.id.button7).setOnClickListener(new CharNotifier('7'));
        findViewById(R.id.button8).setOnClickListener(new CharNotifier('8'));
        findViewById(R.id.button9).setOnClickListener(new CharNotifier('9'));
        findViewById(R.id.button0).setOnClickListener(new CharNotifier('0'));

        findViewById(R.id.button_backspace).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyBackspaceClicked();
            }
        });
    }

    public void setKeyListener(@Nullable KeyListener keyListener) {
        mKeyListener = keyListener;
    }

    private void notifyCharClicked(char digit) {
        if (mKeyListener != null) {
            mKeyListener.onCharClicked(digit);
        }
    }

    private void notifyBackspaceClicked() {
        if (mKeyListener != null) {
            mKeyListener.onBackspace();
        }
    }

    public interface KeyListener {
        void onCharClicked(char character);

        void onBackspace();
    }

    private final class CharNotifier implements View.OnClickListener {
        private final char mChar;

        private CharNotifier(char charToNotify) {
            mChar = charToNotify;
        }

        @Override
        public void onClick(View v) {
            notifyCharClicked(mChar);
        }
    }
}
