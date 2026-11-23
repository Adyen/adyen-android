/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/8/2025.
 */

package com.adyen.checkout.core.common

import com.adyen.checkout.core.common.internal.helper.LogWriter
import com.adyen.checkout.core.components.CheckoutLogLevel
import java.io.PrintWriter
import java.io.StringWriter

internal class PrintLogger : LogWriter {

    override fun write(level: CheckoutLogLevel, tag: String, message: String, throwable: Throwable?) {
        println("${getLogColor(level)}${concatThrowable(message, throwable)}$RESET_COLOR")
    }

    private fun getLogColor(level: CheckoutLogLevel): String {
        return when (level) {
            CheckoutLogLevel.WARN -> YELLOW_COLOR
            CheckoutLogLevel.ERROR,
            CheckoutLogLevel.ASSERT -> RED_COLOR

            else -> ""
        }
    }

    private fun concatThrowable(message: String, throwable: Throwable?): String {
        return if (throwable != null) {
            val stringWriter = StringWriter(STRING_WRITER_SIZE)
            val printWriter = PrintWriter(stringWriter, false)
            throwable.printStackTrace(printWriter)
            printWriter.flush()
            "$message: $stringWriter"
        } else {
            message
        }
    }

    companion object {
        private const val YELLOW_COLOR = "\u001B[33m"
        private const val RED_COLOR = "\u001B[31m"
        private const val RESET_COLOR = "\u001B[0m"

        private const val STRING_WRITER_SIZE = 16
    }
}
