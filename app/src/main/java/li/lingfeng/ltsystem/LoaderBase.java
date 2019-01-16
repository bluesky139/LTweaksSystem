package li.lingfeng.ltsystem;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public abstract class LoaderBase extends ILTweaks.Loader {

    private Map<String, Set<Class<? extends ILTweaksMethods>>> mModules = new HashMap<>();
    private Set<Class<? extends ILTweaksMethods>> mModulesForAll = new HashSet<>();
    private List<ILTweaksMethods> mModuleInstances;
    private static List<ILTweaksMethods> EMPTY_MODULE_INSTANCES = new ArrayList<>();

    @Override
    public void initInZygote() throws Throwable {
        Logger.i("LoaderBase initInZygote.");
        addModules();
        addModulesForAll();
        Prefs.large().load();
    }

    protected void addModule(String packageName, Class<? extends ILTweaksMethods> cls) {
        if (!mModules.containsKey(packageName)) {
            mModules.put(packageName, new HashSet<Class<? extends ILTweaksMethods>>());
        }
        mModules.get(packageName).add(cls);
    }

    protected void addModuleForAll(Class<? extends ILTweaksMethods> cls) {
        mModulesForAll.add(cls);
    }

    private Set<Class<? extends ILTweaksMethods>> getModules(String packageName) {
        Set<Class<? extends ILTweaksMethods>> modules = new HashSet<>(mModulesForAll);
        Set<Class<? extends ILTweaksMethods>> packageModules = mModules.get(packageName);
        if (packageModules != null) {
            modules.addAll(packageModules);
        }
        return modules;
    }

    protected abstract void addModules();
    protected abstract void addModulesForAll();

    protected List<ILTweaksMethods> getModuleInstances() {
        if (mModuleInstances == null) {
            if (getPackageName() == null) {
                return EMPTY_MODULE_INSTANCES;
            }

            Set<Class<? extends ILTweaksMethods>> modules = getModules(getPackageName());
            if (modules != null) {
                mModuleInstances = new ArrayList<>(modules.size());
                for (Class<? extends ILTweaksMethods> cls : modules) {
                    boolean enabled = false;
                    MethodsLoad methodsLoad = cls.getAnnotation(MethodsLoad.class);
                    int[] prefs = methodsLoad.prefs();
                    if (prefs.length > 0) {
                        for (int key : prefs) {
                            if (Prefs.instance().getBoolean(key, false)) {
                                enabled = true;
                                break;
                            }
                        }
                    }

                    if (enabled || prefs.length == 0) {
                        if (mModulesForAll.contains(cls)) {
                            if (getPackageName().equals(PackageNames.ANDROID)) {
                                Logger.i("Load " + cls.getName() + " for all packages");
                            }
                        } else {
                            Logger.d("Load " + cls.getName() + " for " + getPackageName());
                        }
                        if (ArrayUtils.contains(methodsLoad.excludedPackages(), getPackageName())) {
                            continue;
                        }
                        try {
                            mModuleInstances.add(cls.newInstance());
                        } catch (Throwable e) {
                            Logger.e("Can't instantiate module " + cls.getName());
                        }
                    }
                }
            } else {
                mModuleInstances = new ArrayList<>(0);
            }
        }
        return mModuleInstances;
    }

    private String getPackageName() {
        return LTHelper.currentPackageName();
    }
}
