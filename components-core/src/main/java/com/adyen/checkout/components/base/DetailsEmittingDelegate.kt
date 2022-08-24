/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/8/2022.
 */

package com.adyen.checkout.components.base

import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface DetailsEmittingDelegate {

    val detailsFlow: Flow<JSONObject>
}
