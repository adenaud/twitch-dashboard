package com.anthonydenaud.twitchdashboard.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.anthonydenaud.twitchdashboard.R;

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
