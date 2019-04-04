package li.lingfeng.ltsystem.tweaks.system;

import android.content.IntentFilter;
import android.content.pm.PackageParser;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.IntentActions;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
public class PreventReceiver extends PreventRunning {

    @Override
    protected int getPreventListKey() {
        return R.string.key_prevent_list_prevent_receiver;
    }

    @Override
    public void com_android_server_am_ActivityManagerService__registerReceiver__IApplicationThread_String_IIntentReceiver_IntentFilter_String_int_int(ILTweaks.MethodParam param) {
        param.before(() -> {
            String callerPackage = (String) param.args[1];
            if (!getPreventList().contains(callerPackage)) {
                return;
            }
            IntentFilter filter = (IntentFilter) param.args[3];
            filterActions(filter, callerPackage);
            if (filter.countActions() == 0) {
                param.setResult(null);
            }
        });
    }

    @Override
    public void android_content_pm_PackageParser__parsePackage__File_int_boolean(ILTweaks.MethodParam param) {
        param.after(() -> {
            PackageParser.Package pkg = (PackageParser.Package) param.getResult();
            if (pkg == null || !getPreventList().contains(pkg.packageName)) {
                return;
            }
            for (PackageParser.Activity receiver : pkg.receivers) {
                for (IntentFilter filter : receiver.intents) {
                    filterActions(filter, pkg.packageName);
                }
            }
        });
    }

    private void filterActions(IntentFilter filter, String packageName) throws Throwable {
        ArrayList<String> actions = (ArrayList<String>) ReflectUtils.getObjectField(filter, "mActions");
        for (int i = actions.size() - 1; i >= 0; --i) {
            String action = actions.get(i);
            if (ArrayUtils.contains(IntentActions.sReceiverPreventedArray, action)) {
                Logger.v("Prevent receiver action " + action + " from " + packageName);
                actions.remove(i);
            } else {
                //Logger.d("Pass receiver action " + action + " from " + packageName);
            }
        }
    }
}
