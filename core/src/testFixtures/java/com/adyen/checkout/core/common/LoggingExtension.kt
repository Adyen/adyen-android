/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2025.
 */

package com.adyen.checkout.core.common

import com.adyen.checkout.core.common.AdyenLogger.LogLevel
import com.adyen.checkout.core.common.internal.helper.AdyenLogDispatcher
import com.adyen.checkout.core.common.internal.helper.LogWriter
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class LoggingExtension : BeforeAllCallback, AfterAllCallback {

    override fun beforeAll(context: ExtensionContext) {
        context.getStore(NAMESPACE).put(KEY, LoggingState(AdyenLogDispatcher.writer, AdyenLogDispatcher.logLevel))
        AdyenLogDispatcher.writer = PrintLogger()
        AdyenLogDispatcher.logLevel = LogLevel.VERBOSE
    }

    override fun afterAll(context: ExtensionContext) {
        val state = context.getStore(NAMESPACE).get(KEY, LoggingState::class.java) ?: return
        AdyenLogDispatcher.writer = state.writer
        AdyenLogDispatcher.logLevel = state.logLevel
    }

    private data class LoggingState(
        val writer: LogWriter,
        val logLevel: LogLevel,
    )

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(LoggingExtension::class.java)
        private const val KEY = "loggingState"
    }
}
