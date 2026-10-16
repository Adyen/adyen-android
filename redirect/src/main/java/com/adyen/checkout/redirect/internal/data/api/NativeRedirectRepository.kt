/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2026.
 */

package com.adyen.checkout.redirect.internal.data.api

import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectRequest
import com.adyen.checkout.redirect.internal.data.model.NativeRedirectResponse
import org.json.JSONObject

internal class NativeRedirectRepository(
    private val nativeRedirectService: NativeRedirectService,
) {

    suspend fun makeNativeRedirect(
        nativeRedirectData: String?,
        returnQueryString: String,
        clientKey: String,
    ): Result<JSONObject> = runSuspendCatching {
        val request = NativeRedirectRequest(
            redirectData = nativeRedirectData,
            returnQueryString = returnQueryString,
        )
        val response = nativeRedirectService.makeNativeRedirect(request, clientKey)
        NativeRedirectResponse.SERIALIZER.serialize(response)
    }
}
