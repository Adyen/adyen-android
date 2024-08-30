/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/8/2024.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.adyen.checkout.example.R
import com.adyen.checkout.example.ui.compose.GenericDialog
import com.adyen.checkout.example.ui.compose.TextFieldDialog
import com.adyen.checkout.example.ui.compose.UIText
import com.adyen.checkout.example.ui.compose.stringFromUIText
import com.adyen.checkout.example.ui.theme.ExampleTheme

@Composable
internal fun SettingsScreen(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
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
        val uiState by viewModel.uiState.collectAsState()
        SettingsScreen(
            uiState = uiState,
            onItemClicked = viewModel::onItemClicked,
            onEditSettingConsumed = viewModel::onEditSettingConsumed,
            onTextSettingChanged = viewModel::onTextSettingChanged,
            onListSettingChanged = viewModel::onListSettingChanged,
            onSwitchSettingChanged = viewModel::onSwitchSettingChanged,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUIState,
    onItemClicked: (SettingsItem) -> Unit,
    onEditSettingConsumed: () -> Unit,
    onTextSettingChanged: (SettingsIdentifier, String) -> Unit,
    onListSettingChanged: (SettingsIdentifier, EditSettingsData.SingleSelectList.Item) -> Unit,
    onSwitchSettingChanged: (SettingsIdentifier, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsItemsList(
        settingsCategories = uiState.settingsCategories,
        onItemClicked = onItemClicked,
        onSwitchSettingChanged = onSwitchSettingChanged,
        modifier = modifier,
    )

    if (uiState.settingToEdit != null) {
        EditSettingDialog(
            settingToEdit = uiState.settingToEdit,
            onTextSettingChanged = {
                onTextSettingChanged(uiState.settingToEdit.identifier, it)
                onEditSettingConsumed()
            },
            onListSettingChanged = {
                onListSettingChanged(uiState.settingToEdit.identifier, it)
                onEditSettingConsumed()
            },
            onDismiss = {
                onEditSettingConsumed()
            },
        )
    }
}

@Composable
private fun SettingsItemsList(
    settingsCategories: List<SettingsCategory>,
    onItemClicked: (SettingsItem) -> Unit,
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
                        TextSettingsItem(settingsItem, onItemClicked)
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
private fun TextSettingsItem(
    settingsItem: SettingsItem.Text,
    onItemClicked: (SettingsItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClicked(settingsItem) }
            .padding(
                start = ExampleTheme.dimensions.grid_4,
                end = ExampleTheme.dimensions.grid_2,
                top = ExampleTheme.dimensions.grid_2,
                bottom = ExampleTheme.dimensions.grid_2,
            ),
    ) {
        // TODO: create separate style
        Text(
            text = stringResource(id = settingsItem.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            text = stringFromUIText(uiText = settingsItem.subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SwitchSettingsItem(
    settingsItem: SettingsItem.Switch,
    onSwitchSettingChanged: (SettingsIdentifier, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onSwitchSettingChanged(settingsItem.identifier, !settingsItem.checked)
            }
            .padding(
                start = ExampleTheme.dimensions.grid_4,
                end = ExampleTheme.dimensions.grid_2,
                top = ExampleTheme.dimensions.grid_2,
                bottom = ExampleTheme.dimensions.grid_2,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // TODO: create separate style
        Text(
            text = stringResource(id = settingsItem.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = modifier.weight(1f),
        )

        Switch(
            checked = settingsItem.checked,
            onCheckedChange = null,
        )
    }
}

@Composable
fun SettingsCategoryHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(
                start = ExampleTheme.dimensions.grid_4,
                end = ExampleTheme.dimensions.grid_2,
                top = ExampleTheme.dimensions.grid_2,
            )
            .fillMaxWidth(),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
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
    settingToEdit: EditSettingsData,
    onTextSettingChanged: (String) -> Unit,
    onListSettingChanged: (EditSettingsData.SingleSelectList.Item) -> Unit,
    onDismiss: () -> Unit,
) {
    when (settingToEdit) {
        is EditSettingsData.Text -> {
            EditSettingTextFieldDialog(
                settingToEdit = settingToEdit,
                onConfirm = onTextSettingChanged,
                onDismiss = onDismiss,
            )
        }

        is EditSettingsData.SingleSelectList -> {
            EditSettingListFieldDialog(
                settingToEdit = settingToEdit,
                onConfirm = onListSettingChanged,
                onDismiss = onDismiss,
            )
        }
    }
}

@Composable
private fun EditSettingTextFieldDialog(
    settingToEdit: EditSettingsData.Text,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val allowNumbersOnly = when (settingToEdit.inputType) {
        EditSettingsData.Text.InputType.STRING -> false
        EditSettingsData.Text.InputType.INTEGER -> true
    }

    val placeholder = settingToEdit.placeholder?.let { stringFromUIText(uiText = it) }

    TextFieldDialog(
        title = stringResource(id = settingToEdit.titleResId),
        content = settingToEdit.text,
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        allowNumbersOnly = allowNumbersOnly,
        placeholder = placeholder,
    )
}

@Composable
private fun EditSettingListFieldDialog(
    settingToEdit: EditSettingsData.SingleSelectList,
    onConfirm: (EditSettingsData.SingleSelectList.Item) -> Unit,
    onDismiss: () -> Unit,
) {
    GenericDialog(
        title = {
            Text(
                text = stringResource(id = settingToEdit.titleResId),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        content = {
            LazyColumn {
                items(settingToEdit.items) { item ->
                    Text(
                        modifier = Modifier
                            .clickable { onConfirm(item) }
                            .padding(vertical = ExampleTheme.dimensions.grid_2)
                            .fillMaxWidth(),
                        text = stringResource(id = item.textResId),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        },
        confirmButton = null,
        dismissButton = null,
        onDismiss = onDismiss,
    )
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
            onItemClicked = {},
            onEditSettingConsumed = {},
            onTextSettingChanged = { _, _ -> },
            onListSettingChanged = { _, _ -> },
            onSwitchSettingChanged = { _, _ -> },
        )
    }
}
