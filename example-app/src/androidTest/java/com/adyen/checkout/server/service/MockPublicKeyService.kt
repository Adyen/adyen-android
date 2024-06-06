/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.server.service

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

internal class MockPublicKeyService : MockBackendService(
    "/v1/clientKeys/.*",
    useRegex = true,
) {

    override fun handleRequest(request: RecordedRequest): MockResponse {
        return createJsonResponse("public_key_response.json")
    }
}
