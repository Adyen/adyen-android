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
interface PaymentSessionDao extends BaseDao<PaymentSessionEntity> {
    @Nullable
    @Query("SELECT * FROM payment_sessions WHERE uuid = (:uuid);")
    PaymentSessionEntity findByUuid(@NonNull String uuid);

    @Query("DELETE FROM payment_sessions WHERE uuid = (:uuid);")
    void deleteByUuid(@NonNull String uuid);
}
