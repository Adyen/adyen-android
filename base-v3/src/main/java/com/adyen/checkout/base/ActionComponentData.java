/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

import org.json.JSONObject;

public class ActionComponentData {

    private final JSONObject mDetails;

    public ActionComponentData(@NonNull JSONObject actionDetails) {
        mDetails = actionDetails;
    }

    @NonNull
    public JSONObject getDetails() {
        return mDetails;
    }

}
