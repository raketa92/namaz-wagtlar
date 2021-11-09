package com.github.raketa92.namazwagtlar.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.github.raketa92.namazwagtlar.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val reminder_pref = findPreference<SwitchPreferenceCompat>("enable_reminder")
        reminder_pref!!.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {preference, newValue ->
            findPreference<Preference>("reminder_tip")?.isVisible = newValue == true
            true
        }
    super.onViewCreated(view, savedInstanceState)
    }
}