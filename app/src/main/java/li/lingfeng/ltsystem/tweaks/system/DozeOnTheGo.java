package li.lingfeng.ltsystem.tweaks.system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_phone_doze_on_the_go)
public class DozeOnTheGo extends TweakBase {

    private static final int UPDATE_LOW_POWER_MODE = 1;
    private BroadcastReceiver mOriginalReceiver;
    private Handler mHandler;

    @Override
    public void com_android_server_DeviceIdleController$Injector__useMotionSensor__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.v("DeviceIdleController.useMotionSensor() false");
            param.setResult(false);
        });
    }

    @Override
    public void com_android_server_location_GnssLocationProvider$ProviderHandler__handleInitialize__(ILTweaks.MethodParam param) {
        param.before(() -> {
            Logger.d("GnssLocationProvider.handleInitialize()");
            Object locationProvider = ReflectUtils.getSurroundingThis(param.thisObject);
            mOriginalReceiver = (BroadcastReceiver) ReflectUtils.getObjectField(locationProvider, "mBroadcastReceiver");
            ReflectUtils.setObjectField(locationProvider, "mBroadcastReceiver", mReceiver);
            try {
                ReflectUtils.setBooleanField(locationProvider, "mIsDeviceStationary", true);
                mHandler = (Handler) ReflectUtils.getObjectField(locationProvider, "mHandler");
            } catch (Throwable e) {
                Logger.w("No mIsDeviceStationary in GnssLocationProvider, must be < android-10.0.0_r30, ignore.");
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mHandler != null && PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED.equals(intent.getAction())) {
                Logger.v("UPDATE_LOW_POWER_MODE by ACTION_DEVICE_IDLE_MODE_CHANGED.");
                mHandler.sendEmptyMessage(UPDATE_LOW_POWER_MODE);
            } else {
                mOriginalReceiver.onReceive(context, intent);
            }
        }
    };
}
