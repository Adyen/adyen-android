/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.KeyValueStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val keyValueStorage: KeyValueStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState(emptyList()))
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateUIState {
                SettingsUIState(
                    settingsItems = getSettingsItems(),
                )
            }
        }
    }

    private fun getSettingsItems(): List<SettingsItem> {
        val merchantSetting = SettingsItem.Text(
            titleResId = R.string.merchant_account_title,
            subtitle = keyValueStorage.getMerchantAccount(),
        )
        return listOf(
            merchantSetting,
        )
    }


    private fun updateUIState(block: (SettingsUIState) -> SettingsUIState) {
        _uiState.update(block)
    }
}
