package li.lingfeng.ltsystem.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.widget.Toast;

import java.io.File;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.WeChatBrowserActivity;
import li.lingfeng.ltsystem.fragments.base.Extra;
import li.lingfeng.ltsystem.lib.PreferenceChange;
import li.lingfeng.ltsystem.lib.PreferenceClick;
import li.lingfeng.ltsystem.prefs.ActivityRequestCode;
import li.lingfeng.ltsystem.utils.ComponentUtils;
import li.lingfeng.ltsystem.utils.IOUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PermissionUtils;

/**
 * Created by smallville on 2017/1/15.
 */

public class CommunicationPrefFragment extends BasePrefFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_communication);
    }

    @PreferenceChange(prefs = R.string.key_wechat_browser, refreshAtStart = true)
    private void enableWeChatBrowser(SwitchPreference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_wechat_browser, WeChatBrowserActivity.class);
        } else {
            ComponentUtils.enableComponent(WeChatBrowserActivity.class, enabled);
        }
    }

    @PreferenceChange(prefs = R.string.key_qq_clear_background, refreshAtStart = true)
    private void enableQQClearBackground(SwitchPreference preference, boolean enabled) {
        findPreference(R.string.key_qq_clear_background_path).setEnabled(enabled);
    }

    @PreferenceClick(prefs = R.string.key_qq_clear_background_path)
    private void setQQClearBackgroundPath(Preference preference) {
        PermissionUtils.requestPermissions(getActivity(), new PermissionUtils.ResultCallback() {
            @Override
            public void onResult(boolean ok) {
                if (ok) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("image/*");
                    startActivityForResult(intent, ActivityRequestCode.QQ_CLEAR_IMAGE_CHOOSER);
                }
            }
        }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.QQ_CLEAR_IMAGE_CHOOSER) {
            Preference preference = findPreference(R.string.key_qq_clear_background_path);
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                String filepath = getQQClearBackgroundPath();
                if (filepath == null) {
                    Toast.makeText(getActivity(), R.string.error, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (IOUtils.saveUriToFile(uri, filepath)) {
                    Logger.i("New qq background is set.");
                    preference.setSummary(getString(R.string.pref_qq_clear_background_path_summary, filepath));
                } else {
                    Logger.e("New qq background set error.");
                    preference.setSummary(R.string.error);
                }
            } else {
                Logger.i("New qq background is not selected.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getQQClearBackgroundPath() {
        File dir = Environment.getExternalStoragePublicDirectory("Tencent");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Logger.e("Can't create dir " + dir.getAbsolutePath());
                return null;
            }
        }
        return dir.getAbsolutePath() + "/ltweaks_qq_background";
    }

    @PreferenceChange(prefs = R.string.key_qq_clear_background_path, refreshAtStart = true)
    private void refreshQQClearBackgroundPathSummary(Preference preference) {
        String filepath = getQQClearBackgroundPath();
        if (filepath == null) {
            return;
        }
        File file = new File(filepath);
        if (file.exists()) {
            preference.setSummary(getString(R.string.pref_qq_clear_background_path_summary, filepath));
        }
    }
}
