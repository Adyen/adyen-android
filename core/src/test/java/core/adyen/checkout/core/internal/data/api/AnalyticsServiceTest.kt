package core.adyen.checkout.core.internal.data.api

import com.adyen.checkout.core.internal.data.api.AdyenApiResponse
import com.adyen.checkout.core.internal.data.api.AnalyticsService
import com.adyen.checkout.core.internal.data.api.HttpClient
import com.adyen.checkout.core.internal.data.model.AnalyticsTrackRequest
import com.adyen.checkout.core.internal.data.model.EmptyResponse
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class AnalyticsServiceTest(
    @Mock private val httpClient: HttpClient
) {

    private lateinit var analyticsService: AnalyticsService

    @BeforeEach
    fun setup() {
        analyticsService = AnalyticsService(
            httpClient = httpClient,
        )
    }

    @Test
    fun `when sending events, then an empty response is expected`() = runTest {
        val request = AnalyticsTrackRequest(
            channel = "android",
            platform = "android",
            info = emptyList(),
            logs = emptyList(),
            errors = emptyList(),
        )
        val checkoutAttemptId = "testtest"
        whenever(httpClient.post(eq("v3/analytics/$checkoutAttemptId"), any(), any(), any()))
            .doReturn(AdyenApiResponse("", 0, emptyMap(), ""))

        val response = analyticsService.sendEvents(request, checkoutAttemptId, TEST_CLIENT_KEY)

        assertInstanceOf(EmptyResponse::class.java, response)
    }

    companion object {
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
