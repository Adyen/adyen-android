/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2024.
 */

package com.adyen.checkout.demo.data.service

import android.content.res.AssetManager

class MockDataService(
    private val assetManager: AssetManager
) {
    fun readJsonFile(fileName: String): String {
        return assetManager.open(fileName).bufferedReader().use { it.readText() }
    }
}
