package li.lingfeng.ltsystem;

public class ILTweaks {

    public static abstract class Loader {
        public ILTweaksMethods methods = instantiateMethods();
        protected abstract ILTweaksMethods instantiateMethods();
        public abstract void initInZygote() throws Throwable;
    }

    public static abstract class MethodParam {
        public Object thisObject;
        public Object[] args;
        public abstract void before(Before before);
        public abstract void after(After after);
        public abstract boolean hasHook();
        public abstract void hookBefore();
        public abstract void hookAfter();
        public abstract void setArg(int i, Object arg);
        public abstract boolean isArgsModified();
        public abstract void setResult(Object result);
        public abstract void setResultSilently(Object result);
        public abstract void setThrowable(Throwable throwable);
        public abstract boolean hasResult();
        public abstract Object getResult();
        public abstract Object getResultOrThrowable() throws Throwable;
    }

    public interface Before {
        void before() throws Throwable;
    }

    public interface After {
        void after() throws Throwable;
    }

    public interface ParamCreator {
        MethodParam create(Object thisObject, Object... args);
    }
}