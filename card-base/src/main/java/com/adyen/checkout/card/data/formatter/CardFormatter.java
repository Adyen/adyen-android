/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card.data.formatter;

import android.support.annotation.NonNull;

public interface CardFormatter extends NumberFormatter, ExpiryDateFormatter, SecurityCodeFormatter {
    final class Builder {
        private static final char DEFAULT_NUMBER_SEPARATOR = ' ';
        private static final char DEFAULT_EXPIRY_DATE_SEPARATOR = '/';

        private NumberFormatter mNumberFormatter;
        private ExpiryDateFormatter mExpiryDateFormatter;
        private SecurityCodeFormatter mSecurityCodeFormatter;

        @NonNull
        public Builder setNumberFormatter(@NonNull NumberFormatter formatter) {
            mNumberFormatter = formatter;
            return this;
        }

        @NonNull
        public Builder setExpiryDateFormatter(@NonNull ExpiryDateFormatter formatter) {
            mExpiryDateFormatter = formatter;
            return this;
        }

        @NonNull
        public Builder setSecurityCodeFromatter(@NonNull SecurityCodeFormatter formatter) {
            mSecurityCodeFormatter = formatter;
            return this;
        }

        @NonNull
        public CardFormatter build() {
            return new CardFormatterImpl(
                    mNumberFormatter != null ? mNumberFormatter : new NumberFormatterImpl(DEFAULT_NUMBER_SEPARATOR),
                    mExpiryDateFormatter != null ? mExpiryDateFormatter : new ExpiryDateFormatterImpl(DEFAULT_EXPIRY_DATE_SEPARATOR),
                    mSecurityCodeFormatter != null ? mSecurityCodeFormatter : new SecurityCodeFormatterImpl()
            );
        }
    }
}
