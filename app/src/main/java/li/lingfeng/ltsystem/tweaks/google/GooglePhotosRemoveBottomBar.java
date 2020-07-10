package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.SimpleDrawer;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.GOOGLE_PHOTOS, prefs = R.string.key_google_photos_remove_bottom_bar)
public class GooglePhotosRemoveBottomBar extends TweakBase {

    private static final String HOME_ACTIVITY = "com.google.android.apps.photos.home.HomeActivity";
    private static final String TAB_BAR_BUTTON = "com.google.android.apps.photos.tabbar.TabBarButton";
    private SimpleDrawer mDrawerLayout;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(final ILTweaks.MethodParam param) {
        afterOnClass(HOME_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            new Handler().post(() -> {
                try {
                    hookBottomBar(activity);
                } catch (Throwable e) {
                    Logger.e("Can't hookBottomBar.", e);
                }
            });
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        });
    }

    private void hookBottomBar(Activity activity) throws Throwable {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        ViewGroup tabBar = ViewUtils.findViewByName(rootView, "tab_bar");
        Object[] buttons = ViewUtils.findAllViewByTypeInSameHierarchy(tabBar, findClass(TAB_BAR_BUTTON), 3)
                .stream().filter(v -> ((View) v).getVisibility() == View.VISIBLE).toArray();

        SimpleDrawer.NavItem[] navItems = new SimpleDrawer.NavItem[buttons.length];
        for (int i = 0; i < buttons.length; ++i) {
            Button button = (Button) buttons[i];
            SimpleDrawer.NavItem navItem = new SimpleDrawer.NavItem(button.getCompoundDrawables()[1], button.getText(), button);
            navItems[i] = navItem;
        }
        SimpleDrawer.NavItem headerItem = new SimpleDrawer.NavItem(ContextUtils.getAppIcon(),
                ContextUtils.getAppName(), null);

        FrameLayout allView = ViewUtils.rootChildsIntoOneLayout(activity);
        mDrawerLayout = new SimpleDrawer(activity, allView, navItems, headerItem);
        mDrawerLayout.updateHeaderBackground(0xFF202124);
        mDrawerLayout.updateNavListBackground(0xFF202124);
        mDrawerLayout.updateNavListTextColor(Color.WHITE);
        rootView.addView(mDrawerLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Logger.i("Simple drawer is created with " + buttons.length + " buttons.");

        new Handler().post(() -> {
            try {
                tabBar.setVisibility(View.GONE);
                View recyclerView = ViewUtils.findViewByName(rootView, "recycler_view");
                int oldHeight = recyclerView.getHeight();
                int newHeight = ((View) recyclerView.getParent()).getHeight();
                Logger.d("Recycler view oldHeight " + oldHeight + ", parentHeight " + newHeight);
                if (oldHeight > 0 && newHeight > 0) {
                    Logger.d("Set recycler view height " + newHeight);
                    recyclerView.getLayoutParams().height = newHeight;
                }
                recyclerView.requestLayout();
            } catch (Throwable e) {
                Logger.e("Hide tabBar exception.", e);
            }
        });
    }

    @Override
    public void android_app_Activity__onKeyUp__int_KeyEvent(ILTweaks.MethodParam param) {
        beforeOnBackPressed(HOME_ACTIVITY, param, () -> {
            if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                Logger.i("Back is pressed for closing drawer.");
                mDrawerLayout.closeDrawers();
                param.setResult(true);
            }
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(HOME_ACTIVITY, param, () -> {
            mDrawerLayout = null;
        });
    }
}