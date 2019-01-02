package li.lingfeng.ltsystem.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ListCheckActivity;
import li.lingfeng.ltsystem.activities.QrCodeActivity;
import li.lingfeng.ltsystem.activities.SelectableTextActivity;
import li.lingfeng.ltsystem.fragments.sub.system.TextActionDataProvider;
import li.lingfeng.ltsystem.lib.PreferenceChange;
import li.lingfeng.ltsystem.lib.PreferenceClick;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ComponentUtils;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.PermissionUtils;

/**
 * Created by smallville on 2017/1/4.
 */

public class SystemPrefFragment extends BasePrefFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_system);

        uncheckPreferenceByDisabledComponent(R.string.key_text_selectable_text, SelectableTextActivity.class);
        /*uncheckPreferenceByDisabledComponent(R.string.key_system_share_qrcode_scan, QrCodeActivity.class);
        uncheckPreferenceByDisabledComponent(R.string.key_system_share_image_search, ImageSearchActivity.class);*/
    }

    @PreferenceChange(prefs = R.string.key_text_selectable_text)
    private void enableSelectableText(Preference preference, boolean enabled) {
        ComponentUtils.enableComponent(SelectableTextActivity.class, enabled);
    }

    @PreferenceClick(prefs = R.string.key_text_actions)
    private void manageTextActions(Preference preference) {
        ListCheckActivity.create(getActivity(), TextActionDataProvider.class);
    }

    @PreferenceClick(prefs = R.string.key_youdao_quick_query_shortcut)
    private void youdaoQuckQueryShortcut(Preference preference) {
        Intent pending = new Intent(Intent.ACTION_QUICK_VIEW);
        pending.setClassName(PackageNames.YOUDAO_DICT, "com.youdao.dict.activity.QuickDictQueryActivity");
        pending.putExtra("isEditable", true);
        pending.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        Context context = ContextUtils.createPackageContext(PackageNames.YOUDAO_DICT);
        ShortcutInfo info = new ShortcutInfo.Builder(getActivity(), "Youdao Dict Quick Query")
                .setIcon(Icon.createWithResource(getActivity(), R.drawable.youdao_dict))
                .setShortLabel(ContextUtils.getString("app_name", context))
                .setIntent(pending)
                .build();

        ShortcutManager shortcutManager = (ShortcutManager) getActivity().getSystemService(Context.SHORTCUT_SERVICE);
        shortcutManager.requestPinShortcut(info, null);
    }

    @PreferenceChange(prefs = R.string.key_system_share_qrcode_scan)
    private void systemShareQrcodeScan(final SwitchPreference preference, boolean enabled) {
        if (enabled) {
            PermissionUtils.requestPermissions(getActivity(), new PermissionUtils.ResultCallback() {
                @Override
                public void onResult(boolean ok) {
                    if (ok) {
                        ComponentUtils.enableComponent(QrCodeActivity.class, true);
                    } else {
                        preference.setChecked(false);
                    }
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            ComponentUtils.enableComponent(QrCodeActivity.class, false);
        }
    }

/*    @PreferenceChange(prefs = R.string.key_system_share_image_search)
    private void systemShareImageSearch(final SwitchPreference preference, boolean enabled) {
        if (enabled) {
            PermissionUtils.requestPermissions(getActivity(), new PermissionUtils.ResultCallback() {
                @Override
                public void onResult(boolean ok) {
                    if (ok) {
                        ComponentUtils.enableComponent(ImageSearchActivity.class, true);
                    } else {
                        preference.setChecked(false);
                    }
                }
            }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            ComponentUtils.enableComponent(ImageSearchActivity.class, false);
        }
    }

    @PreferenceClick(prefs = R.string.key_system_share_filter)
    private void systemShareFilter(Preference preference) {
        ListCheckActivity.create(getActivity(), ShareFilterDataProvider.class);
    }

    @PreferenceClick(prefs = R.string.key_prevent_running_set_list)
    private void setPreventList(Preference preference) {
        ListCheckActivity.create(getActivity(), PreventListDataProvider.class);
    }

    @PreferenceChange(prefs = R.string.key_shadowsocks_primary_dns, refreshAtStart = true)
    private void setShadowsocksPrimaryDns(EditTextPreference preference, String value, Extra extra) {
        String[] dnsArray = StringUtils.split(value, ',');
        StringBuilder summary = new StringBuilder(getString(R.string.pref_shadowsocks_primary_dns_summary));
        for (String dns : dnsArray) {
            dns = StringUtils.strip(dns, " ");
            summary.append("\n");
            summary.append(dns);
        }
        preference.setSummary(summary);
    }

    @PreferenceChange(prefs = R.string.key_quick_settings_tile_4g3g, refreshAtStart = true)
    private void tile4G3G(SwitchPreference preference, boolean enabled, Extra extra) {
        ListPreference pref4g = findListPreference(R.string.key_quick_settings_tile_4g);
        ListPreference pref3g = findListPreference(R.string.key_quick_settings_tile_3g);
        pref4g.setEnabled(enabled);
        pref3g.setEnabled(enabled);

        if (extra.refreshAtStart) {
            Logger.d("Try get network types.");
            try {
                Context context = getActivity().createPackageContext(PackageNames.ANDROID_SETTINGS, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
                Class cls = Class.forName(ClassNames.RADIO_INFO, true, context.getClassLoader());
                Field field = cls.getDeclaredField("mPreferredNetworkLabels");
                field.setAccessible(true);
                String[] types = (String[]) field.get(Modifier.isStatic(field.getModifiers()) ? null : cls.newInstance());
                setTypesForListPreference(types, pref4g);
                setTypesForListPreference(types, pref3g);
            } catch (Throwable e) {
                Logger.e("Failed to get network types, " + e);
                Logger.stackTrace(e);
                pref4g.setEnabled(false);
                pref3g.setEnabled(false);
                preference.setEnabled(false);
                preference.setChecked(false);
                preference.setSummary(R.string.not_supported);
            }
        }
    }

    private void setTypesForListPreference(String[] types, ListPreference listPreference) {
        listPreference.setEntries(types);
        String[] entryValues = new String[types.length];
        for (int i = 0; i < types.length; ++i) {
            entryValues[i] = String.valueOf(i);
        }
        listPreference.setEntryValues(entryValues);
        listPreference.setSummary("%s");
    }

    @PreferenceChange(prefs = R.string.key_quick_settings_tile_set_preconfigured_brightness, refreshAtStart = true)
    private void tileSetPreconfiguredBrightness(SwitchPreference preference, boolean enabled, Extra extra) {
        findPreference(R.string.key_quick_settings_tile_preconfigured_brightness).setEnabled(enabled);
    }

    @PreferenceChange(prefs = R.string.key_quick_settings_tile_preconfigured_brightness, refreshAtStart = true)
    private boolean tilePrecongiruedBrightness(EditTextPreference preference, String intValue, Extra extra) {
        if (intValue.isEmpty()) {
            preference.setSummary("");
            return true;
        }
        int value = Integer.parseInt(intValue);
        if (value > 0 && value < 255) {
            preference.setSummary(intValue);
            return true;
        } else {
            return false;
        }
    }

    @PreferenceClick(prefs = R.string.key_trust_agent_wifi_aps)
    private void setSmartLockWifiList(Preference preference) {
        KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (keyguardManager.isKeyguardSecure()) {
            Intent keyguardIntent = keyguardManager.createConfirmDeviceCredentialIntent(getString(R.string.pref_trust_agent_wifi), "");
            startActivityForResult(keyguardIntent, ActivityRequestCode.KEYGUARD);
        } else {
            Toast.makeText(getActivity(), R.string.secure_lock_screen_not_setup, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ActivityRequestCode.KEYGUARD) {
            if (resultCode == Activity.RESULT_OK) {
                Intent intent = new Intent(getActivity(), TrustAgentWifiSettings.class);
                getActivity().startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @PreferenceClick(prefs = R.string.key_solid_explorer_url_replacers)
    private void setupSolidExplorerUrlReplacer(Preference preference) {
        startActivity(new Intent(getActivity(), SolidExplorerUrlReplacerSettings.class));
    }

    @PreferenceChange(prefs = R.string.key_lineage_os_live_display_time, refreshAtStart = true)
    private void customizeLineageOSLiveDisplayTime(SwitchPreference preference, boolean enabled) {
        enablePreference(R.string.key_lineage_os_live_display_time_sunrise, enabled);
        enablePreference(R.string.key_lineage_os_live_display_time_sunset, enabled);
    }

    @PreferenceChange(prefs = R.string.key_display_min_brightness, refreshAtStart = true)
    private boolean setMinBrightness(EditTextPreference preference, String intValue, Extra extra) {
        if (extra.refreshAtStart) {
            PowerManager powerManager = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
            try {
                int defaultMinBrightness = (int) PowerManager.class.getDeclaredMethod("getMinimumScreenBrightnessSetting").invoke(powerManager);
                preference.setDialogTitle(getString(R.string.pref_display_min_brightness_dialog_title, defaultMinBrightness));
            } catch (Throwable e) {
                Logger.e("Can't get default min brightness, " + e);
            }
        }
        if (intValue.isEmpty()) {
            preference.setSummary("");
            return true;
        }
        int value = Integer.parseInt(intValue);
        if (value > 0 && value < 255) {
            preference.setSummary(intValue);
            return true;
        } else {
            return false;
        }
    }*/
}
