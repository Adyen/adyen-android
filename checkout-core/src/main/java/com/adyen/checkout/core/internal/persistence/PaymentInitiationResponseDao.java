package com.adyen.checkout.core.internal.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 29/05/2018.
 */
@Dao
interface PaymentInitiationResponseDao extends BaseDao<PaymentInitiationResponseEntity> {
    @Nullable
    @Query("SELECT * FROM payment_initiation_responses WHERE payment_session_uuid = (:paymentSessionEntityUuid) ORDER BY id DESC LIMIT 1;")
    PaymentInitiationResponseEntity findLatestByPaymentSessionEntityUuid(@NonNull String paymentSessionEntityUuid);

    @Query("DELETE FROM payment_initiation_responses WHERE payment_session_uuid = (:paymentSessionUuid);")
    void deleteByPaymentSessionEntityUuid(@NonNull String paymentSessionUuid);
}
