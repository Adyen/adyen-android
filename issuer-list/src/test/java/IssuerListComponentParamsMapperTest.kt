/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/11/2022.
 */

import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListComponentParams
import com.adyen.checkout.issuerlist.IssuerListComponentParamsMapper
import com.adyen.checkout.issuerlist.IssuerListViewType
import com.adyen.checkout.issuerlist.utils.TestIssuerListConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Locale

internal class IssuerListComponentParamsMapperTest {

    @Test
    fun `when parent configuration is null and custom issuer list configuration fields are null then all fields should match`() {
        val issuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .build()

        val params = IssuerListComponentParamsMapper(
            parentConfiguration = null,
            isCreatedByDropIn = false
        ).mapToParams(issuerListConfiguration)

        val expected = IssuerListComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isCreatedByDropIn = false,
            viewType = IssuerListViewType.RECYCLER_VIEW,
            hideIssuerLogos = false,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is null and custom issuer list configuration fields are set then all fields should match`() {
        val issuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setHideIssuerLogos(true)
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .build()

        val params = IssuerListComponentParamsMapper(
            parentConfiguration = null,
            isCreatedByDropIn = false
        ).mapToParams(issuerListConfiguration)

        val expected = IssuerListComponentParams(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1,
            isCreatedByDropIn = false,
            viewType = IssuerListViewType.SPINNER_VIEW,
            hideIssuerLogos = true,
        )

        assertEquals(expected, params)
    }

    @Test
    fun `when parent configuration is set then parent configuration fields should override issuer list configuration fields`() {
        val issuerListConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.US,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY_1
        )
            .setHideIssuerLogos(true)
            .setViewType(IssuerListViewType.SPINNER_VIEW)
            .build()

        // this is in practice DropInConfiguration, but we don't have access to it in this module and any Configuration
        // class can work
        val parentConfiguration = TestIssuerListConfiguration.Builder(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
        )
            .build()

        val params = IssuerListComponentParamsMapper(
            parentConfiguration = parentConfiguration,
            isCreatedByDropIn = true
        ).mapToParams(issuerListConfiguration)

        val expected = IssuerListComponentParams(
            shopperLocale = Locale.GERMAN,
            environment = Environment.EUROPE,
            clientKey = TEST_CLIENT_KEY_2,
            isCreatedByDropIn = true,
            viewType = IssuerListViewType.SPINNER_VIEW,
            hideIssuerLogos = true,
        )

        assertEquals(expected, params)
    }

    companion object {
        private const val TEST_CLIENT_KEY_1 = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
        private const val TEST_CLIENT_KEY_2 = "live_qwertyui34566776787zxcvbnmqwerty"
    }
}
