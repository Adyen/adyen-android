package com.adyen.checkout.base.internal;

import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 05/08/2017.
 */
public interface JsonSerializable {
    @NonNull
    JSONObject serialize() throws JSONException;
}
