package li.lingfeng.ltsystem;

import android.app.ActivityThread;
import android.app.Application;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

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
        private boolean _isArgsModified = false;
        private Object result;
        private Throwable throwable;
        private boolean _hasResult = false;
        private List<Before> befores;
        private List<After> afters;

        public MethodParam(Object thisObject, Object... args) {
            this.thisObject = thisObject;
            this.args = args;
        }

        public void before(Before before) {
            if (befores == null) {
                befores = new ArrayList<>();
            }
            befores.add(before);
        }

        public void after(After after) {
            if (afters == null) {
                afters = new ArrayList<>();
            }
            afters.add(after);
        }

        public boolean hasHook() {
            return befores != null || afters != null;
        }

        public void hookBefore() {
            if (befores == null) {
                return;
            }
            for (Before hook : befores) {
                try {
                    hook.before();
                } catch (Throwable throwable) {
                    Log.e(TAG, "Hook before exception.", throwable);
                }
                if (hasResult()) {
                    return;
                }
            }
        }

        public void hookAfter() {
            if (afters == null) {
                return;
            }
            if (hasResult()) {
                Log.e(TAG, "Already has result, why execute after?");
                return;
            }
            for (After hook : afters) {
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

        public void setArg(int i, Object arg) {
            args[i] = arg;
            _isArgsModified = true;
        }

        public boolean isArgsModified() {
            return _isArgsModified;
        }

        public void setResult(Object result) {
            this.result = result;
            _hasResult = true;
        }

        public void setResultSilently(Object result) {
            this.result = result;
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
    }

    public interface Before {
        void before() throws Throwable;
    }

    public interface After {
        void after() throws Throwable;
    }

    public static Application currentApplication() {
        return ActivityThread.currentApplication();
    }
}