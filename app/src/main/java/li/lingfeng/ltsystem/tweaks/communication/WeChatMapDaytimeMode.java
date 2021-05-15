package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_map_daytime_mode)
public class WeChatMapDaytimeMode extends TweakBase {

    private static final String LOCATION_ACTIVITY = "com.tencent.mm.plugin.location_soso.SoSoProxyUI";
    private static final String MAP_VIEW = "com.tencent.tencentmap.mapsdk.maps.MapView";
    private static final int MAP_TYPE_NORMAL = 1000;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(LOCATION_ACTIVITY, param, () -> {
            Logger.v("Set map to daytime mode.");
            Activity activity = (Activity) param.thisObject;
            View mapView = ViewUtils.findViewByType(activity, findClass(MAP_VIEW));
            Object map = ReflectUtils.callMethod(mapView, "getMap");
            ReflectUtils.callMethod(map, "setMapType", new Object[] {
                    MAP_TYPE_NORMAL
            }, new Class[] {
                    int.class
            });
        });
    }
}
