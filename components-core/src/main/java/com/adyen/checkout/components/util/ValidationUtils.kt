package com.adyen.checkout.components.util

import com.adyen.checkout.core.api.Environment
import java.util.regex.Pattern

object ValidationUtils {

    private const val EMAIL_REGEX = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$"
    private val EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX, Pattern.CASE_INSENSITIVE)

    private const val PHONE_REGEX = "^\\D*(\\d\\D*){9,14}$"
    private val PHONE_PATTERN = Pattern.compile(PHONE_REGEX)

    private const val CLIENT_KEY_REGEX = "([a-z]){4}\\_([A-z]|\\d){32}"
    private val CLIENT_KEY_PATTERN = Pattern.compile(CLIENT_KEY_REGEX)

    private const val CLIENT_KEY_TEST_PREFIX = "test_"
    private const val CLIENT_KEY_LIVE_PREFIX = "live_"

    private val LIVE_ENVIRONMENTS = listOf(
        Environment.APSE,
        Environment.AUSTRALIA,
        Environment.EUROPE,
        Environment.INDIA,
        Environment.UNITED_STATES,
    )

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
     * @return If we consider it a valid client key or not.
     */
    fun isClientKeyValid(clientKey: String): Boolean {
        return CLIENT_KEY_PATTERN.matcher(clientKey).matches()
    }

    /**
     * Check if the Client Key matches the Environment
     *
     * @param clientKey A string to check if is a client key.
     * @param environment Selected environment in the configuration.
     * @return If Client Key is preceded with correct prefix
     */
    fun doesClientKeyMatchEnvironment(clientKey: String, environment: Environment): Boolean {
        val isTestEnvironment = environment == Environment.TEST
        val isLiveEnvironment = LIVE_ENVIRONMENTS.contains(environment)

        return (isLiveEnvironment && clientKey.startsWith(CLIENT_KEY_LIVE_PREFIX)) ||
            (isTestEnvironment && clientKey.startsWith(CLIENT_KEY_TEST_PREFIX)) ||
            (!isLiveEnvironment && !isTestEnvironment)
    }
}
