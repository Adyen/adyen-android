/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/4/2024.
 */

package com.adyen.threeds2;

/**
 * @noinspection unused
 */
/*
Fake ThreeDS2Service that overrides the static instance of the actual library, because it crashes unit tests. Do not
move this class to the 3ds2 module because the tests there depend on the actual ThreeDS2Service from the 3DS2 SDK.
*/
public interface ThreeDS2Service {

    /**
     * @noinspection UnnecessaryModifier
     */
    public String getSDKVersion();


    static String SDK_VERSION = "1.2.3-test";

    /**
     * @noinspection UnnecessaryModifier
     */
    static ThreeDS2Service INSTANCE = () -> SDK_VERSION;
}
