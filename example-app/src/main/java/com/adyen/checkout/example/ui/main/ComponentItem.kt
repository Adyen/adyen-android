package com.adyen.checkout.example.ui.main

import androidx.annotation.StringRes
import com.adyen.checkout.example.R

internal sealed class ComponentItem {

    abstract val stringResource: Int

    data class Title(@StringRes override val stringResource: Int) : ComponentItem()

    sealed class Entry(@StringRes override val stringResource: Int) : ComponentItem() {
        object DropIn : Entry(R.string.drop_in_entry)
        object DropInWithSession : Entry(R.string.drop_in_with_session_entry)
        object DropInWithCustomSession : Entry(R.string.drop_in_with_session_custom_entry)
        object Card : Entry(R.string.card_component_entry)
    }
}
