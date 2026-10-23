package es.upm.miw.bantumi.ui.fragmentos;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import es.upm.miw.bantumi.R;

public class BantumiPrefsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}