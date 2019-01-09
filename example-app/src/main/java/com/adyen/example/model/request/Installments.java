/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.example.model.request;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

import java.io.Serializable;


public final class Installments implements Serializable {
    @Json(name = "maxNumberOfInstallments")
    private Integer mMaxNumberOfInstallments;

    public Installments(@NonNull Integer maxNumberOfInstallments) {
        mMaxNumberOfInstallments = maxNumberOfInstallments;
    }

    @NonNull
    public Integer getMaxNumberOfInstallments() {
        return mMaxNumberOfInstallments;
    }
}
