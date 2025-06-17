@file:Suppress("MaximumLineLength")

package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.Environment
import java.util.regex.Pattern

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object ValidationUtils {

    @Suppress("ktlint:standard:max-line-length", "MaxLineLength")
    private const val EMAIL_REGEX =
        "^(([a-z0-9!#$%&'*+\\-/=?^_`{|}~]+(\\.[a-z0-9!#$%&'*+\\-/=?^_`{|}~]+)*)|(\".+\"))@((\\[((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}])|((?!-)[a-z0-9-]{1,63}(?<!-)(\\.[a-z0-9-]{1,63}(?<!-))*\\.[a-z]{2,}))$"
    private val EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE)

    private const val PHONE_REGEX = "^\\D*(\\d\\D*){9,14}$"
    private val PHONE_PATTERN = Pattern.compile(PHONE_REGEX)

    private val TEST_CLIENT_KEY_PATTERN = Pattern.compile("test_([a-zA-Z0-9]){32}")
    private val LIVE_CLIENT_KEY_PATTERN = Pattern.compile("live_([a-zA-Z0-9]){32}")

    /**
     * Check if phone number is valid.
     *
     * @param phoneNumber A string to check if is a phone number.
     * @return If we consider it a valid phone number or not.
     */
    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return PHONE_PATTERN.matcher(phoneNumber).matches()
    }

    /**
     * Check if email is valid.
     *
     * @param emailAddress A string to check if is an email address.
     * @return If we consider it a valid email or not.
     */
    fun isEmailValid(emailAddress: String): Boolean {
        return EMAIL_PATTERN.matcher(emailAddress).matches()
    }

    /**
     * Check if the Client Key is valid.
     *
     * @param clientKey A string to check if is a client key.
     * @param environment Selected environment in the configuration.
     * @return If the client key is valid and matches the chosen environment.
     */
    internal fun isClientKeyValid(clientKey: String, environment: Environment): Boolean {
        return when (environment) {
            Environment.TEST -> TEST_CLIENT_KEY_PATTERN.matcher(clientKey).matches()

            Environment.APSE,
            Environment.AUSTRALIA,
            Environment.EUROPE,
            Environment.INDIA,
            Environment.UNITED_STATES -> LIVE_CLIENT_KEY_PATTERN.matcher(clientKey).matches()

            // this should not be reachable in reality as Environment cannot be instantiated using the constructor
            else -> false
        }
    }
}
