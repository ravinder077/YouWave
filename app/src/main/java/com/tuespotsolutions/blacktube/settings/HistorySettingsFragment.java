package com.tuespotsolutions.blacktube.settings;

import android.os.Bundle;

import com.tuespotsolutions.blacktube.R;

public class HistorySettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.history_settings);
    }
}
