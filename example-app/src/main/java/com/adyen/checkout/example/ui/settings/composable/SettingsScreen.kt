/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/9/2024.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.settings.composable

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.adyen.checkout.example.R
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.settings.model.EditSettingData
import com.adyen.checkout.example.ui.settings.model.EditSettingDialogData
import com.adyen.checkout.example.ui.settings.model.SettingsCategory
import com.adyen.checkout.example.ui.settings.model.SettingsIdentifier
import com.adyen.checkout.example.ui.settings.model.SettingsItem
import com.adyen.checkout.example.ui.settings.model.SettingsUIState
import com.adyen.checkout.example.ui.settings.viewmodel.SettingsViewModel
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun SettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    SettingsScreen(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onTextSettingClicked = viewModel::onItemClicked,
        onEditSettingDismissed = viewModel::onEditSettingDismissed,
        onSettingEdited = viewModel::onSettingEdited,
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUIState,
    onBackPressed: () -> Unit,
    onTextSettingClicked: (SettingsItem) -> Unit,
    onEditSettingDismissed: () -> Unit,
    onSettingEdited: (EditSettingData) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.settings_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        SettingsItemsList(
            modifier = Modifier.padding(innerPadding),
            settingsCategories = uiState.settingsCategories,
            onTextSettingClicked = onTextSettingClicked,
            onSwitchSettingChanged = { identifier, newValue ->
                onSettingEdited(EditSettingData.Switch(identifier, newValue))
            },
        )

        if (uiState.editSettingDialogData != null) {
            EditSettingDialog(
                settingToEdit = uiState.editSettingDialogData,
                onTextSettingChanged = {
                    onSettingEdited(EditSettingData.Text(uiState.editSettingDialogData.identifier, it))
                },
                onListSettingChanged = {
                    onSettingEdited(EditSettingData.ListItem(uiState.editSettingDialogData.identifier, it.value))
                },
                onDismiss = {
                    onEditSettingDismissed()
                },
            )
        }
    }
}

@Composable
private fun SettingsItemsList(
    settingsCategories: List<SettingsCategory>,
    onTextSettingClicked: (SettingsItem) -> Unit,
    onSwitchSettingChanged: (SettingsIdentifier, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        settingsCategories.forEachIndexed { index, category ->
            item {
                SettingsCategoryHeader(title = stringResource(id = category.titleResId))
            }

            items(items = category.settingsItems) { settingsItem ->
                when (settingsItem) {
                    is SettingsItem.Text -> {
                        TextSettingsItem(settingsItem, onTextSettingClicked)
                    }

                    is SettingsItem.Switch -> SwitchSettingsItem(settingsItem, onSwitchSettingChanged)
                }
            }

            if (index != settingsCategories.size - 1) {
                item {
                    SettingsDivider()
                }
            }
        }
    }
}

@Composable
fun SettingsCategoryHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier
            .padding(
                start = ExampleTheme.dimensions.grid_2,
                end = ExampleTheme.dimensions.grid_2,
                top = ExampleTheme.dimensions.grid_2,
            )
            .fillMaxWidth(),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(
        modifier = modifier
            .padding(
                bottom = ExampleTheme.dimensions.grid_1,
            ),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun EditSettingDialog(
    settingToEdit: EditSettingDialogData,
    onTextSettingChanged: (String) -> Unit,
    onListSettingChanged: (EditSettingDialogData.SingleSelectList.Item) -> Unit,
    onDismiss: () -> Unit,
) {
    when (settingToEdit) {
        is EditSettingDialogData.Text -> {
            EditSettingTextFieldDialog(
                settingToEdit = settingToEdit,
                onConfirm = onTextSettingChanged,
                onDismiss = onDismiss,
            )
        }

        is EditSettingDialogData.SingleSelectList -> {
            EditSettingListFieldDialog(
                settingToEdit = settingToEdit,
                onConfirm = onListSettingChanged,
                onDismiss = onDismiss,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ExampleTheme {
        SettingsScreen(
            uiState = SettingsUIState(
                listOf(
                    SettingsCategory(
                        R.string.settings_category_integration_parameters,
                        listOf(
                            SettingsItem.Text(
                                identifier = SettingsIdentifier.MERCHANT_ACCOUNT,
                                titleResId = R.string.settings_title_shopper_reference,
                                subtitle = UIText.String("shopper_reference_123"),
                            ),
                        ),
                    ),
                    SettingsCategory(
                        R.string.settings_category_shopper_information,
                        listOf(
                            SettingsItem.Text(
                                identifier = SettingsIdentifier.AMOUNT,
                                titleResId = R.string.settings_title_amount,
                                subtitle = UIText.String("1337"),
                            ),
                            SettingsItem.Switch(
                                identifier = SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT,
                                titleResId = R.string.settings_title_card_installment_show_amount,
                                checked = true,
                            ),
                        ),
                    ),
                    SettingsCategory(
                        R.string.settings_category_card,
                        listOf(
                            SettingsItem.Text(
                                identifier = SettingsIdentifier.ADDRESS_MODE,
                                titleResId = R.string.settings_title_address_mode,
                                subtitle = UIText.String("Full address"),
                            ),
                            SettingsItem.Switch(
                                identifier = SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD,
                                titleResId = R.string.settings_title_remove_stored_payment_method,
                                checked = false,
                            ),
                        ),
                    ),
                ),
            ),
            onBackPressed = {},
            onTextSettingClicked = {},
            onEditSettingDismissed = {},
            onSettingEdited = { },
        )
    }
}
