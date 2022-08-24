/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/8/2022.
 */

package com.adyen.checkout.components.status.model

data class TimerData(
    val millisUntilFinished: Long,
    val progress: Int,
)
