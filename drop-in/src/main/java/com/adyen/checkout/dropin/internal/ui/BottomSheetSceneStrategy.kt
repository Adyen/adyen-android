/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import com.adyen.checkout.ui.internal.theme.CheckoutThemeProvider
import com.adyen.checkout.ui.internal.theme.Dimensions

internal class BottomSheetSceneStrategy<T : Any> : SceneStrategy<T> {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val lastEntry = entries.lastOrNull()
        return (lastEntry?.metadata?.get(BOTTOM_SHEET_KEY) as? ModalBottomSheetProperties?)?.let { properties ->
            val previousEntries = entries.dropLast(1)
            BottomSheetScene(
                key = lastEntry.contentKey,
                previousEntries = previousEntries,
                overlaidEntries = previousEntries,
                entry = lastEntry,
                modalBottomSheetProperties = properties,
                onBack = onBack,
            )
        }
    }

    companion object {
        private const val BOTTOM_SHEET_KEY = "bottom_sheet"

        @OptIn(ExperimentalMaterial3Api::class)
        fun bottomSheet(
            modalBottomSheetProperties: ModalBottomSheetProperties = ModalBottomSheetProperties()
        ) = mapOf(BOTTOM_SHEET_KEY to modalBottomSheetProperties)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private class BottomSheetScene<T : Any>(
    override val overlaidEntries: List<NavEntry<T>>,
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
    private val modalBottomSheetProperties: ModalBottomSheetProperties,
    private val onBack: () -> Unit,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable () -> Unit = {
        ModalBottomSheet(
            onDismissRequest = onBack,
            properties = modalBottomSheetProperties,
            containerColor = CheckoutThemeProvider.colors.background,
            dragHandle = null,
        ) {
            Spacer(Modifier.size(Dimensions.Spacing.Small))
            entry.Content()
            Spacer(Modifier.size(Dimensions.Spacing.Large))
        }
    }
}
