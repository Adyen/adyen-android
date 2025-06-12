package core.adyen.checkout.core.internal.analytics.data.remote

import com.adyen.checkout.core.internal.analytics.AnalyticsEvent
import com.adyen.checkout.core.internal.analytics.DirectAnalyticsEventCreation
import com.adyen.checkout.core.internal.analytics.data.remote.AnalyticsTrackRequestProvider
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackError
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackInfo
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackLog
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(DirectAnalyticsEventCreation::class)
internal class AnalyticsTrackRequestProviderTest {

    private lateinit var analyticsTrackRequestProvider: AnalyticsTrackRequestProvider

    @BeforeEach
    fun setup() {
        analyticsTrackRequestProvider = AnalyticsTrackRequestProvider()
    }

    @Test
    fun `when providing, then objects should be mapped correctly`() {
        val infoList = listOf(
            AnalyticsEvent.Info(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = AnalyticsEvent.Info.Type.INPUT,
                target = "target",
                isStoredPaymentMethod = true,
                brand = "brand",
                issuer = "issuer",
                validationErrorCode = "418",
                validationErrorMessage = "I'm a teapot",
                configData = mapOf("test" to "yes"),
            ),
        )
        val logList = listOf(
            AnalyticsEvent.Log(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                type = null,
                subType = null,
                target = null,
                message = null,
            ),
        )
        val errorList = listOf(
            AnalyticsEvent.Error(
                id = "id",
                timestamp = 12345L,
                component = "dropin",
                errorType = AnalyticsEvent.Error.Type.INTERNAL,
                code = "100",
                target = null,
                message = null,
            ),
        )

        val result = analyticsTrackRequestProvider.invoke(infoList, logList, errorList)

        val expected = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = listOf(
                AnalyticsTrackInfo(
                    id = "id",
                    timestamp = 12345L,
                    component = "dropin",
                    type = "input",
                    target = "target",
                    isStoredPaymentMethod = true,
                    brand = "brand",
                    issuer = "issuer",
                    validationErrorCode = "418",
                    validationErrorMessage = "I'm a teapot",
                    configData = mapOf("test" to "yes"),
                ),
            ),
            logs = listOf(
                AnalyticsTrackLog(
                    id = "id",
                    timestamp = 12345L,
                    component = "dropin",
                    type = null,
                    subType = null,
                    result = null,
                    target = null,
                    message = null,
                ),
            ),
            errors = listOf(
                AnalyticsTrackError(
                    id = "id",
                    timestamp = 12345L,
                    component = "dropin",
                    errorType = "Internal",
                    code = "100",
                    target = null,
                    message = null,
                ),
            ),
        )
        assertEquals(expected, result)
    }
}
