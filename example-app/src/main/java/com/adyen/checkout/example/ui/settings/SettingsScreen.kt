/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/8/2024.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.settings

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
        settingsItems = uiState.settingsItems,
        modifier = modifier,
    )
}

@Composable
private fun SettingsItemsList(
    settingsItems: List<SettingsItem>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = settingsItems) { settingsItem ->
            when (settingsItem) {
                is SettingsItem.Text -> TextSettingsItem(settingsItem)
                is SettingsItem.Switch -> SwitchSettingsItem(settingsItem)
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
            .padding(horizontal = ExampleTheme.dimensions.grid_4, vertical = ExampleTheme.dimensions.grid_1_5),
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
            .padding(horizontal = ExampleTheme.dimensions.grid_4, vertical = ExampleTheme.dimensions.grid_1_5),
    ) {
        // TODO: create separate style
        Text(
            text = stringResource(id = settingsItem.titleResId),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )

        Switch(
            checked = settingsItem.checked,
            onCheckedChange = null, // TODO: implement
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    ExampleTheme {
        SettingsScreen(
            uiState = SettingsUIState(
                listOf(
                    SettingsItem.Text(R.string.shopper_reference_title, "shopper_reference_123"),
                    SettingsItem.Text(R.string.amount_value_title, "1337"),
                    SettingsItem.Switch(R.string.card_installment_show_amount_title, true),
                    SettingsItem.Text(R.string.card_address_form_title, "Full address"),
                    SettingsItem.Switch(R.string.remove_stored_payment_method_title, false),
                ),
            ),
        )
    }
}
