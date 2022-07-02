package com.shid.mosquefinder.ui.main.views

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.shid.mosquefinder.R
import com.shid.mosquefinder.utils.SharePref

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

        var sharePref: SharePref? = null

        companion object {
            const val WORKER_TAG = "notification_worker"
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            sharePref = SharePref(requireContext())
            val switchPreference =
                findPreference<SwitchPreference>(requireContext().getString(R.string.pref_notification_key))
            switchPreference!!.isChecked = sharePref!!.loadSwitchState()
        }

        override fun onPreferenceTreeClick(preference: Preference): Boolean {
            val notificationKey = getString(R.string.pref_notification_key)
            /*val listKey = getString(R.string.pref_sort_key)*/
            if (preference.key == notificationKey) {
                val on =
                    (preference as SwitchPreference).isChecked
                sharePref!!.saveSwitchState(on)
                /*val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
                with (sharedPref.edit()) {
                    putBoolean(getString(R.string.pref_notification_key), on)
                    apply()
                }*/
            } /*else if (preference.key == listKey) {
                val listPreference = (preference as ListPreference).value
                sharePref!!.saveSort(listPreference)
                Log.d("Setting", listPreference)
            }*/
            return super.onPreferenceTreeClick(preference)
        }
    }
}