package com.adyen.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.adyen.core.utils.AsyncImageDownloader;
import com.adyen.core.utils.StringUtils;
import com.adyen.ui.R;
import com.adyen.ui.utils.AdyenInputValidator;
import com.adyen.ui.utils.CardType;
import com.adyen.ui.utils.IconUtil;
import com.adyen.ui.utils.Luhn;

import java.util.ArrayList;
import java.util.List;


/**
 * EditText element to capture credit card number.
 */
public class CreditCardEditText extends CheckoutEditText {

    private static final int CC_MAX_LENGTH = 19 + 4;

    private AdyenInputValidator validator;
    private CVCEditText cvcEditText;

    private String baseURL;
    private String placeHolderIconUrl = "";

    private List<String> allowedCardTypes;
    private Integer[] numberOfDigits;

    private void init() {
        ArrayList<InputFilter> cardNoFilters = new ArrayList<>();
        //TODO adjust cc number max length to type of card
        cardNoFilters.add(new InputFilter.LengthFilter(CC_MAX_LENGTH));
        cardNoFilters.add(new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (char c : source.toString().toCharArray()) {
                    if (!Character.isDigit(c) && !Character.isWhitespace(c)) {
                         return "";
                    }
                }
                return source;
            }
        });
        this.setFilters(cardNoFilters.toArray(new InputFilter[cardNoFilters.size()]));
        this.addTextChangedListener(new CreditCardInputFormatWatcher());

        setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CreditCardEditText.this.setTextColor(ContextCompat.getColor(getContext(), R.color.black_text));
                } else {
                    if (!isValidNr(CreditCardEditText.this.getCCNumber())) {
                        CreditCardEditText.this.setTextColor(ContextCompat.getColor(getContext(),
                                R.color.red_invalid_input_highlight));
                    }
                }
            }
        });
    }

    public CreditCardEditText(Context context) {
        super(context);
        init();
    }

    public CreditCardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CreditCardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setValidator(AdyenInputValidator validator) {
        this.validator = validator;
        this.validator.addInputField(CreditCardEditText.this);
    }

    public String getCCNumber() {
        return this.getText().toString().replace(" ", "");
    }

    private class CreditCardInputFormatWatcher implements TextWatcher {

        private static final char SEPARATOR = ' ';
        private int pos;
        private int mLength;

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            pos = start;
            mLength = s.toString().length();

        }

        @Override
        public void afterTextChanged(Editable s) {

            if (pos == 0) {
                initializeLogo();
                if (s.toString().length() == 1 && s.charAt(pos) == SEPARATOR) {
                    s.clear();
                }
                if (validator != null) {
                    validator.setReady(CreditCardEditText.this, isValidNr(s.toString()));
                }
                return;
            }

            int len = s.length();
            if (!(mLength >= len)) {
                // Remove spacing char
                if (!(pos > 0 && ((pos - 4) % 5) == 0)) {
                    final char c = s.charAt(pos);
                    if (SEPARATOR == c) {
                        s.delete(pos - 1, pos);
                    }
                }

                // Insert SEPARATOR where needed.
                if (s.length() > 0 && (s.length() % 5) == 0) {
                    char c = s.charAt(pos);
                    //If user enters a digit where there should be a space we insert SEPARATOR
                    if (Character.isDigit(c) && TextUtils.split(s.toString(), String.valueOf(SEPARATOR)).length <= 4) {
                        s.insert(s.length() - 1, String.valueOf(SEPARATOR));
                    }
                }
            }

            int cvcLength = CVCEditText.CVC_MAX_LENGTH;

            CardType cardType = CardType.detect(s.toString().replace(" ", ""), allowedCardTypes);
            if (cardType == CardType.amex) {
                cvcLength = CVCEditText.CVC_MAX_LENGTH_AMEX;
            }
            if (cardType != CardType.UNKNOWN) {
                if (!StringUtils.isEmptyOrNull(baseURL)) {
                    final String imageUrl = IconUtil.addScaleFactorToIconUrl(getContext(), baseURL + cardType + ".png");
                    AsyncImageDownloader.downloadImage(getContext(), new AsyncImageDownloader.ImageListener() {
                        @Override
                        public void onImage(final Bitmap bitmap, final String url) {
                            setCCIcon(bitmap);
                        }
                    }, imageUrl, null);
                }
                numberOfDigits = cardType.getNumberOfDigits();
            } else {
                initializeLogo();
                numberOfDigits = null;
            }

            if (cvcEditText != null) {
                cvcEditText.setMaxLength(cvcLength);
            }

            if (validator != null) {
                validator.setReady(CreditCardEditText.this, isValidNr(s.toString()));
            }

        }
    }

    private boolean isValidNr(String str) {
        boolean luhn = Luhn.check(str);
        boolean length = false;
        if (numberOfDigits != null) {
            for (Integer i : numberOfDigits) {
                if (str.replaceAll(" ", "").length() == i) {
                    length = true;
                }
            }
        }
        return luhn && length;
    }

    private void setCCIcon(Bitmap bitmap) {
        Drawable drawable = IconUtil.resizeRoundCornersAndAddBorder(getContext(), bitmap, (int) getTextSize());
        CreditCardEditText.this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
    }

    public void setCVCEditText(CVCEditText cvcEditText) {
        this.cvcEditText = cvcEditText;
    }

    public void setLogoUrl(String cardIconUrl) {
        this.placeHolderIconUrl = cardIconUrl;
        this.baseURL = getLogoBaseURL(cardIconUrl);
    }

    public void initializeLogo() {
        if (!StringUtils.isEmptyOrNull(placeHolderIconUrl)) {
            AsyncImageDownloader.downloadImage(getContext(), new AsyncImageDownloader.ImageListener() {
                @Override
                public void onImage(final Bitmap bitmap, final String url) {
                    setCCIcon(bitmap);
                }
            }, IconUtil.addScaleFactorToIconUrl(getContext(), placeHolderIconUrl), null);
        }
    }

    private String getLogoBaseURL(final String logoUrl) {
        final int lastIndexOfSeparator = logoUrl.lastIndexOf("/");
        if (lastIndexOfSeparator == -1) {
            return "";
        }
        return logoUrl.substring(0, lastIndexOfSeparator + 1);
    }

    public void setAllowedCardTypes(List<String> allowedCardTypes) {
        this.allowedCardTypes = allowedCardTypes;
    }
}
