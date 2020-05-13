package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.ClassNames;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.SimpleDrawer;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_remove_bottom_bar)
public class WeChatRemoveBottomBar extends TweakBase {

    private static final String PERSIONAL_INFO = "com.tencent.mm.plugin.setting.ui.setting.SettingsPersonalInfoUI";
    private static final int MAX_ERROR_COUNT = 20;
    private SimpleDrawer mDrawerLayout;
    private WeakReference mUserInfoDbRef;
    private Method mMethodAvatar;
    private boolean mNoDrawer = false;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(ClassNames.WE_CHAT_LAUNCHER_UI, param, () -> {
            /*if (!mNoDrawer) {
                List<Method> methods = getPossibleMethodsOfAvatar();
                if (methods == null) {
                    throw new Exception("Can't get methods of avatar.");
                }
                Logger.d("Possible " + methods.size() + " methods of avatar.");

                final List<Unhook> unhooks = new ArrayList<>(methods.size());
                for (final Method method : methods) {
                    Unhook unhook = XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Logger.i("Got avatar method, " + method);
                            mMethodAvatar = method;
                            XposedUtils.unhookAll(unhooks);
                        }
                    });
                    unhooks.add(unhook);
                }
            }*/

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = (Activity) param.thisObject;
                    View rootView = activity.findViewById(android.R.id.content);
                    listenOnLayoutChange(activity, rootView);
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(ClassNames.WE_CHAT_LAUNCHER_UI, param, () -> {
            mDrawerLayout = null;
            mMethodAvatar = null;
        });
    }

    private void listenOnLayoutChange(final Activity activity, final View view) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int errorCount = 0;
            @Override
            public void onGlobalLayout() {
                try {
                    boolean isOk = doModification(activity);
                    if (isOk) {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        Logger.v("Layout is not ready.");
                    }
                } catch (Throwable e) {
                    Logger.v("startHook error, " + e);
                    ++errorCount;
                    if (errorCount >= MAX_ERROR_COUNT) {
                        Logger.stackTrace(e);
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            }
        });
    }

    private boolean doModification(Activity activity) throws Throwable {
        final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        List<RelativeLayout> tabs = ViewUtils.findAllViewByTypeInSameHierarchy(rootView, RelativeLayout.class, 4);
        if (tabs.size() > 0) {
            Logger.i("Got " + tabs.size() + " tabs.");
            boolean isOk = handleWithTabs(activity, rootView, tabs);
            if (!isOk) {
                return false;
            }
        } else {
            throw new Exception("No tabs.");
        }

        /*if (!mNoDrawer) {
            String nickName = (String) XposedHelpers.callMethod(mUserInfoDbRef.get(), "get", 4, null);
            String userName = (String) XposedHelpers.callMethod(mUserInfoDbRef.get(), "get", 42, null);
            String originalUserName = (String) XposedHelpers.callMethod(mUserInfoDbRef.get(), "get", 2, null);
            Logger.d("nickName " + nickName + ", userName " + userName + ", originalUserName " + originalUserName);

            String name = !StringUtils.isEmpty(nickName) ? nickName : userName;
            name = !StringUtils.isEmpty(name) ? name : originalUserName;
            mDrawerLayout.getHeaderText().setText(name);
            mMethodAvatar.invoke(null, mDrawerLayout.getHeaderImage(), originalUserName);
        }*/
        return true;
    }

