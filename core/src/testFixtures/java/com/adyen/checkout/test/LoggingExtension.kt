/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.test

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LoggingExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext?) {
        // TODO Adyen logger
//    AdyenLogger.setLogger(PrintLogger())
    }

    override fun afterAll(context: ExtensionContext?) {
        // TODO Adyen logger
//    AdyenLogger.resetLogger()
    }
}
