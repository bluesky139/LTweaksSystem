package li.lingfeng.ltsystem.tweaks.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.android.internal.telephony.PhoneFactory;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_PHONE, prefs = R.string.key_quick_settings_tile_4g3g)
public class Switch4G3G extends TweakBase {

    private static final String PHONE_FACTORY = "com.android.internal.telephony.PhoneFactory";
    public static final String ACTION_SWITCH = Switch4G3G.class.getName() + ".ACTION_SWITCH";
    private SwitchHandler mHandler;
    private SwitchNetTypeReceiver mSwitchNetTypeReceiver;

    @Override
    public void com_android_internal_telephony_PhoneFactory__makeDefaultPhone__Context(ILTweaks.MethodParam param) {
        param.after(() -> {
            Context context = (Context) param.args[0];
            initAndroidPhone(context);
        });
    }

    private void initAndroidPhone(Context context) {
        Logger.i("Init android phone for Xposed4G3G.");
        mHandler = new SwitchHandler();
        mSwitchNetTypeReceiver = new SwitchNetTypeReceiver();
        IntentFilter filter = new IntentFilter(ACTION_SWITCH);
        context.registerReceiver(mSwitchNetTypeReceiver, filter);
    }

    private class SwitchNetTypeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                boolean isOn = intent.getBooleanExtra("is_on", true);
                int typeKey = isOn ? R.string.key_quick_settings_tile_3g : R.string.key_quick_settings_tile_4g;
                String strType = Prefs.instance().getString(typeKey, null);
                if (strType == null) {
                    Logger.e("SwitchNetTypeReceiver strType is null.");
                    return;
                }
                int type = Integer.parseInt(strType);
                Logger.d("SwitchNetTypeReceiver setPreferredNetworkType " + type);
                Message msg = mHandler.obtainMessage(0);
                PhoneFactory.getDefaultPhone().setPreferredNetworkType(type, msg);
            } catch (Throwable e) {
                Logger.e("SwitchNetTypeReceiver error, " + e);
                Logger.stackTrace(e);
            }
        }
    }

    private static class SwitchHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            try {
                Object e = ReflectUtils.getObjectField(msg.obj, "exception");
                if (e != null) {
                    Logger.e("Set preferred net type failed, " + e);
                }
            } catch (Throwable e) {
                Logger.w("Don't know preferred net type is set or not, " + e);
            }
        }
    }
}
