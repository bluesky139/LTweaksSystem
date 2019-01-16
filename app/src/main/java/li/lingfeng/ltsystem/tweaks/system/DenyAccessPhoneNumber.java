package li.lingfeng.ltsystem.tweaks.system;

import android.content.pm.ApplicationInfo;
import android.os.Process;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = {}, prefs = R.string.key_phone_deny_access_phone_number)
public class DenyAccessPhoneNumber extends TweakBase {

    @Override
    public void android_telephony_TelephonyManager__getLine1Number__int(ILTweaks.MethodParam param) {
        handleGet(param, "getLine1Number");
    }

    @Override
    public void android_telephony_SubscriptionInfo__getNumber__(ILTweaks.MethodParam param) {
        handleGet(param, "getNumber");
    }

    private void handleGet(ILTweaks.MethodParam param, String methodName) {
        param.before(() -> {
            ApplicationInfo appInfo = LTHelper.currentApplication().getApplicationInfo();
            if (appInfo.uid < Process.FIRST_APPLICATION_UID || appInfo.uid > Process.LAST_APPLICATION_UID) {
                return;
            }
            Logger.d("Deny access phone number from " + methodName);
            param.setResult(null);
        });
    }
}
