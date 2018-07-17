package com.adyen.checkout.core.internal.persistence;

import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by ran on 30/05/2018.
 */
interface BaseDao<T> {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    void insert(@NonNull T t);

    @Update
    void update(@NonNull T t);
}
