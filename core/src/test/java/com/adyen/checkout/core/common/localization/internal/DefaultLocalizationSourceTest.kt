/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/9/2026.
 */

package com.adyen.checkout.core.common.localization.internal

import android.content.Context
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.whenever

// The Context is mocked instead of using Robolectric because string resources are not packaged into unit
// tests in this project, so the resources can only be told apart by their id here.
@ExtendWith(MockitoExtension::class)
internal class DefaultLocalizationSourceTest(
    @param:Mock private val context: Context,
) {

    // The compiler already guarantees every key is mapped, but not that it is mapped to a resource of its own.
    // Reusing one resource for two keys silently ties their copy together and makes one of them impossible to
    // override on its own, which is the mistake a new key is most likely to introduce.
    @Test
    fun `when every key is resolved, then no two keys share a string resource`() {
        whenever(context.getString(any())) doAnswer { invocation ->
            invocation.getArgument<Int>(0).toString()
        }
        val source = DefaultLocalizationSource()

        val keysByResource = CheckoutLocalizationKey.entries.groupBy { source.getString(context, it) }

        assertEquals(emptyList<List<CheckoutLocalizationKey>>(), keysByResource.values.filter { it.size > 1 })
    }
}
