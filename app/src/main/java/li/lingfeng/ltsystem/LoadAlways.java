package li.lingfeng.ltsystem;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageParser;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

public class LoadAlways {

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = {})
    public static class Android extends TweakBase {

        @Override
        public void com_android_server_am_ActivityManagerService__ActivityManagerService__Context_ActivityTaskManagerService(ILTweaks.MethodParam param) {
            param.after(() -> {
                LTPrefService.register((Context) param.args[0]);
            });
        }

        @Override
        public void com_android_server_pm_PackageDexOptimizer__performDexOptLI__PackageParser$Package_String$array_CompilerStats$PackageStats_PackageDexUsage$PackageUseInfo_DexoptOptions(ILTweaks.MethodParam param) {
            param.before(() -> {
                PackageParser.Package pkg = (PackageParser.Package) param.args[0];
                if (pkg.packageName.equals(PackageNames.L_TWEAKS)) {
                    Logger.i("Set LTweaks dex2oat compiler filter to speed.");
                    ReflectUtils.setObjectField(param.args[4], "mCompilerFilter", "speed");
                    String[] targetInstructionSets = (String[]) param.args[1];
                    Logger.d("targetInstructionSets " + StringUtils.join(targetInstructionSets, ','));

                    Class cls = findClass("com.android.server.pm.InstructionSets");
                    String[] allInstructionSets = ((List<String>) ReflectUtils.callStaticMethod(cls, "getAllInstructionSets")).toArray(new String[0]);
                    Logger.i("Set targetInstructionSets to all: " + StringUtils.join(allInstructionSets, ','));
                    param.setArg(1, allInstructionSets);
                }
            });
        }
    }

    @MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = {})
    public static class SystemUI extends TweakBase {

        @Override
        public void com_android_systemui_qs_external_TileLifecycleManager__setBindService__boolean(ILTweaks.MethodParam param) {
            param.before(() -> {
                if (!((boolean) param.args[0])) {
                    Intent intent = (Intent) ReflectUtils.getObjectField(param.thisObject, "mIntent");
                    if (PackageNames.L_TWEAKS.equals(intent.getComponent().getPackageName())) {
                        Logger.v("Stay bound with " + intent.getComponent().toShortString());
                        param.setResult(null);
                    }
                }
            });
        }
    }
}
