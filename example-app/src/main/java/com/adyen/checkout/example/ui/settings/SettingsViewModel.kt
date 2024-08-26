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
    private val settingsUIMapper: SettingsUIMapper,
    private val settingsEditor: SettingsEditor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState(emptyList()))
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            updateUIState {
                it.copy(
                    settingsCategories = settingsUIMapper.getSettingsCategories(),
                )
            }
        }
    }

    fun onItemClicked(item: SettingsItem) {
        val editSettingsData = settingsEditor.getEditSettingsData(item)
        updateUIState {
            it.copy(
                settingToEdit = editSettingsData,
            )
        }
    }

    fun onEditSettingConsumed() {
        updateUIState {
            it.copy(
                settingToEdit = null,
            )
        }
    }

    fun onSettingChanged(editSettingsData: EditSettingsData, newValue: String) {
        settingsEditor.editSetting(editSettingsData.identifier, newValue)
    }

    private fun updateUIState(block: (SettingsUIState) -> SettingsUIState) {
        _uiState.update(block)
    }
}
