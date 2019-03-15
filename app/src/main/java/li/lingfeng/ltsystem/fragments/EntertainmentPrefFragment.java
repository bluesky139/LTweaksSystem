package li.lingfeng.ltsystem.fragments;

import android.os.Bundle;
import android.preference.Preference;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.BilibiliActivity;
import li.lingfeng.ltsystem.activities.ProcessTextActivity;
import li.lingfeng.ltsystem.fragments.base.Extra;
import li.lingfeng.ltsystem.lib.PreferenceChange;
import li.lingfeng.ltsystem.utils.ComponentUtils;

/**
 * Created by smallville on 2017/1/7.
 */

public class EntertainmentPrefFragment extends BasePrefFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_entertainment);
    }

    @PreferenceChange(prefs = R.string.key_douban_search, refreshAtStart = true)
    private void enableDoubanSearch(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_douban_search,
                    ComponentUtils.getFullAliasName(ProcessTextActivity.class, "Douban"));
        } else {
            ComponentUtils.enableComponent(ComponentUtils.getFullAliasName(ProcessTextActivity.class, "Douban"), enabled);
        }
    }

    @PreferenceChange(prefs = R.string.key_bilibili_search, refreshAtStart = true)
    private void enableBilibiliSearch(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_bilibili_search,
                    ComponentUtils.getFullAliasName(ProcessTextActivity.class, "Bilibili"));
        } else {
            ComponentUtils.enableComponent(ComponentUtils.getFullAliasName(ProcessTextActivity.class, "Bilibili"), enabled);
        }
    }

    @PreferenceChange(prefs = R.string.key_bilibili_open_link_in_app, refreshAtStart = true)
    private void enableBilibiliOpenLinkInApp(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_bilibili_open_link_in_app, BilibiliActivity.class);
        } else {
            ComponentUtils.enableComponent(BilibiliActivity.class, enabled);
        }
    }
}
