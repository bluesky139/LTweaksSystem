package li.lingfeng.ltsystem;

import android.util.Log;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;

import android.app.ActivityThread;

public class ILTweaks {

    private static final String TAG = "ILTweaks";

    public static abstract class Loader {
        public ILTweaksMethods methods = instantiateMethods();
        protected abstract ILTweaksMethods instantiateMethods();
        public abstract void initInZygote() throws Throwable;
    }

    public static class MethodParam {
        public Object thisObject;
        public Object[] args;
        private Object result;
        private Throwable throwable;
        private boolean _hasResult = false;
        private MethodHookWrapper hookWrapper;
        private List<MethodHook> hooks;

        public MethodParam(Object thisObject, Object... args) {
            this.thisObject = thisObject;
            this.args = args;
        }

        public void addHook(MethodHook hook) {
            if (hooks == null) {
                hooks = new ArrayList();
                hookWrapper = new MethodHookWrapper();
            }
            hooks.add(hook);
        }

        public boolean hasHook() {
            return hooks != null;
        }

        public void hookBefore() {
            try {
                hookWrapper.before();
            } catch (Throwable throwable) {}
        }

        public void hookAfter() {
            try {
                hookWrapper.after();
            } catch (Throwable throwable) {}
        }

        public void setResult(Object result) {
            this.result = result;
            _hasResult = true;
        }

        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            _hasResult = true;
        }

        public boolean hasResult() {
            return _hasResult;
        }

        public Object getResult() {
            return result;
        }

        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null) {
                throw throwable;
            } else {
                return result;
            }
        }

        class MethodHookWrapper extends MethodHook {
            public void before() throws Throwable {
                for (MethodHook hook : hooks) {
                    try {
                        hook.before();
                    } catch (Throwable throwable) {
                        Log.e(TAG, "Hook before execute exception.", throwable);
                    }
                    if (hasResult()) {
                        return;
                    }
                }
            }
            public void after() throws Throwable {
                if (hasResult()) {
                    Log.e(TAG, "Already has result, why execute after?");
                    return;
                }
                for (MethodHook hook : hooks) {
                    try {
                        hook.after();
                    } catch (Throwable throwable) {
                        Log.e(TAG, "Hook after execute exception.", throwable);
                    }
                    if (hasResult()) {
                        return;
                    }
                }
            }
        }
    }

    public static abstract class MethodHook {
        public void before() throws Throwable {
        }
        public void after() throws Throwable {
        }
    }

    public static Application currentApplication() {
        return ActivityThread.currentApplication();
    }
}