/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/10/2018.
 */

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject.SerializedName;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.internal.model.FieldSetConfigurationImpl;

/**
 * Configuration items about an {@link InputDetail}.
 */
@ProvidedBy(FieldSetConfigurationImpl.class)
public interface FieldSetConfiguration extends Configuration {

    /**
     * @return the {@link FieldVisibility} of the {@link InputDetail}.
     */
    @NonNull
    FieldVisibility getFieldVisibility();

    /**
     * The visibility type indicates how the {@link InputDetail} should be displayed to the user.
     */
    enum FieldVisibility {
        @SerializedName("hidden")
        HIDDEN,
        @SerializedName("readOnly")
        READ_ONLY,
        @SerializedName("editable")
        EDITABLE
    }
}
