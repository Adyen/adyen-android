package com.adyen.checkout.ui.internal.common.util;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.model.InputDetailImpl;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.PaymentMethod;

import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 02/05/2018.
 */
public final class PaymentMethodUtil {
    @NonNull
    public static Requirement getRequirementForInputDetail(@NonNull String key, @NonNull PaymentMethod paymentMethod) {
        InputDetail inputDetail = InputDetailImpl.findByKey(paymentMethod.getInputDetails(), key);

        return inputDetail == null ? Requirement.NONE : (inputDetail.isOptional() ? Requirement.OPTIONAL : Requirement.REQUIRED);
    }

    @NonNull
    public static Requirement getRequirementForInputDetail(@NonNull String key, @NonNull List<PaymentMethod> paymentMethods) {
        Requirement requirement = Requirement.NONE;

        for (PaymentMethod paymentMethod : paymentMethods) {
            Requirement newRequirement = getRequirementForInputDetail(key, paymentMethod);

            if (newRequirement.dominates(requirement)) {
                requirement = newRequirement;
            }
        }

        return requirement;
    }

    private PaymentMethodUtil() {
        throw new IllegalStateException("No instances.");
    }

    public enum Requirement {
        NONE,
        REQUIRED,
        OPTIONAL;

        private boolean dominates(@NonNull Requirement requirement) {
            return ordinal() > requirement.ordinal();
        }
    }
}
