package li.lingfeng.ltsystem.tweaks.entertainment;

import android.content.SharedPreferences;

import java.util.Map;
import java.util.WeakHashMap;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_danmaku_off)
public class BilibiliDanmakuOff extends TweakBase {

    private WeakHashMap<SharedPreferences, Void> mModified = new WeakHashMap<>();

    @Override
    public void android_app_SharedPreferencesImpl__awaitLoadedLocked__(ILTweaks.MethodParam param) {
        param.after(() -> {
            SharedPreferences preferences = (SharedPreferences) param.thisObject;
            if (!mModified.containsKey(preferences)) {
                Map<String, Object> map = (Map<String, Object>) ReflectUtils.getObjectField(preferences, "mMap");
                if (map.containsKey("danmaku_switch")) {
                    Logger.d("has danmaku_switch in map.");
                    map.put("danmaku_switch", false);
                }
                mModified.put(preferences, null);
            }
        });
    }

    @Override
    public void android_app_SharedPreferencesImpl$EditorImpl__commitToMemory__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Map<String, Object> map = (Map<String, Object>) ReflectUtils.getObjectField(param.thisObject, "mModified");
            if (map.containsKey("danmaku_switch")) {
                Logger.d("has danmaku_switch in map before commit to memory.");
                map.put("danmaku_switch", false);
            }
        });
    }
}
