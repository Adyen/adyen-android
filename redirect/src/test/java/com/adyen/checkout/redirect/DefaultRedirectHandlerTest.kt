/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 22/4/2021.
 */

package com.adyen.checkout.redirect

import android.net.Uri
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.redirect.handler.DefaultRedirectHandler
import com.google.common.collect.Iterators
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DefaultRedirectHandlerTest {

    private val handler = DefaultRedirectHandler()

    @Test
    fun parseRedirectResult_ExtractPayload_ExpectResponse() {
        val uri = Uri.parse("http://www.example.com?p1=value1&payload=YOUR_PAYLOAD&p2=value2")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("payload"), "YOUR_PAYLOAD")
    }

    @Test
    fun parseRedirectResult_ExtractRedirectResult_ExpectResponse() {
        val uri = Uri.parse("url://domain?param1=dfgsd&redirectResult=ZXdxcjQzMnI0Zg%3D%3D&param2=1464")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("redirectResult"), "ZXdxcjQzMnI0Zg==")
    }

    @Test
    fun parseRedirectResult_ExtractPaResAndMD_ExpectResponse() {
        val uri = Uri.parse("https://example.com/actor/board?param=abcfd&PaRes=paymentresult&MD=lorem")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 2)
        assertEquals(response.getString("MD"), "lorem")
        assertEquals(response.getString("PaRes"), "paymentresult")
    }

    @Test
    fun parseRedirectResult_ExtractReturnUrlQueryString_ExpectResponse() {
        val uri = Uri.parse("http://www.example.com/attack.aspx?param1=abc&pp=ZXdxcj1zZ/+Cs0Mz+JyNGY=&p2=fdghb")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("returnUrlQueryString"), "param1=abc&pp=ZXdxcj1zZ/+Cs0Mz+JyNGY=&p2=fdghb")
    }

    @Test
    fun parseRedirectResult_PayloadAndPaResWithoutMD_ExpectResponse() {
        val uri = Uri.parse("https://www.example.net/airport.htm/?payload=some&param1=rtgt&PaRes=PA_RES")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("payload"), "some")
    }

    @Test
    fun parseRedirectResult_PaResWithoutMD_ExpectResponse() {
        val uri = Uri.parse("https://www.example.com/balance.php?param1=abc&PaRes=436564")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("returnUrlQueryString"), "param1=abc&PaRes=436564")
    }

    @Test
    fun parseRedirectResult_MDWithoutPaRes_ExpectResponse() {
        val uri = Uri.parse("url://?param1=abc&MD=MD23u09")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("returnUrlQueryString"), "param1=abc&MD=MD23u09")
    }

    @Test
    fun parseRedirectResult_NoValidParameters_ExpectResponse() {
        val uri = Uri.parse("url://www.example.com/?p1=abc&p2=3")
        val response = handler.parseRedirectResult(uri)

        assertEquals(Iterators.size(response.keys()), 1)
        assertEquals(response.getString("returnUrlQueryString"), "p1=abc&p2=3")
    }

    @Test(expected = CheckoutException::class)
    fun parseRedirectResult_NoQuery_ExpectException() {
        val uri = Uri.parse("http://www.example.com/payment#invoice")
        handler.parseRedirectResult(uri)
    }

    @Test(expected = CheckoutException::class)
    fun parseRedirectResult_NullUri_ExpectException() {
        handler.parseRedirectResult(null)
    }
}
