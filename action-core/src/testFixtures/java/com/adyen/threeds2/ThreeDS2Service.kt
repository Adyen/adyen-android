/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 19/7/2024.
 */

package com.adyen.threeds2

/*
Fake ThreeDS2Service that overrides the static instance of the actual library, because it crashes unit tests. Do not
move this class to the 3ds2 module because the tests there depend on the actual ThreeDS2Service from the 3DS2 SDK.
*/
interface ThreeDS2Service {

    val sdkVersion: String
    fun getSDKVersion(): String = sdkVersion

    companion object {

        @JvmField
        val INSTANCE: ThreeDS2Service = object : ThreeDS2Service {
            override val sdkVersion: String = "1.2.3-test"
        }
    }
}
