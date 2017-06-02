package com.adyen.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.adyen.ui.utils.AdyenInputValidator;
import com.adyen.ui.utils.Iban;

import java.util.ArrayList;

public class IBANEditText extends EditText {

    private AdyenInputValidator validator;

    private static final int MAX_IBAN_LENGTH = 30;
    public static final int MAX_LENGTH_EDITTEXT = MAX_IBAN_LENGTH + 6; //max iban length + 6 white spaces

    public IBANEditText(Context context) {
        super(context);
        init();
    }

    public IBANEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IBANEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IBANEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setValidator(AdyenInputValidator validator) {
        this.validator = validator;
        this.validator.addInputField(IBANEditText.this);
    }

    private void init() {
        ArrayList<InputFilter> inputFilters = new ArrayList<>();
        inputFilters.add(new InputFilter.LengthFilter(MAX_LENGTH_EDITTEXT));
        inputFilters.add(new InputFilter.AllCaps());
        inputFilters.add(new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (char c : source.toString().toCharArray()) {
                    if (!Character.isDigit(c) && !Character.isWhitespace(c) && !Character.isLetter(c)) {
                        return "";
                    }
                }
                return source;
            }
        });
        this.setFilters(inputFilters.toArray(new InputFilter[inputFilters.size()]));

        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (validator != null) {
                    if (s.toString().replaceAll(" ", "").length() > 2) {
                        boolean ibanValid = Iban.validate(s.toString().replaceAll(" ", ""));
                        validator.setReady(IBANEditText.this, ibanValid);
                    } else {
                        validator.setReady(IBANEditText.this, false);
                    }
                }
            }
        });
    }

    public String getIbanNumber() {
        return getText().toString().replaceAll(" ", "");
    }

}
