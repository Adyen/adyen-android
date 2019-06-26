/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.card.data.output;

import com.adyen.checkout.base.component.data.output.BaseField;
import com.adyen.checkout.base.component.validator.Validity;
import com.adyen.checkout.card.data.validator.CardValidator;

public class SecurityCodeField extends BaseField<String> {
    private static final String EMPTY_STRING = "";

    SecurityCodeField() {
        super(EMPTY_STRING, EMPTY_STRING, new CardValidator.SecurityCodeValidationResult(Validity.INVALID, null));
    }
}
