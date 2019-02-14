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

public final class LineItem implements Serializable {
    private static final String TAX_CATEGORY_HIGH = "High";
    private static final String TAX_CATEGORY_LOW = "Low";

    private static final int MOCKED_TAX_THRESHOLD = 2500;
    private static final String DEFAULT_DESCRIPTION = "Description";
    private static final int DEFAULT_TAX_PERCENTAGE = 2000;
    private static final int DEFAULT_QUANTITY = 1;

    @Json(name = "amountExcludingTax")
    private int mAmountExcludingTax;

    @Json(name = "amountIncludingTax")
    private int mAmountIncludingTax;

    @Json(name = "description")
    private String mDescription;

    @Json(name = "id")
    private String mId;

    @Json(name = "quantity")
    private int mQuantity;

    @Json(name = "taxAmount")
    private int mTaxAmount;

    @Json(name = "taxCategory")
    private String mTaxCategory;

    @Json(name = "taxPercentage")
    private int mTaxPercentage;

    public LineItem(@NonNull String id, int amountIncludingTax) {
        this.mId = id;
        this.mAmountIncludingTax = amountIncludingTax;

        mDescription = DEFAULT_DESCRIPTION;
        mQuantity = DEFAULT_QUANTITY;
        setTaxPercentage(DEFAULT_TAX_PERCENTAGE);
    }

    public void setDescription(@NonNull String description) {
        mDescription = description;
    }

    public void setQuantity(int quantity) {
        mQuantity = quantity;
    }

    public void setTaxPercentage(int taxPercentage) {
        mTaxPercentage = taxPercentage;

        mTaxAmount = mAmountIncludingTax / (10000 / this.mTaxPercentage);
        mAmountExcludingTax = mAmountIncludingTax - mTaxAmount;
        mTaxCategory = this.mTaxPercentage >= MOCKED_TAX_THRESHOLD ? TAX_CATEGORY_HIGH : TAX_CATEGORY_LOW;
    }
}
