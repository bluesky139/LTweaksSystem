package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_danmaku_off)
public class BilibiliDanmakuOff extends TweakBase {

    private static final String VIDEO_DETAILS_ACTIVITY = "tv.danmaku.bili.ui.video.VideoDetailsActivity";
    private static final String BANGUMI_DETAIL_ACTIVITY = "com.bilibili.bangumi.ui.page.detail.BangumiDetailActivityV3";
    private Handler mHandler;
    private SharedPreferences mSharedPreferences;
    private int mTryCount;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(VIDEO_DETAILS_ACTIVITY, param, () -> {
            handleActivity(param);
        });
        afterOnClass(BANGUMI_DETAIL_ACTIVITY, param, () -> {
            handleActivity(param);
        });
    }

    private void handleActivity(ILTweaks.MethodParam param) throws Throwable {
        final Activity activity = (Activity) param.thisObject;
        View videoView = (View) ViewUtils.findViewByName(activity, "play").getParent();
        View.OnClickListener originalClickListener = ViewUtils.getViewClickListener(videoView);
        if (videoView == null || originalClickListener == null) {
            Logger.e("videoView " + videoView + ", originalClickListener " + originalClickListener);
            return;
        }
        if (mSharedPreferences == null) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity.getApplication());
            mHandler = new Handler();
        }

        View.OnClickListener listener = v -> {
            videoView.setOnClickListener(originalClickListener);
            originalClickListener.onClick(v);
            mTryCount = 0;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        boolean danmakuOn = mSharedPreferences.getBoolean("danmaku_switch", true);
                        if (danmakuOn) {
                            Logger.v("Danmaku is on, try off, retry " + mTryCount);
                            View danmakuSwitch = ViewUtils.findViewByName(activity, "new_danmaku_switch");
                            danmakuSwitch.performClick();
                        } else {
                            return;
                        }
                        ++mTryCount;
                        if (mTryCount > 20) {
                            Logger.e("Reach max try count.");
                            return;
                        }
                        mHandler.postDelayed(this, 200);
                    } catch (Throwable e) {
                        Logger.e("Danmaku off exception.", e);
                    }
                }
            }, 200);
        };
        videoView.setOnClickListener(listener);
    }
}
