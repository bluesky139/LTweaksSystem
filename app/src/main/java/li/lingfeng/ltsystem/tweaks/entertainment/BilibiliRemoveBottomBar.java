package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.SimpleDrawer;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_remove_bottom_bar)
public class BilibiliRemoveBottomBar extends TweakBase {

    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";
    private static final String GENERAL_ACTIVITY = "com.bilibili.lib.ui.GeneralActivity";
    private static final String FAVORITES_ACTIVITY = "tv.danmaku.bili.ui.favorite.FavoriteBoxActivity";
    private SimpleDrawer mDrawerLayout;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            new Handler().post(() -> {
                try {
                    hookBottomBar(activity);
                } catch (Throwable e) {
                    Logger.e("Can't hookBottomBar.");
                    Logger.stackTrace(e);
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        beforeOnClass(MAIN_ACTIVITY, param, () -> {
            mDrawerLayout = null;
        });
    }

    private void hookBottomBar(final Activity activity) throws Throwable {
        final ViewGroup rootView = activity.findViewById(android.R.id.content);
        final ViewGroup bottomNav = ViewUtils.findViewByName(rootView, "bottom_navigation");
        List<FrameLayout> layouts = ViewUtils.findAllViewByTypeInSameHierarchy(bottomNav, FrameLayout.class, 4);

        final SimpleDrawer.NavItem[] navItems = new SimpleDrawer.NavItem[layouts.size() + 2];
        for (int i = 0; i < layouts.size(); ++i) {
            FrameLayout layout = layouts.get(i);
            ImageView imageView = ViewUtils.findViewByName(layout, "tab_icon");
            if (imageView == null) {
                Logger.w("Can't get imageview from bottom button.");
                return;
            }
            TextView textView = ViewUtils.findViewByType(layout, TextView.class);
            if (textView == null) {
                Logger.w("Can't get textview from bottom button.");
                return;
            }
            SimpleDrawer.NavItem navItem = new SimpleDrawer.NavItem(imageView.getDrawable(), textView.getText(), layout);
            navItems[i] = navItem;
        }

        SimpleDrawer.NavItem navItem = new SimpleDrawer.NavItem(new ColorDrawable(), "History", (View.OnClickListener) v -> {
            Intent intent = new Intent();
            intent.setClassName(PackageNames.BILIBILI, GENERAL_ACTIVITY);
            intent.putExtra("fragment", "com.bilibili.app.history.HistoryFragmentV3");
            activity.startActivity(intent);
        });
        navItems[layouts.size()] = navItem;

        navItem = new SimpleDrawer.NavItem(new ColorDrawable(), "Favorites", (View.OnClickListener) v -> {
            Intent intent = new Intent();
            intent.setClassName(PackageNames.BILIBILI, FAVORITES_ACTIVITY);
            activity.startActivity(intent);
        });
        navItems[layouts.size() + 1] = navItem;

        SimpleDrawer.NavItem headerItem = new SimpleDrawer.NavItem(ContextUtils.getAppIcon(),
                ContextUtils.getAppName(), null);

        FrameLayout allView = ViewUtils.rootChildsIntoOneLayout(activity);
        mDrawerLayout = new SimpleDrawer(activity, allView, navItems, headerItem);
        mDrawerLayout.updateHeaderBackground(0xFF3B3B3B);
        mDrawerLayout.updateNavListBackground(0xFF3B3B3B);
        mDrawerLayout.updateNavListTextColor(0xFFDADADA);
        rootView.addView(mDrawerLayout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        Logger.i("Simple drawer is created.");
        bottomNav.setVisibility(View.GONE);
    }
}
