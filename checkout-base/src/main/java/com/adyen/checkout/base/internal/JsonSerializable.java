/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 05/08/2017.
 */

package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonSerializable {
    @NonNull
    JSONObject serialize() throws JSONException;
}
