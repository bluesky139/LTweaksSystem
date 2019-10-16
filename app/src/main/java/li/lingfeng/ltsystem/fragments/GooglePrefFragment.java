package li.lingfeng.ltsystem.fragments;

import android.os.Bundle;
import android.preference.Preference;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ChromeIncognitoActivity;
import li.lingfeng.ltsystem.activities.GoMarketActivity;
import li.lingfeng.ltsystem.fragments.base.Extra;
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
    }

    @PreferenceChange(prefs = R.string.key_google_play_view_in_coolapk, refreshAtStart = true)
    private void enableGoMarket(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_google_play_view_in_coolapk, GoMarketActivity.class);
        } else {
            ComponentUtils.enableComponent(GoMarketActivity.class, enabled);
        }
    }

    @PreferenceChange(prefs = R.string.key_chrome_incognito_search, refreshAtStart = true)
    private void enableChromeIncognitoSearch(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_chrome_incognito_search, ChromeIncognitoActivity.class);
        } else {
            ComponentUtils.enableComponent(ChromeIncognitoActivity.class, enabled);
        }
    }

    /*@PreferenceChange(prefs = R.string.key_youtube_set_quality, refreshAtStart = true)
    private void setYoutubeQuality(ListPreference preference, String intValue) {
        int index = ArrayUtils.indexOf(getResources().getStringArray(R.array.youtube_quality_int), intValue);
        preference.setSummary(getResources().getStringArray(R.array.youtube_quality_string)[index]);
    }*/
}
