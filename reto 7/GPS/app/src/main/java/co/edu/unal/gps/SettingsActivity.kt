package co.edu.unal.gps

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val searchRadiusPreference = findPreference<EditTextPreference>("search_radius")
            searchRadiusPreference?.summaryProvider = Preference.SummaryProvider<EditTextPreference> { preference ->
                val text = preference.text
                if (text.isNullOrEmpty()) {
                    "Not set"
                } else {
                    "Current radius: $text km"
                }
            }
        }
    }
}