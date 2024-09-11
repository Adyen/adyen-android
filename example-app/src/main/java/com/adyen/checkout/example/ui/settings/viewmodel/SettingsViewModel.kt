/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

package com.adyen.checkout.example.ui.settings.viewmodel

import androidx.lifecycle.ViewModel
import com.adyen.checkout.example.ui.settings.model.EditSettingDialogData
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.settings.model.SettingsItem
import com.adyen.checkout.example.ui.settings.model.SettingsUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val settingsUIMapper: SettingsUIMapper,
    private val settingsEditor: SettingsEditor,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState(emptyList()))
    val uiState: StateFlow<SettingsUIState> = _uiState.asStateFlow()

    init {
        fetchSettings()
    }

    fun onItemClicked(item: SettingsItem) {
        val editSettingDialogData = settingsUIMapper.getEditSettingDialogData(item)
        updateUIState {
            it.copy(
                editSettingDialogData = editSettingDialogData,
            )
        }
    }

    fun onTextSettingChanged(identifier: SettingsIdentifier, newValue: String) {
        onEditSettingDismissed()
        settingsEditor.editSetting(identifier, newValue)
        fetchSettings()
    }

    fun onListSettingChanged(identifier: SettingsIdentifier, selectedItem: EditSettingDialogData.SingleSelectList.Item) {
        onEditSettingDismissed()
        settingsEditor.editSetting(identifier, selectedItem)
        fetchSettings()
    }

    fun onSwitchSettingChanged(identifier: SettingsIdentifier, newValue: Boolean) {
        onEditSettingDismissed()
        settingsEditor.editSetting(identifier, newValue)
        fetchSettings()
    }

    fun onEditSettingDismissed() {
        updateUIState {
            it.copy(
                editSettingDialogData = null,
            )
        }
    }

    private fun fetchSettings() {
        updateUIState {
            it.copy(
                settingsCategories = settingsUIMapper.getSettingsCategories(),
            )
        }
    }

    private fun updateUIState(block: (SettingsUIState) -> SettingsUIState) {
        _uiState.update(block)
    }
}
