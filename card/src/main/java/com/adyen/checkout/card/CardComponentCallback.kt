/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/7/2023.
 */

package com.adyen.checkout.card

import com.adyen.checkout.components.core.BinComponentCallback
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.sessions.core.SessionComponentCallback

interface CardComponentCallback : ComponentCallback<CardComponentState>, BinComponentCallback

interface SessionCardComponentCallback : SessionComponentCallback<CardComponentState>, BinComponentCallback
