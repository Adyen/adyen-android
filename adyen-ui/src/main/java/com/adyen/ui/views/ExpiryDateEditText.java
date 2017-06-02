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
import android.widget.EditText;

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
        this.addTextChangedListener(new ExpiryDateFormatWatcher(this));

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

        private static final String ZERO = "0";
        private static final String SEPARATOR = "/";

        private EditText mEditText;
        private int mLength;
        private int pos;
        private int count;

        ExpiryDateFormatWatcher(EditText editText) {
            this.mEditText = editText;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            pos = start;
            this.count = count;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            mLength = s.toString().length();
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (count > 1) {
                s.delete(pos, pos + count);
                validator.setReady(ExpiryDateEditText.this, false);
                return;
            }

            boolean moveCursorToEnd = false;
            String str = s.toString();
            int len = s.length();

            //user is deleting a char
            if (mLength >= len) {
                validator.setReady(ExpiryDateEditText.this, false);
                return;
            }

            String newChar = str.substring(pos, pos + 1);

            //user is trying to add invalid number to month value (number before separator), might result in
            // 122/17 or 76/17
            if (len >= 3 && pos < 3) {
                if (isDigit(str, 2) || Integer.parseInt(str.substring(0, 2)) > 12) {
                    s.delete(pos, pos + 1);
                    return;
                }
            }

            //Check if user is entering separator, only valid at pos 3
            if (!isDigit(newChar)) {
                if (newChar.equals(SEPARATOR)) {
                    if (pos == 1) {
                        s.insert(0, ZERO);
                    } else if (pos == 2) {
                        //do nothing
                    } else {
                        s.delete(pos, pos + 1);
                    }
                } else {
                    s.delete(pos, pos + 1);
                }
            } else {
                //user added a digit, check if result is valid
                if (len == 1) {
                    if (!charIsOneOrZero(s.charAt(0))) {
                        final String newStr = ZERO + str;
                        s.insert(0, ZERO);
                        moveCursorToEnd = true;
                    }
                } else if (len == 2) {
                    if (String.valueOf(s.charAt(s.length() - 1)).equals(SEPARATOR)) {
                        mEditText.setText(ZERO + str);
                        moveCursorToEnd = true;
                    } else if (Integer.parseInt(str.substring(0, 2)) <= 12
                            && Integer.parseInt(str.substring(0, 2)) > 0) {
                        s.append(SEPARATOR);
                        moveCursorToEnd = true;
                    } else {
                        s.delete(pos, pos + 1);
                    }
                } else if (len == 3) {

                } else if (len == 4) {

                } else if (len == 5) {

                }
            }

            if (moveCursorToEnd) {
                mEditText.setSelection(mEditText.getText().toString().length());
            }

            if (validator != null) {
                validator.setReady(ExpiryDateEditText.this, isInputDateValid(s.toString()));
            }

            if (isInputDateValid(s.toString())) {
                View next = focusSearch(View.FOCUS_RIGHT);
                if (next != null) {
                    next.requestFocus();
                }
            }
        }

        private boolean charIsOneOrZero(char charFromString) {
            int month = 0;
            if (Character.isDigit(charFromString)) {
                month = Integer.parseInt(String.valueOf(charFromString));
            }
            return month <= 1;
        }

        private boolean isDigit(String string) {
            return string.matches(".*\\d.*");
        }

        private boolean isDigit(String str, int pos) {
            return str.substring(pos, pos + 1).matches(".*\\d.*");
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
