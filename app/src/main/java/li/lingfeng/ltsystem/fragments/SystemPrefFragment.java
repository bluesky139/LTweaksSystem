package li.lingfeng.ltsystem.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.activities.ImageSearchActivity;
import li.lingfeng.ltsystem.activities.ListCheckActivity;
import li.lingfeng.ltsystem.activities.ProcessTextActivity;
import li.lingfeng.ltsystem.activities.QrCodeActivity;
import li.lingfeng.ltsystem.activities.SelectableTextActivity;
import li.lingfeng.ltsystem.fragments.base.Extra;
import li.lingfeng.ltsystem.fragments.sub.system.AppListProvider;
import li.lingfeng.ltsystem.fragments.sub.system.ShareFilterDataProvider;
import li.lingfeng.ltsystem.fragments.sub.system.TextActionDataProvider;
import li.lingfeng.ltsystem.lib.PreferenceChange;
import li.lingfeng.ltsystem.lib.PreferenceClick;
import li.lingfeng.ltsystem.lib.PreferenceLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.services.CellLocationService;
import li.lingfeng.ltsystem.services.CopyToShareService;
import li.lingfeng.ltsystem.utils.ComponentUtils;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PermissionUtils;
import li.lingfeng.ltsystem.utils.ReflectUtils;

/**
 * Created by smallville on 2017/1/4.
 */

public class SystemPrefFragment extends BasePrefFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_system);
    }

    @PreferenceChange(prefs = R.string.key_text_selectable_text, refreshAtStart = true)
    private void enableSelectableText(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_text_selectable_text, SelectableTextActivity.class);
        } else {
            ComponentUtils.enableComponent(SelectableTextActivity.class, enabled);
        }
    }

    @PreferenceClick(prefs = R.string.key_text_actions)
    private void manageTextActions(Preference preference) {
        ListCheckActivity.create(getActivity(), TextActionDataProvider.class);
    }

    @PreferenceChange(prefs = R.string.key_selection_action_mode_original, refreshAtStart = true)
    private void enableOriginalSelectionActionMode(Preference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_selection_action_mode_original,
                    ComponentUtils.getFullAliasName(ProcessTextActivity.class, "WebSearch"));
        } else {
            ComponentUtils.enableComponent(ComponentUtils.getFullAliasName(ProcessTextActivity.class, "WebSearch"), enabled);
        }
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

    @PreferenceChange(prefs = R.string.key_system_share_qrcode_scan, refreshAtStart = true)
    private void systemShareQrcodeScan(final SwitchPreference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_system_share_qrcode_scan, QrCodeActivity.class);
        } else {
            if (enabled) {
                PermissionUtils.requestPermissions(getActivity(), (ok) -> {
                    if (ok) {
                        ComponentUtils.enableComponent(QrCodeActivity.class, true);
                    } else {
                        preference.setChecked(false);
                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                ComponentUtils.enableComponent(QrCodeActivity.class, false);
            }
        }
    }

    @PreferenceChange(prefs = R.string.key_system_share_image_search, refreshAtStart = true)
    private void systemShareImageSearch(final SwitchPreference preference, boolean enabled, Extra extra) {
        if (extra.refreshAtStart) {
            uncheckPreferenceByDisabledComponent(R.string.key_system_share_image_search, ImageSearchActivity.class);
        } else {
            if (enabled) {
                PermissionUtils.requestPermissions(getActivity(), (ok) -> {
                    if (ok) {
                        ComponentUtils.enableComponent(ImageSearchActivity.class, true);
                    } else {
                        preference.setChecked(false);
                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            } else {
                ComponentUtils.enableComponent(ImageSearchActivity.class, false);
            }
        }
    }

    @PreferenceClick(prefs = R.string.key_system_share_filter)
    private void systemShareFilter(Preference preference) {
        ListCheckActivity.create(getActivity(), ShareFilterDataProvider.class);
    }

    @PreferenceClick(prefs = R.string.key_system_share_copy_to_share)
    private void systemShareCopyToShare(SwitchPreference preference) {
        Intent intent = new Intent(getActivity(), CopyToShareService.class);
        if (!preference.isChecked()) {
            intent.putExtra("stop", true);
        }
        getActivity().startService(intent);
    }

    @PreferenceClick(prefs = R.string.key_prevent_process_set_list)
    private void setPreventProcessList(Preference preference) {
        Bundle extra = new Bundle();
        extra.putInt("key", R.string.key_prevent_process_list);
        extra.putString("title", getString(R.string.pref_prevent_process));
        extra.putBoolean("app_user_only", true);
        ListCheckActivity.create(getActivity(), AppListProvider.class, extra);
    }

    @PreferenceClick(prefs = R.string.key_app_list_block_package)
    private void setAppListBlockPackage(Preference preference) {
        Bundle extra = new Bundle();
        extra.putInt("key", R.string.key_app_list_block_package_list);
        extra.putString("title", getString(R.string.pref_app_list_block_package_summary));
        ListCheckActivity.create(getActivity(), AppListProvider.class, extra);
    }

    @PreferenceClick(prefs = R.string.key_app_list_block_for)
    private void setAppListBlockFor(Preference preference) {
        Bundle extra = new Bundle();
        extra.putInt("key", R.string.key_app_list_block_for_list);
        extra.putString("title", getString(R.string.pref_app_list_block_for_summary));
        extra.putBoolean("app_user_only", true);
        ListCheckActivity.create(getActivity(), AppListProvider.class, extra);
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

    @PreferenceLoad
    private void tile4G3G() {
        ListPreference pref4g = findListPreference(R.string.key_quick_settings_tile_4g);
        ListPreference pref3g = findListPreference(R.string.key_quick_settings_tile_3g);
        Logger.d("Try get network types.");
        try {
            Context context = getActivity().createPackageContext(PackageNames.ANDROID_SETTINGS, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            Class cls = Class.forName(ClassNames.RADIO_INFO, true, context.getClassLoader());
            String[] fieldNames = new String[] { "PREFERRED_NETWORK_LABELS_MAX_LTE", "mPreferredNetworkLabels" };
            String[] types = null;
            for (String fieldName : fieldNames) {
                try {
                    types = (String[]) ReflectUtils.getStaticObjectField(cls, fieldName);
                } catch (Throwable e) {}
                if (types != null) {
                    break;
                }
            }
            setTypesForListPreference(types, pref4g);
            setTypesForListPreference(types, pref3g);
        } catch (Throwable e) {
            Logger.e("Failed to get network types, " + e);
            Logger.stackTrace(e);
            pref4g.setEnabled(false);
            pref3g.setEnabled(false);
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

    @PreferenceClick(prefs = R.string.key_phone_broadcast_cell_location_change)
    private void phoneBroadcastCellLocationChange(SwitchPreference preference) {
        Intent intent = new Intent(getActivity(), CellLocationService.class);
        if (!preference.isChecked()) {
            intent.putExtra("stop", true);
        }
        getActivity().startService(intent);
    }

    @PreferenceClick(prefs = R.string.key_display_toast_block_list)
    private void displayToastBlockList(EditTextPreference preference) {
        List<String> words = Prefs.large().getStringList(R.string.key_display_toast_block_list, new ArrayList<>(), false);
        preference.getEditText().setText(String.join("\n", words));
        preference.setOnPreferenceChangeListener((_preference, newValue) -> {
            Prefs.large().putStringList(R.string.key_display_toast_block_list,
                    StringUtils.split(newValue.toString(), '\n'));
            return false;
        });
    }

  /*  @PreferenceClick(prefs = R.string.key_trust_agent_wifi_aps)
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
    }*/
}
