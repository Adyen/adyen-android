/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/2/2024.
 */

package com.adyen.checkout.test

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.AdyenLogger
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

@RestrictTo(RestrictTo.Scope.TESTS, RestrictTo.Scope.LIBRARY_GROUP)
class LoggingExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        AdyenLogger.setLogger(PrintLogger())
    }

    override fun afterAll(context: ExtensionContext?) {
        AdyenLogger.resetLogger()
    }
}
