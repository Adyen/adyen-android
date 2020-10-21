/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/4/2019.
 */

package com.adyen.checkout.core.mock;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONObject;

public class MockModelObject extends ModelObject {

    @NonNull
    public static final Serializer<MockModelObject> SERIALIZER = new Serializer<MockModelObject>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull MockModelObject modelObject) {
            return new JSONObject();
        }

        @NonNull
        @Override
        public MockModelObject deserialize(@NonNull JSONObject jsonObject) {
            return new MockModelObject();
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // empty
    }
}
