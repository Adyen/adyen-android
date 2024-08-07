/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/8/2024.
 */

package com.adyen.checkout.example

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.textfield.TextInputEditText
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.modifierprovider.withoutAbstractModifier
import com.lemonappdev.konsist.api.ext.list.withPackage
import com.lemonappdev.konsist.api.ext.list.withoutAllConstructors
import com.lemonappdev.konsist.api.ext.list.withoutExternalParentOf
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.jupiter.api.Test

internal class ClassVisibilityTest {

    @Test
    fun `when class is in internal package, then visibility should be internal`() {
        Konsist
            .scopeFromProduction()
            .classes(includeNested = false)
            .withPackage("..internal..")
            // Exclude abstract classes because @RestrictTo carries over to their children.
            .withoutAbstractModifier()
            // Exclude Views because they have to be public to be used in XML.
            .withoutExternalParentOf(
                AppCompatImageView::class,
                ConstraintLayout::class,
                LinearLayout::class,
                TextInputEditText::class,
                ViewGroup::class,
                indirectParents = true,
            )
            // Exclude classes that are exposed publicly but can only be instantiated internally (like ComponentProviders).
            .withoutAllConstructors {
                it.hasInternalModifier || it.hasPrivateModifier || it.hasAnnotationOf(RestrictTo::class)
            }
            .assertTrue {
                it.hasInternalModifier || it.hasPrivateModifier || it.hasAnnotationOf(RestrictTo::class)
            }
    }
}
