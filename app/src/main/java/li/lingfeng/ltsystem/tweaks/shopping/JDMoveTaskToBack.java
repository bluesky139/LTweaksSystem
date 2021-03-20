package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakMoveTaskToBack;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.JD, prefs = R.string.key_jd_move_task_to_back)
public class JDMoveTaskToBack extends TweakMoveTaskToBack {

    private static final String MAIN_ACTIVITY = "com.jingdong.app.mall.main.MainActivity";
    private static final String INTERFACE_ACTIVITY = "com.jingdong.app.mall.open.InterfaceActivity";
    private static final String WEB_ACTIVITY = "com.jingdong.app.mall.WebActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        super.android_app_Activity__performCreate__Bundle_PersistableBundle(param);
        beforeOnClass(INTERFACE_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            Intent intent = activity.getIntent();
            Bundle extras = intent.getExtras();
            for (String key : extras.keySet()) {
                Object _value = extras.get(key);
                if (_value != null && _value instanceof String
                        && ((String) _value).startsWith(PackageNames.SMZDM)) {
                    Logger.d("Remove " + key + ", " + _value);
                    extras.remove(key);
                    break;
                }
            }

            Uri uri = intent.getData();
            String params = uri.getQueryParameter("params");
            if (params != null) {
                Logger.d("Remove keplerID from params.");
                JSONObject jParams = JSON.parseObject(params);
                jParams.put("keplerFrom", "0");
                jParams.remove("keplerID");
                jParams.remove("kepler_param");
                jParams.remove("openflag");
                params = jParams.toString();
                Uri.Builder builder = uri.buildUpon().clearQuery();
                for (String name : uri.getQueryParameterNames()) {
                    builder.appendQueryParameter(name, name.equals("params") ? params : uri.getQueryParameter(name));
                }
                uri = builder.build();
                intent.setData(uri);
            }
        });
    }

    @Override
    protected String getLaunchActivity() {
        return MAIN_ACTIVITY;
    }

    // Handle this WebActivity manually, onKeyUp is not called.
    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        afterOnClass(WEB_ACTIVITY, param, () -> {
            if (mCount == 1) {
                Activity activity = (Activity) param.thisObject;
                if (activity.isFinishing()) {
                    Logger.d("MoveTaskToBack finishing last activity in WebActivity.onPause.");
                    mCount = 0;
                    activity.moveTaskToBack(true);
                }
            }
        });
    }
}
