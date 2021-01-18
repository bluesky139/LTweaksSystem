package li.lingfeng.ltsystem.fragments;

import android.os.Bundle;
import android.preference.Preference;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.JDActivity;
import li.lingfeng.ltsystem.fragments.base.Extra;
import li.lingfeng.ltsystem.lib.PreferenceChange;
import li.lingfeng.ltsystem.utils.ComponentUtils;

/**
 * Created by smallville on 2016/12/25.
 */

public class ShoppingPrefFragment extends BasePrefFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_shopping);
    }

    @PreferenceChange(prefs = R.string.key_jd_open_link_in_app, refreshAtStart = true)
    private void enableJdOpenLinkInApp(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_jd_open_link_in_app, JDActivity.class);
        } else {
            ComponentUtils.enableComponent(JDActivity.class, enabled);
        }
    }
}