    private boolean handleWithTabs(final Activity activity, ViewGroup rootView, List<RelativeLayout> tabs) throws Throwable {
        if (!mNoDrawer) {
            SimpleDrawer.NavItem[] navItems = new SimpleDrawer.NavItem[tabs.size()];
            for (int i = 0; i < tabs.size(); ++i) {
                RelativeLayout tab = tabs.get(i);
                ImageView imageView = ViewUtils.findViewByType(tab, ImageView.class);
                if (imageView.getWidth() == 0) {
                    return false;
                }
                Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                imageView.draw(canvas);
                BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), bitmap);
                TextView textView = ViewUtils.findViewByType(tab, TextView.class);
                SimpleDrawer.NavItem navItem = new SimpleDrawer.NavItem(drawable, textView.getText(), tab);
                navItems[i] = navItem;
            }
            SimpleDrawer.NavItem headerItem = new SimpleDrawer.NavItem(ContextUtils.getAppIcon(),
                    ContextUtils.getAppName(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClassName(PackageNames.WE_CHAT, PERSIONAL_INFO);
                    activity.startActivity(intent);
                    mDrawerLayout.closeDrawers();
                }
            });

            FrameLayout allView = ViewUtils.rootChildsIntoOneLayout(activity);
            mDrawerLayout = new SimpleDrawer(activity, allView, navItems, headerItem);
            mDrawerLayout.updateHeaderBackground(0xFF191919);
            mDrawerLayout.updateNavListBackground(0xFF191919);
            mDrawerLayout.updateNavListTextColor(0xFFD3D3D3);
            rootView.addView(mDrawerLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            Logger.i("Drawer is created.");
        }
        ((ViewGroup) tabs.get(0).getParent()).setVisibility(View.GONE);
        Logger.i("Bottom bar is removed.");
        return true;
    }

    private Class getUserInfoDbCls(String strPrefixCls) throws Throwable {
        for (char a = 'a'; a <= 'z'; ++a) {
            Class cls = findClass(strPrefixCls + a);
            if (cls == null) {
                break;
            }
            if (checkClsUserInfoDb(cls)) {
                return cls;
            }
        }
        return null;
    }

    private boolean checkClsUserInfoDb(Class cls) throws Throwable {
        if (!Modifier.isFinal(cls.getModifiers()) || cls.getSuperclass() == Object.class) {
            return false;
        }
        Field[] fields = cls.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType() != String[].class || !Modifier.isFinal(field.getModifiers())
                    || !Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            field.setAccessible(true);
            String[] s = (String[]) field.get(null);
            if (s == null || s.length == 0) {
                continue;
            }
            if (s[0].contains("CREATE TABLE IF NOT EXISTS userinfo")) {
                return true;
            }
        }
        return false;
    }

    private List<Method> getPossibleMethodsOfAvatar() throws Throwable {
        for (char a = 'a'; a <= 'z'; ++a) {
            Class cls = findClass("com.tencent.mm.pluginsdk.ui." + a);
            if (cls == null) {
                break;
            }
            List<Method> methodList = getPossibleMethodsOfAvatar(cls);
            if (methodList != null) {
                return methodList;
            }
        }
        return null;
    }

    private List<Method> getPossibleMethodsOfAvatar(Class clsAvatarDrawable) throws Throwable {
        if (!BitmapDrawable.class.isAssignableFrom(clsAvatarDrawable)
                || !Modifier.isFinal(clsAvatarDrawable.getModifiers())) {
            return null;
        }

        // com.tencent.mm.pluginsdk.ui.a$a in v6.5.4
        Class clsInterface = null;
        for (char a = 'a'; a <= 'b'; ++a) {
            Class cls = findClass(clsAvatarDrawable.getName() + "$" + a);
            if (cls.isInterface()) {
                clsInterface = cls;
                break;
            }
        }
        if (clsInterface == null) {
            return null;
        }
        try {
            clsInterface.getDeclaredMethod("doInvalidate");
        } catch (NoSuchMethodException e) {
            Logger.e("Can't get doInvalidate()");
            return null;
        }

        // com.tencent.mm.pluginsdk.ui.a$b in v6.5.4
        Class clsAvatar = null;
        for (char a = 'a'; a <= 'b'; ++a) {
            Class cls = findClass(clsAvatarDrawable.getName() + "$" + a);
            if (!cls.isInterface() && Modifier.isStatic(cls.getModifiers())
                    && Modifier.isPublic(cls.getModifiers())) {
                clsAvatar = cls;
                break;
            }
        }
        if (clsAvatar == null) {
            return null;
        }

        // com.tencent.mm.pluginsdk.ui.a$b.l() in v6.5.4
        List<Method> methodList = new ArrayList<>();
        Method[] methods = clsAvatar.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isPublic(method.getModifiers()) && Modifier.isStatic(method.getModifiers())
                    && method.getReturnType() == void.class && method.getParameterTypes().length == 2
                    && method.getParameterTypes()[0] == ImageView.class
                    && method.getParameterTypes()[1] == String.class) {
                methodList.add(method);
            }
        }
        if (methodList.isEmpty()) {
            return null;
        }
        return methodList;
    }
}
