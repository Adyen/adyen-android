/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/4/2019.
 */
package com.adyen.checkout.core.api

import org.junit.Assert
import org.junit.Test

class HttpUrlConnectionFactoryTest {

    @Test
    fun `when URL is secure - insecure connection callback is NOT called`() {
        val url = "https://test.com"
        val urlConnection = HttpUrlConnectionFactory.createHttpUrlConnection(url) {
            Assert.assertEquals(1, 2)
            it
        }
        Assert.assertEquals(urlConnection.url.toString(), url)
    }

    @Test(expected = SecurityException::class)
    fun `when URL is NOT secure - insecure connection callback is called`() {
        val url = "http://test.com"
        HttpUrlConnectionFactory.createHttpUrlConnection(url) {
            throw SecurityException()
        }
    }
}
