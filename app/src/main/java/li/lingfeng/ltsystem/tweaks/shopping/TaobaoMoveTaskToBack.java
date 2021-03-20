package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakMoveTaskToBack;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.TAOBAO, prefs = R.string.key_taobao_move_task_to_back)
public class TaobaoMoveTaskToBack extends TweakMoveTaskToBack {

    private static final String WELCOME_ACTIVITY = "com.taobao.tao.welcome.Welcome";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        super.android_app_Activity__performCreate__Bundle_PersistableBundle(param);
        beforeOnClass(WELCOME_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = activity.getIntent();
            Uri uri = intent.getData();
            if (uri != null) {
                Uri.Builder builder = uri.buildUpon().clearQuery();
                for (String name : uri.getQueryParameterNames()) {
                    if (name.equals("backURL")) {
                        Logger.d("Remove backURL.");
                        continue;
                    }
                    builder.appendQueryParameter(name, uri.getQueryParameter(name));
                }
                uri = builder.build();
                intent.setData(uri);
            }
        });
    }

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        param.before(() -> {
            Activity activity = (Activity) param.thisObject;
            if (mCount == 0 && activity.isFinishing()) {
                Logger.w("MoveTaskToBack prevent start new activity, due to last activity is finishing.");
                param.setResult(null);
            }
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mCount == 1) {
                Activity activity = (Activity) param.thisObject;
                if (activity.isFinishing()) {
                    Logger.d("MoveTaskToBack finishing last activity in onPause.");
                    mCount = 0;
                    activity.moveTaskToBack(true);
                }
            }
        });
    }

    @Override
    protected String getLaunchActivity() {
        return null;
    }
}
