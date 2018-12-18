package li.lingfeng.ltsystem;

import android.util.Log;

import li.lingfeng.ltsystem.lib.MethodsLoad;

@MethodsLoad(packages = "com.android.messaging", prefs = {})
public class TestHook2 extends ILTweaksMethods {

    @Override
    public void android_app_Activity__onCreate__Bundle(final ILTweaks.MethodParam param) {
        param.addHook(new ILTweaks.MethodHook() {
            @Override
            public void before() {
                Log.d("LTweaks", "test before2.");
            }
        });
    }
}
