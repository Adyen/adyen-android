/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/8/2022.
 */

package com.adyen.checkout.await

import com.adyen.checkout.components.base.ActionDelegate
import com.adyen.checkout.components.base.DetailsEmittingDelegate
import com.adyen.checkout.components.base.StatusPollingDelegate
import com.adyen.checkout.components.base.ViewableDelegate
import com.adyen.checkout.components.model.payments.response.AwaitAction

interface AwaitDelegate :
    ActionDelegate<AwaitAction>,
    DetailsEmittingDelegate,
    ViewableDelegate<AwaitOutputData>,
    StatusPollingDelegate
