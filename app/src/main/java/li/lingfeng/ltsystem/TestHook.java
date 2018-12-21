package li.lingfeng.ltsystem;

import android.os.Bundle;
import android.util.Log;

import li.lingfeng.ltsystem.lib.MethodsLoad;

@MethodsLoad(packages = "com.android.messaging", prefs = {})
public class TestHook extends ILTweaksMethods {

    @Override
    public void android_app_Activity__onCreate__Bundle(final ILTweaks.MethodParam param) {
        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void before() {
                Log.d("LTweaks", "test before.");
                Log.d("LTweaks", "param.args.length " + param.args.length);
            }

            @Override
            public void after() {
                Log.d("LTweaks", "test after " + param.thisObject);
                Log.d("LTweaks", "currentApplication " + ILTweaks.currentApplication());
            }
        });
    }
}
