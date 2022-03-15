package com.adyen.checkout.example.ui.main

internal sealed class ComponentItem {

    abstract val text: String

    data class Title(override val text: String) : ComponentItem()

    sealed class Entry(override val text: String) : ComponentItem() {
        object DropIn : Entry("Start")
        object Card : Entry("Card")
    }
}
