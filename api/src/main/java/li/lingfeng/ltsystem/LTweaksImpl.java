package li.lingfeng.ltsystem;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * {@hide}
 */
public class LTweaksImpl {

    private static final String TAG = "LTweaksImpl";

    public static class MethodParamImpl extends ILTweaks.MethodParam {
        private boolean _isArgsModified = false;
        private Object result;
        private Throwable throwable;
        private boolean _hasResult = false;
        private List<ILTweaks.Before> befores;
        private List<ILTweaks.After> afters;

        public MethodParamImpl(Object thisObject, Object... args) {
            this.thisObject = thisObject;
            this.args = args;
        }

        @Override
        public void before(ILTweaks.Before before) {
            if (befores == null) {
                befores = new ArrayList<>();
            }
            befores.add(before);
        }

        @Override
        public void after(ILTweaks.After after) {
            if (afters == null) {
                afters = new ArrayList<>();
            }
            afters.add(after);
        }

        @Override
        public boolean hasHook() {
            return befores != null || afters != null;
        }

        @Override
        public void hookBefore() {
            if (befores == null) {
                return;
            }
            for (ILTweaks.Before hook : befores) {
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

        @Override
        public void hookAfter() {
            if (afters == null) {
                return;
            }
            if (hasResult()) {
                Log.e(TAG, "Already has result, why execute after?");
                return;
            }
            for (ILTweaks.After hook : afters) {
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

        @Override
        public void setArg(int i, Object arg) {
            args[i] = arg;
            _isArgsModified = true;
        }

        @Override
        public boolean isArgsModified() {
            return _isArgsModified;
        }

        @Override
        public void setResult(Object result) {
            this.result = result;
            _hasResult = true;
        }

        @Override
        public void setResultSilently(Object result) {
            this.result = result;
        }

        @Override
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            _hasResult = true;
        }

        @Override
        public boolean hasResult() {
            return _hasResult;
        }

        @Override
        public Object getResult() {
            return result;
        }

        @Override
        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null) {
                throw throwable;
            } else {
                return result;
            }
        }
    }

    public static class ParamCreatorImpl implements ILTweaks.ParamCreator {

        @Override
        public ILTweaks.MethodParam create(Object thisObject, Object... args) {
            return new MethodParamImpl(thisObject, args);
        }
    }
}
