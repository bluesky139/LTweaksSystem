package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_danmaku_off)
public class BilibiliDanmakuOff extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";
    private static final String BANGUMI_DETAIL_ACTIVITY = "com.bilibili.bangumi.ui.page.detail.BangumiDetailActivityV3";
    private SharedPreferences mSharedPreferences;

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        beforeOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            handleActivity(param);
        });
        beforeOnClass(BANGUMI_DETAIL_ACTIVITY, param, () -> {
            handleActivity(param);
        });
    }

    private void handleActivity(ILTweaks.MethodParam param) throws Throwable {
        final Activity activity = (Activity) param.thisObject;
        View v = ViewUtils.findViewByName(activity, "new_danmaku_switch");
        if (v != null) {
            if (mSharedPreferences == null) {
                Class cls = findClass("com.bilibili.lib.blkv.internal.sp.BLPrefManager");
                Field field = ReflectUtils.findFirstFieldByExactType(cls, cls);
                Object prefManager = field.get(null);
                Method method = ReflectUtils.findFirstMethodByTypes(cls, new Class[] {
                        Context.class, File.class, boolean.class
                }, SharedPreferences.class);
                File dir = LTHelper.currentApplication().getDir("blkv", 0);
                mSharedPreferences = (SharedPreferences) method.invoke(prefManager,
                        LTHelper.currentApplication(), new File(dir, "biliplayer.blkv"), false);
            }
            if (mSharedPreferences.getBoolean("danmaku_switch", true)) {
                Logger.v("Danmaku off at activity destroy.");
                v.performClick();
            }
        } else {
            Logger.e("Can't find new_danmaku_switch.");
        }
    }
}
