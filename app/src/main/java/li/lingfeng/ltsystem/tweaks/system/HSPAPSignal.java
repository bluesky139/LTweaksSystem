package li.lingfeng.ltsystem.tweaks.system;

import android.telephony.TelephonyManager;
import android.util.SparseArray;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_display_hspap_signal)
public class HSPAPSignal extends TweakBase {

    private static final String MOBILE_SIGNAL_CONTROLLER = "com.android.systemui.statusbar.policy.MobileSignalController";
    private static final String MOBILE_ICON_GROUP = MOBILE_SIGNAL_CONTROLLER + "$MobileIconGroup";
    private static final String ACCESSIBILITY_CONTENT_DESCRIPTIONS = "com.android.systemui.statusbar.policy.AccessibilityContentDescriptions";
    private Object mHP;

    @Override
    public void com_android_systemui_statusbar_policy_MobileSignalController__mapIconSets__(ILTweaks.MethodParam param) {
        param.after(() -> {
            SparseArray icons = (SparseArray) ReflectUtils.getObjectField(param.thisObject, "mNetworkToIconLookup");
            if (mHP == null) {
                int[] signalStrength = (int[]) ReflectUtils.getStaticObjectField(findClass(ACCESSIBILITY_CONTENT_DESCRIPTIONS), "PHONE_SIGNAL_STRENGTH");
                mHP = ReflectUtils.getFirstConstructor(findClass(MOBILE_ICON_GROUP)).newInstance(
                        "HP",
                        null,
                        null,
                        signalStrength,
                        0, 0,
                        0,
                        0,
                        signalStrength[0],
                        ContextUtils.getStringId("accessibility_data_connection_3_5g"),
                        ContextUtils.getDrawableId("ltweaks_stat_sys_data_fully_connected_hp"),
                        false,
                        ContextUtils.getDrawableId("ltweaks_ic_qs_signal_hp")
                );
            }
            icons.put(TelephonyManager.NETWORK_TYPE_HSPAP, mHP);
            Logger.i("MobileSignalController.mapIconSets H+");
        });
    }
}
