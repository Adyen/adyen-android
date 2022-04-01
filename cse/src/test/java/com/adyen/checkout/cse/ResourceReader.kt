/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/3/2021.
 */
package com.adyen.checkout.cse

import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

object ResourceReader {
    @JvmStatic
    @Throws(IOException::class, JSONException::class)
    fun readJsonFileFromResource(fileName: String?, classLoader: ClassLoader): JSONObject {
        val file = File(classLoader.getResource(fileName).file)
        val encoded = Files.readAllBytes(Paths.get(file.path))
        return JSONObject(String(encoded, Charset.defaultCharset()))
    }
}
