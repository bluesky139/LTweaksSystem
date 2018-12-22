package li.lingfeng.ltsystem.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import org.apache.commons.lang3.ArrayUtils;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ChromeIncognitoActivity;
import li.lingfeng.ltsystem.lib.PreferenceChange;
import li.lingfeng.ltsystem.utils.ComponentUtils;

/**
 * Created by smallville on 2016/12/24.
 */

public class GooglePrefFragment extends BasePrefFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_google);

        uncheckPreferenceByDisabledComponent(R.string.key_chrome_incognito_search, ChromeIncognitoActivity.class);
    }

    @PreferenceChange(prefs = R.string.key_chrome_incognito_search)
    private void enableChromeIncognitoSearch(Preference preference, boolean enabled) {
        ComponentUtils.enableComponent(ChromeIncognitoActivity.class, enabled);
    }

    @PreferenceChange(prefs = R.string.key_youtube_set_quality, refreshAtStart = true)
    private void setYoutubeQuality(ListPreference preference, String intValue) {
        int index = ArrayUtils.indexOf(getResources().getStringArray(R.array.youtube_quality_int), intValue);
        preference.setSummary(getResources().getStringArray(R.array.youtube_quality_string)[index]);
    }
}
