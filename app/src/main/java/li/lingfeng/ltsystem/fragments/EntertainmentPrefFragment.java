package li.lingfeng.ltsystem.fragments;

import android.os.Bundle;
import android.preference.Preference;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.DoubanMovieActivity;
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

        /*
        uncheckPreferenceByDisabledComponent(R.string.key_douban_movie_search,
                ComponentUtils.getFullAliasName(ProcessTextActivity.class, "DoubanMovie"));
        uncheckPreferenceByDisabledComponent(R.string.key_bilibili_search,
                ComponentUtils.getFullAliasName(ProcessTextActivity.class, "Bilibili"));*/
    }

    @PreferenceChange(prefs = R.string.key_douban_movie_url)
    private void enableDoubanMovieUrl(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_douban_movie_url, DoubanMovieActivity.class);
        } else {
            ComponentUtils.enableComponent(DoubanMovieActivity.class, enabled);
        }
    }

    /*@PreferenceChange(prefs = R.string.key_douban_movie_search)
    private void enableDoubanMovieSearch(Preference preference, boolean enabled) {
        ComponentUtils.enableComponent(ComponentUtils.getFullAliasName(ProcessTextActivity.class, "DoubanMovie"), enabled);
    }

    @PreferenceChange(prefs = R.string.key_bilibili_search)
    private void enableBilibiliSearch(Preference preference, boolean enabled) {
        ComponentUtils.enableComponent(ComponentUtils.getFullAliasName(ProcessTextActivity.class, "Bilibili"), enabled);
    }

    @PreferenceChange(prefs = R.string.key_bilibili_open_link_in_app)
    private void enableBilibiliOpenLinkInApp(Preference preference, boolean enabled) {
        ComponentUtils.enableComponent(BilibiliActivity.class, enabled);
    }*/
}
