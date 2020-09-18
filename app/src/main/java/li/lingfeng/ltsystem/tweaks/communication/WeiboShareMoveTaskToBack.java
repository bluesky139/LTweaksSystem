package li.lingfeng.ltsystem.tweaks.communication;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakMoveTaskToBack;

@MethodsLoad(packages = PackageNames.WEIBO_SHARE, prefs = R.string.key_weibo_share_move_task_to_back)
public class WeiboShareMoveTaskToBack extends TweakMoveTaskToBack {

    private static final String LAUNCHER = "com.hengye.share.Launcher";

    @Override
    protected String getLaunchActivity() {
        return LAUNCHER;
    }
}
