/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/4/2019.
 */
package com.adyen.checkout.components.models

import com.adyen.checkout.components.base.OutputData

class TestOutputData @JvmOverloads constructor(override var isValid: Boolean = false) : OutputData
