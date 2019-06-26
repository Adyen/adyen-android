/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */

package com.adyen.checkout.base.models;

import com.adyen.checkout.base.component.data.output.OutputData;

public class TestOutputData implements OutputData {

    public String type;
    public boolean isValid;

    public TestOutputData(String type) {
        this.type = type;
    }

    public TestOutputData(String type, boolean isValid) {
        this.type = type;
        this.isValid = isValid;
    }

    @Override
    public boolean isValid() {
        return this.isValid;
    }


}
