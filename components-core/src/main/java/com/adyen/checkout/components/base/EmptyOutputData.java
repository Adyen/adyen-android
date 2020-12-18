/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/10/2019.
 */

package com.adyen.checkout.components.base;

public final class EmptyOutputData implements OutputData {
    @Override
    public boolean isValid() {
        return true;
    }
}
