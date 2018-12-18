package li.lingfeng.ltsystem;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.utils.Logger;

public abstract class LoaderBase extends ILTweaks.Loader {

    private Map<String, Set<Class<? extends ILTweaksMethods>>> mModules = new HashMap<>();
    private List<ILTweaksMethods> mModuleInstances;

    @Override
    public void initInZygote() throws Throwable {
        Logger.i("LoaderBase initInZygote.");
        addModules();
    }

    protected void addModule(String packageName, Class<? extends ILTweaksMethods> cls) {
        if (!mModules.containsKey(packageName)) {
            mModules.put(packageName, new HashSet<Class<? extends ILTweaksMethods>>());
        }
        mModules.get(packageName).add(cls);
    }

    private Set<Class<? extends ILTweaksMethods>> getModules(String packageName) {
        return mModules.get(packageName);
    }

    protected abstract void addModules();

    protected List<ILTweaksMethods> getModuleInstances() {
        if (mModuleInstances == null) {
            Set<Class<? extends ILTweaksMethods>> modules = getModules(getPackageName());
            if (modules != null) {
                mModuleInstances = new ArrayList<>(modules.size());
                for (Class<? extends ILTweaksMethods> cls : modules) {
                    boolean enabled = false;
                    int[] prefs = cls.getAnnotation(MethodsLoad.class).prefs();
                    if (prefs.length > 0) {
                        for (int key : prefs) {
                            if (Prefs.instance().getBoolean(key, false)) {
                                enabled = true;
                                break;
                            }
                        }
                    }

                    if (enabled || prefs.length == 0) {
                        Logger.d("Load " + cls.getName() + " for " + getPackageName());
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
        return ILTweaks.currentApplication().getPackageName();
    }
}
