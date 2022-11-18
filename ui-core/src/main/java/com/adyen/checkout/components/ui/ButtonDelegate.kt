package com.adyen.checkout.components.ui

import kotlinx.coroutines.flow.Flow

// TODO docs
interface ButtonDelegate {

    val submitFlow: Flow<Unit>

    fun onSubmit()
}
