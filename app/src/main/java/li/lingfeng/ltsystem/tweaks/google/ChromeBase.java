package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.Utils;
import li.lingfeng.ltsystem.utils.ViewUtils;

public class ChromeBase extends TweakBase {

    protected static final String TABBED_ACTIVITY = "org.chromium.chrome.browser.ChromeTabbedActivity";
    protected static final String CUSTOM_ACTIVITY = "org.chromium.chrome.browser.customtabs.CustomTabActivity";
    protected static final String LOAD_URL_PARAMS = "org.chromium.content_public.browser.LoadUrlParams";
    protected static final String TAB = "org.chromium.chrome.browser.tab.Tab";
    protected static final String TAB_WEB_CONTENTS_DELEGATE_ANDROID = "org.chromium.chrome.browser.tab.TabWebContentsDelegateAndroid";

    protected interface NewMenuCallback {
        void onOptionsItemSelected(Activity activity, String url, boolean isCustomTab);
    }

    protected class MenuInfo {
        public String title;
        public int order;
        public NewMenuCallback selectedCallback;

        public MenuInfo(String title, int order, NewMenuCallback selectedCallback) {
            this.title = title;
            this.order = order;
            this.selectedCallback = selectedCallback;
        }
    }

    private Map<String, MenuInfo> mMenuInfos;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        if (mMenuInfos == null) {
            mMenuInfos = newMenus();
        }
    }

    protected Map<String, MenuInfo> newMenus() {
        return null;
    }

    @Override
    public void com_android_internal_view_menu_MenuBuilder__setGroupVisible__int_boolean(final ILTweaks.MethodParam param) {
        if (mMenuInfos == null) {
            return;
        }
        param.after(() -> {
            int menuId = (int) param.args[0];
            int idPageMenu = ContextUtils.getIdId("PAGE_MENU");
            if (menuId == idPageMenu) {
                boolean visible = (boolean) param.args[1];
                Logger.i("PAGE_MENU visible " + visible);
                Menu menu = (Menu) param.thisObject;
                mMenuInfos.forEach((title, info) -> {
                    addMenu(menu, title, info.order, visible);
                });
            }
        });
    }

    @Override
    public void android_widget_PopupMenu__inflate__int(final ILTweaks.MethodParam param) {
        if (mMenuInfos == null) {
            return;
        }
        param.after(() -> {
            int id = (int) param.args[0];
            int idCustomTabMenu = ContextUtils.getMenuId("custom_tabs_menu");
            Logger.i("Inflate custom_tabs_menu.");
            PopupMenu popupMenu = (PopupMenu) param.thisObject;
            Menu menu = popupMenu.getMenu();
            if (id == idCustomTabMenu) {
                mMenuInfos.forEach((title, info) -> {
                    addMenu(menu, title, info.order, true);
                });
            }
        });
    }

    @Override
    public void android_view_View__setOnClickListener__OnClickListener(ILTweaks.MethodParam param) {
        param.before(() -> {
            View view = (View) param.thisObject;
            Optional.of(view)
                    .filter(LinearLayout.class::isInstance)
                    .map(v -> ViewUtils.findViewByType((ViewGroup) view, TextView.class, 0))
                    .filter(Objects::nonNull)
                    .map(textView -> mMenuInfos.get(((TextView) textView).getText().toString()))
                    .ifPresent(info -> {
                        param.setArg(0, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ViewUtils.dispatchBackKeyEventOnRoot(view);
                                Activity activity = ViewUtils.getActivityFromView(view);
                                try {
                                    String url = getCurrentUrl(activity);
                                    Logger.i("Menu \"" + info.title + "\" is clicked, url " + url);
                                    info.selectedCallback.onOptionsItemSelected(activity, url, isCustomTab(activity));
                                } catch (Throwable e) {
                                    Toast.makeText(activity, "Error.", Toast.LENGTH_SHORT).show();
                                    Logger.stackTrace(e);
                                }
                            }
                        });
                    });
        });
    }

    protected MenuItem addMenu(Menu menu, String title, int order, boolean visible) {
        MenuItem item = Utils.findMenuItemByTitle(menu, title);
        if (visible) {
            if (item == null) {
                Logger.i("Add menu \"" + title + "\"");
                item = menu.add(Menu.NONE, Menu.NONE, order, title);
            }
            Logger.i("Set \"" + title + "\" visible.");
            item.setVisible(true);
        } else {
            if (item != null) {
                Logger.i("Set \"" + title + "\" invisible.");
                item.setVisible(false);
            }
        }
        return item;
    }

    protected void loadUrl(Activity activity, String url) {
        Logger.i("loadUrl " + url);
        try {
            Object tab = getCurrentTab(activity);
            _loadUrl(tab, url);
        } catch (Throwable e) {
            Toast.makeText(activity, "Error.", Toast.LENGTH_SHORT).show();
            Logger.stackTrace(e);
        }
    }

    protected void loadUrl(Object tab, String url) {
        Logger.i("loadUrl " + url + " on tab " + tab);
        try {
            _loadUrl(tab, url);
        } catch (Throwable e) {
            Toast.makeText(ILTweaks.currentApplication(), "Error.", Toast.LENGTH_SHORT).show();
            Logger.stackTrace(e);
        }
    }

    private void _loadUrl(Object tab, String url) throws Throwable {
        final Class clsLoadUrlParams = findClass(LOAD_URL_PARAMS);
        Method method = Utils.findMethodFromList(tab.getClass().getDeclaredMethods(), new Utils.FindMethodCallback() {
            @Override
            public boolean onMethodCheck(Method m) {
                return Modifier.isPublic(m.getModifiers()) && m.getParameterTypes().length == 1
                        && m.getParameterTypes()[0] == clsLoadUrlParams && m.getReturnType() == int.class;
            }
        });
        Object loadUrlParams = ConstructorUtils.invokeConstructor(clsLoadUrlParams, url);
        int ret = (int) method.invoke(tab, loadUrlParams);
        Logger.d("loadUrl return " + ret);
    }

    protected Object getCurrentTab(Activity activity) throws Throwable {
        Method[] methods = activity.getClass().getMethods();
        Method method = Utils.findMethodFromList(methods, new Utils.FindMethodCallback() {
            @Override
            public boolean onMethodCheck(Method m) {
                return m.getReturnType().getName().equals(TAB) && m.getParameterTypes().length == 0;
            }
        });
        if (method == null) {
            throw new RuntimeException("Can't find getCurrentTab method.");
        }
        return method.invoke(activity);
    }

    protected String getCurrentUrl(Activity activity) throws Throwable {
        Object activityTab = getCurrentTab(activity);
        return (String) MethodUtils.invokeMethod(activityTab, "getUrl");
    }

    protected boolean isCustomTab(Activity activity) {
        return activity.getClass().getName().equals(CUSTOM_ACTIVITY);
    }
}
