/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.ui.configuration

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.adyen.checkout.example.R
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.databinding.ActivitySettingsBinding
import com.adyen.checkout.example.ui.theme.NightTheme
import com.adyen.checkout.example.ui.theme.NightThemeRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ConfigurationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settingsContainer, ConfigurationFragment())
            .commit()
        supportActionBar?.setTitle(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    @AndroidEntryPoint
    class ConfigurationFragment : PreferenceFragmentCompat() {

        @Inject
        lateinit var keyValueStorage: KeyValueStorage

        @Inject
        internal lateinit var nightThemeRepository: NightThemeRepository

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            preferenceManager
                .findPreference<DropDownPreference>(requireContext().getString(R.string.night_theme_key))
                ?.setOnPreferenceChangeListener { _, newValue ->
                    nightThemeRepository.theme = NightTheme.findByPreferenceValue(newValue as String?)
                    true
                }

            /* This workaround is needed to display the default value of Merchant Account. We cannot set this value in
            `preferences.xml` because it's only available in the code and there is no "clean" way to set the default
            value programmatically. */
            preferenceManager
                .findPreference<EditTextPreference>(requireContext().getString(R.string.merchant_account_key))
                ?.text = keyValueStorage.getMerchantAccount()
        }
    }
}
