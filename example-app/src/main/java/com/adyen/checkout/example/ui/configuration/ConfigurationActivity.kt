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
import androidx.preference.PreferenceFragmentCompat
import com.adyen.checkout.example.R
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
        internal lateinit var nightThemeRepository: NightThemeRepository

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            preferenceManager.preferenceScreen
                .findPreference<DropDownPreference>(requireContext().getString(R.string.night_theme_title))
                ?.setOnPreferenceChangeListener { _, newValue ->
                    nightThemeRepository.theme = NightTheme.findByPreferenceValue(newValue as String?)
                    true
                }
        }
    }
}
