/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/7/2022.
 */

package com.adyen.checkout.components.core.internal.util

import com.adyen.checkout.core.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ValidationUtilsTest {

    @ParameterizedTest
    @MethodSource("clientKeyEnvironmentSource")
    fun `client key should match environment`(clientKey: String, environment: Environment, shouldMatch: Boolean) {
        assertEquals(shouldMatch, ValidationUtils.doesClientKeyMatchEnvironment(clientKey, environment))
    }

    @ParameterizedTest
    @MethodSource("getTestEmailList")
    fun `Email must match the value given in the list`(email: String, shouldMatch: Boolean) {
        assertEquals(ValidationUtils.isEmailValid(email), shouldMatch)
    }

    companion object {

        @JvmStatic
        fun clientKeyEnvironmentSource() = listOf(
            arguments("test_someclientkey", Environment.TEST, true),
            arguments("test_someclientkey", Environment.EUROPE, false),
            arguments("test_someclientkey", Environment.UNITED_STATES, false),
            arguments("test_someclientkey", Environment.AUSTRALIA, false),
            arguments("test_someclientkey", Environment.INDIA, false),
            arguments("test_someclientkey", Environment.APSE, false),
            arguments("live_someclientkey", Environment.EUROPE, true),
            arguments("live_someclientkey", Environment.UNITED_STATES, true),
            arguments("live_someclientkey", Environment.AUSTRALIA, true),
            arguments("live_someclientkey", Environment.INDIA, true),
            arguments("live_someclientkey", Environment.APSE, true),
        )

        @JvmStatic
        fun getTestEmailList() = listOf(
            arguments("simple@example.com", true),
            arguments("very.common@example.com", true),
            arguments("disposable.style.email.with+symbol@example.com", true),
            arguments("other.email-with-hyphen@example.com", true),
            arguments("fully-qualified-domain@example.com", true),
            arguments("user.name+tag+sorting@example.com", true),
            arguments("x@example.com", true),
            arguments("example-indeed@strange-example.com", true),
            arguments("test/test@test.com", true),
            arguments("example@s.example", true),
            arguments("\" \"@example.org", true),
            arguments("\"john..doe\"@example.org", true),
            arguments("mailhost!username@example.org", true),
            arguments(
                "\"very.(),:;<>[]\".VERY.\"very@\\ \"very\".unusual\"@strange.example.com",
                true
            ),
            arguments("user%example.com@example.org", true),
            arguments("user-@example.org", true),
            arguments("postmaster@[123.123.123.123]", true),
            arguments("john.smith@mohamed12.eldoheiri", true),
            arguments("john.smith@[12.2.255.45]", true),

            arguments("john.smith!#$%&'*+-/=?^_`{|}~@[12.2.255.45]", true),
            arguments("john!#$%&'*+-/=?^_`{|}~.smith@[12.2.255.45]", true),
            arguments(
                "john!#$%&'*+-/=?^_`{|}~.smith!#$%&'*+-/=?^_`{|}~" +
                    ".efwe!#$%&'*+-/=?^_`{|}~.weoihefw.!#$%&'*+-/=?^_`{|}~@[12.2.255.45]",
                true
            ),
            arguments("\" ewc429 (%($^)*_)*(&&R%$&$&^$#     \"@mohamed12.eldoheiri", true),

            // Domain part is an one or more alpha-numeric strings separated by a dot.
            arguments("john.smith@abc-12CB-FVCbh45.co", true),
            arguments(
                "john.smith@abc-12CB-FVCbh45-979HVU.uk.us.mrweew.co",
                true
            ),
            arguments(
                "john.smith@abc-12CB-FVCbh45-979HVU.uk-weoh-238y23-ewfioh234.us-wefwef.mrweew.co",
                true
            ),
            // Quoted local part can contain any character except for line terminators
            arguments("\"UYFG)O^R&|.:;(%&*]T*T*[&GIU\"@gmail.com", true),

            // INVALID EMAIL LIST
            arguments("Abc.example.com", false),
            arguments("A@b@c@example.com", false),
            arguments("a\"b(c)d,e:f;g<h>i[j\\k]l@example.com", false),
            arguments("just\"not\"right@example.com", false),
            arguments("this is\"not\\allowed@example.com", false),
            arguments("this\\ still\"not\\allowed@example.com", false),
            arguments("i_like_underscore@but_its_not_allowed_in_this_part.example.com", false),
            arguments("QA[icon]CHOCOLATE[icon]@test.com", false),

            // Domain part components must not start with a `-` or end with it
            arguments("john.smith@-12CB-FVCbh45.co", false),
            arguments("john.smith@abc-12CB-.co", false),

            // The `.` in the local part shouldnot be consecutive
            arguments("john..smith@mohamed12.eldoheiri", false),

            // The `.` in the local part shouldnot be at the beginning or end
            arguments(".john.smith@mohamed12.eldoheiri", false),
            arguments("john.smith.@mohamed12.eldoheiri", false),

            // Domain part shouldn't contain not latin characters
            arguments("あいうえお@example.com", false),

            // Domain last component must be alphabetical string.
            arguments("john.smith@abc-12CB-FVCbh45.co1", false),

            // The DomainPart before and after dot should be at least 2 characters
            arguments("john.smith@1.c", false),

            // Domain part can be an IP address of four of 1-3 long numbers separated by a dot.
            arguments("john.smith@[12.2.344.45].com", false),

            // The IP address is out of bounds
            arguments("john.smith@[12.2.344.45]", false),

            // The Domain part is not a valid IP address.
            arguments("john.smith@[12.2.344]", false),

            // Local part contains a "\n"
            arguments("\"UYFG)O^R&|\n.:;(%&*]T*T*[&GIU\"@gmail.com", false),

            /* Unquoted local part should't contain any of those characters
                "("   ")"   "["   "]"   "\"   ","   ";"   ":"   "\s"   "@"
             */
            arguments("UYFGO^R&%&*T .*T*&GIU@gmail.com", false),

            // Local part contains ";"
            arguments("UYFGO^R&%;&*T*T*&GIU@gmail.com", false),

            // Local part contains a space
            arguments("UYFGO^R&% &*T*T*&GIU@gmail.com", false),

            // Local part contains a ","
            arguments("UYFGO^R&%,&*T*T*&GIU@gmail.com", false),

            // Local part contains a ":"
            arguments("UYFGO^R&%:&*T*T*&GIU@gmail.com", false),

            // Local part contains a "@"
            arguments("UYFGO^R&%@&*T*T*&GIU@gmail.com", false),
        )
    }
}
