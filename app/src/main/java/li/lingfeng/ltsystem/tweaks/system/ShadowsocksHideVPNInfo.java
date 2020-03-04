package li.lingfeng.ltsystem.tweaks.system;

import android.util.SparseArray;

import com.android.internal.net.VpnConfig;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_shadowsocks_hide_vpn_info)
public class ShadowsocksHideVPNInfo extends TweakBase {

    @Override
    public void com_android_systemui_statusbar_policy_SecurityControllerImpl__updateState__(ILTweaks.MethodParam param) {
        param.after(() -> {
            SparseArray<VpnConfig> currentVpns = (SparseArray<VpnConfig>) ReflectUtils.getObjectField(param.thisObject, "mCurrentVpns");
            for (int i = currentVpns.size() - 1; i >= 0; --i) {
                String packageName = currentVpns.get(i).user;
                if (packageName.equals(PackageNames.SHADOWSOCKS)) {
                    Logger.v("Remove Shadowsocks from SystemUI.");
                    currentVpns.removeAt(i);
                }
            }
        });
    }
}
