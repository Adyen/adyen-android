package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.adyen.ui.R;
import com.adyen.ui.utils.AdyenInputValidator;

import java.util.ArrayList;
import java.util.Calendar;


public class ExpiryDateEditText extends CheckoutEditText {

    private static final String TWENTY = "20";
    private static final int EDIT_TEXT_MAX_LENGTH = 4 + 1;
    private static final int MAX_EXPIRED_MONTHS = 3; //max number of months that creditcard can be expired

    private AdyenInputValidator validator;

    private void init() {
        ArrayList<InputFilter> dateFilters = new ArrayList<>();
        dateFilters.add(new InputFilter.LengthFilter(EDIT_TEXT_MAX_LENGTH));
        this.setFilters(dateFilters.toArray(new InputFilter[dateFilters.size()]));
        this.addTextChangedListener(new ExpiryDateFormatWatcher());

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ExpiryDateEditText.this.setTextColor(ContextCompat.getColor(getContext(), R.color.black_text));
                } else {
                    if (!isInputDateValid(ExpiryDateEditText.this.getText().toString())) {
                        ExpiryDateEditText.this.setTextColor(ContextCompat.getColor(getContext(),
                                R.color.red_invalid_input_highlight));
                    }
                }
            }
        });
    }

    public ExpiryDateEditText(Context context) {
        super(context);
        init();
    }

    public ExpiryDateEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ExpiryDateEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ExpiryDateEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setValidator(AdyenInputValidator validator) {
        this.validator = validator;
        this.validator.addInputField(ExpiryDateEditText.this);
    }

    public String getMonth() {
        return this.getText().toString().substring(0, 2);
    }

    public String getFullYear() {
        return TWENTY + this.getText().toString().substring(3, 5);
    }

    class ExpiryDateFormatWatcher implements TextWatcher {
        private final char separatorChar = '/';

        private final String separatorString = String.valueOf(separatorChar);

        private boolean deleted;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Nothing to do.
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            deleted = count == 0;
        }

        @Override
        public void afterTextChanged(Editable s) {
            removeTextChangedListener(this);

            if (s.length() == 1 && s.charAt(0) > '1') {
                s.insert(0, "0");
            }

            if (s.length() == 2 && !deleted) {
                if (s.toString().matches("\\d/")) {
                    s.insert(0, "0");
                } else if (!s.toString().contains(separatorString)) {
                    s.append(separatorChar);
                }
            }

            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);

                if (i == 2) {
                    if (c != separatorChar) {
                        if (!Character.isDigit(c)) {
                            s.replace(i, i + 1, separatorString);
                        } else {
                            s.insert(i, separatorString);

                            if (deleted) {
                                int selectionStart = getSelectionStart();
                                int selectionEnd = getSelectionEnd();
                                int newSelectionStart = selectionStart - 1 == i ? selectionStart - 1 : selectionStart;
                                int newSelectionEnd = selectionEnd - 1 == i ? selectionEnd - 1 : selectionEnd;
                                setSelection(newSelectionStart, newSelectionEnd);
                            }
                        }
                    }
                } else {
                    if (!Character.isDigit(c)) {
                        s.delete(i, i + 1);
                    }
                }
            }

            boolean inputDateValid = isInputDateValid(s.toString());

            if (validator != null) {
                validator.setReady(ExpiryDateEditText.this, inputDateValid);
            }

            if (inputDateValid) {
                View next = focusSearch(View.FOCUS_RIGHT);
                if (next != null) {
                    next.requestFocus();
                }
            }

            addTextChangedListener(this);
        }
    }

    private boolean isInputDateValid(String dateStr) {
        if (dateStr.length() == 5) {
            Calendar c = Calendar.getInstance();
            int currentYear = c.get(Calendar.YEAR) - 2000;
            int currentMonth = c.get(Calendar.MONTH) + 1; //month january is 0

            int inputMonth = Integer.parseInt(dateStr.substring(0, 2));
            int inputYear = Integer.parseInt(dateStr.substring(3, 5));

            return ((inputYear * 12 + inputMonth)) >= ((currentYear * 12 + currentMonth) - MAX_EXPIRED_MONTHS);
        } else {
            return false;
        }
    }

}
