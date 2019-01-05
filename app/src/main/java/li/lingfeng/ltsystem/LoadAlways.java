package li.lingfeng.ltsystem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.File;

import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

public class LoadAlways {

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
    public static class Android extends TweakBase {

        private boolean mCalledFinishBooting = false;

        @Override
        public void com_android_server_am_ActivityManagerService__finishBooting__(ILTweaks.MethodParam param) {
            if (mCalledFinishBooting) {
                return;
            }
            param.after(() -> {
                mCalledFinishBooting = true;
                LTHelper.currentApplication().registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            String key = intent.getStringExtra("key");
                            String value = intent.getStringExtra("value");
                            Logger.d("Large store update, " + key + ": " + value);

                            JSONObject jContent = readLargeStore();
                            jContent.put(key, value);
                            File file = new File(Prefs.LARGE_STORE_PATH);
                            FileUtils.writeStringToFile(file, jContent.toString(), "UTF-8");
                        } catch (Throwable e) {
                            Logger.e("Save preference into " + Prefs.LARGE_STORE_PATH + " exception.", e);
                            Toast.makeText(LTHelper.currentApplication(), "Large store save error.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new IntentFilter(Prefs.LARGE_STORE_UPDATE));

                LTHelper.currentApplication().registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        try {
                            JSONObject jContent = readLargeStore();
                            intent = new Intent(Prefs.LARGE_STORE_ALL);
                            intent.putExtra("all", jContent.toString());
                            context.sendBroadcast(intent);
                        } catch (Throwable e) {
                            Logger.e("Large store get exception.", e);
                        }
                    }
                }, new IntentFilter(Prefs.LARGE_STORE_GET));
            });
        }

        private JSONObject readLargeStore() throws Throwable {
            String content = "{}";
            File file = new File(Prefs.LARGE_STORE_PATH);
            if (file.exists()) {
                content = FileUtils.readFileToString(file, "UTF-8");
            }
            return JSON.parseObject(content);
        }

        @Override
        public void com_android_server_am_ActivityManagerService__checkBroadcastFromSystem__Intent_ProcessRecord_String_int_boolean_List(ILTweaks.MethodParam param) {
            param.before(() -> {
                Intent intent = (Intent) param.args[0];
                if (intent.getAction().startsWith(PackageNames.L_TWEAKS)) {
                    param.setResult(null);
                }
            });
        }
    }
}
