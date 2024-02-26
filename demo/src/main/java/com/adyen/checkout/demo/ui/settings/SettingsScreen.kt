/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 21/2/2024.
 */

package com.adyen.checkout.demo.ui.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.ExposedDropdownMenuDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adyen.checkout.demo.data.api.model.Country
import com.adyen.checkout.demo.ui.MyStoreDemoViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(myStoreDemoViewModel: MyStoreDemoViewModel, onCountryChanged: (Country) -> Unit) {
    val state by myStoreDemoViewModel.myStoreState.collectAsState()
    var expanded by remember {
        mutableStateOf(false)
    }

    Column {
        Text(modifier = Modifier.padding(4.dp), text = "Country", fontWeight = FontWeight.Black, fontSize = 16.sp)
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                TextField(
                    value = state.country.title,
                    readOnly = true,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                )

                ExposedDropdownMenu(modifier = Modifier.fillMaxWidth(), expanded = expanded, onDismissRequest = { }) {
                    Country.entries.forEach {
                        DropdownMenuItem(
                            onClick = {
                                expanded = !expanded
                                onCountryChanged(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row {
                                Text(text = it.emoji)
                                Text(
                                    text = it.title,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
