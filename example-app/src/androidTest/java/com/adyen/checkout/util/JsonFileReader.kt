/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/6/2024.
 */

package com.adyen.checkout.util

import androidx.test.platform.app.InstrumentationRegistry
import dagger.hilt.android.testing.HiltTestApplication
import java.io.InputStreamReader

internal object JsonFileReader {

    operator fun invoke(fileName: String): String {
        val application =
            (InstrumentationRegistry.getInstrumentation().context)
        val inputStream = application.assets.open(fileName)
        val stringBuilder = StringBuilder()
        val reader = InputStreamReader(inputStream)
        reader.readLines().forEach { stringBuilder.append(it) }
        return stringBuilder.toString()
    }
}
