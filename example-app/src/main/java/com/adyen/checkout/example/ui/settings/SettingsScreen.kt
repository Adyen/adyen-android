/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/8/2024.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.settings

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.adyen.checkout.example.R
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
                title = { Text(text = stringResource(id = R.string.settings)) },
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
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUIState,
    modifier: Modifier = Modifier,
) {
    SettingsItemsList(
        settingsCategories = uiState.settingsCategories,
        modifier = modifier,
    )
}

@Composable
private fun SettingsItemsList(
    settingsCategories: List<SettingsCategory>,
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
                        TextSettingsItem(settingsItem)
                    }

                    is SettingsItem.Switch -> SwitchSettingsItem(settingsItem)
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
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
            text = settingsItem.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SwitchSettingsItem(
    settingsItem: SettingsItem.Switch,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
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
            modifier = modifier.weight(1f),
        )

        Switch(
            checked = settingsItem.checked,
            onCheckedChange = null, // TODO: implement
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

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ExampleTheme {
        SettingsScreen(
            uiState = SettingsUIState(
                listOf(
                    SettingsCategory(
                        R.string.merchant_information,
                        listOf(
                            SettingsItem.Text(
                                identifier = SettingsIdentifier.MERCHANT_ACCOUNT,
                                titleResId = R.string.shopper_reference_title,
                                subtitle = "shopper_reference_123",
                            ),
                        ),
                    ),
                    SettingsCategory(
                        R.string.shopper_information,
                        listOf(
                            SettingsItem.Text(
                                identifier = SettingsIdentifier.AMOUNT,
                                titleResId = R.string.amount_value_title,
                                subtitle = "1337",
                            ),
                            SettingsItem.Switch(
                                identifier = SettingsIdentifier.SHOW_INSTALLMENT_AMOUNT,
                                titleResId = R.string.card_installment_show_amount_title,
                                checked = true,
                            ),
                        ),
                    ),
                    SettingsCategory(
                        R.string.other_payment_methods_settings_title,
                        listOf(
                            SettingsItem.Text(
                                identifier = SettingsIdentifier.ADDRESS_MODE,
                                titleResId = R.string.card_address_form_title,
                                subtitle = "Full address",
                            ),
                            SettingsItem.Switch(
                                identifier = SettingsIdentifier.REMOVE_STORED_PAYMENT_METHOD,
                                titleResId = R.string.remove_stored_payment_method_title,
                                checked = false,
                            ),
                        ),
                    ),
                ),
            ),
        )
    }
}
