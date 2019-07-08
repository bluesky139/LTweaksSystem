package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

import static li.lingfeng.ltsystem.utils.ContextUtils.dp2px;

@MethodsLoad(packages = PackageNames.BILIBILI, prefs = R.string.key_bilibili_remove_bottom_bar)
public class BilibiliRemoveBottomBar extends TweakBase {

    private static final String MAIN_ACTIVITY = "tv.danmaku.bili.MainActivityV2";
    private LinearLayout mNavHeader;
    private SimpleDrawer.NavItem[] mNavItems;
    private ListView mListView;
    private CheckedTextView mHomeTextView;
    private ViewGroup mBottomNav;

    private TextView mNickView;
    private boolean mHookedDynamicPageTab = false;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            final Activity activity = (Activity) param.thisObject;
            final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                try {
                    if (mBottomNav != null) {
                        if (!ViewUtils.isViewInWindow(mBottomNav)) {
                            mBottomNav = null;
                        }
                    }
                    if (mBottomNav == null) {
                        hookBottomBar(activity);
                    }
                    if (mNavHeader != null) {
                        int[] pos = new int[2];
                        mNavHeader.getLocationInWindow(pos);
                        if (pos[0] == 0) {
                            mListView.requestFocus();
                        }
                    }
                    //tabClickToTopInDynamicPage(rootView);
                } catch (Throwable e) {
                    Logger.e("Can't hook bottom bar or dynamic page, " + e);
                    Logger.stackTrace(e);
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        beforeOnClass(MAIN_ACTIVITY, param, () -> {
            mNavHeader = null;
            mNavItems = null;
            mListView = null;
            mHomeTextView = null;
            mBottomNav = null;
            mNickView = null;
            mHookedDynamicPageTab = false;
        });
    }

    private void hookBottomBar(final Activity activity) throws Throwable {
        Logger.v("hookBottomBar");
        int idNav = ContextUtils.getIdId("design_navigation_view");
        ViewGroup nav = (ViewGroup) activity.findViewById(idNav);
        if (nav == null || nav.getChildCount() == 0) {
            Logger.w("Nav child count 0.");
            return;
        }

        List<CheckedTextView> textViews = ViewUtils.findAllViewByType(nav, CheckedTextView.class);
        for (CheckedTextView textView : textViews) {
            String text = textView.getText().toString();
            if ("首页".equals(text) || "Home".equals(text)) {
                //Logger.v("Got home textview.");
                mHomeTextView = textView;
                break;
            }
        }
        if (mHomeTextView == null) {
            Logger.w("Home textview is null.");
            return;
        }

        int idBottomNav = ContextUtils.getIdId("bottom_navigation");
        final ViewGroup bottomNav = (ViewGroup) activity.findViewById(idBottomNav);
        if (bottomNav == null) {
            hideDefaultHomeFromDrawer();
            return;
        }
        List<FrameLayout> layouts = ViewUtils.findAllViewByTypeInSameHierarchy(bottomNav, FrameLayout.class, 4);
        if (layouts.size() == 0) {
            return;
        }

        final ViewGroup rootView = (ViewGroup) activity.findViewById(android.R.id.content);
        if (mNavItems == null) {
            Logger.d("Got " + layouts.size() + " bottom buttons.");
            final SimpleDrawer.NavItem[] navItems = new SimpleDrawer.NavItem[layouts.size()];
            for (int i = 0; i < layouts.size(); ++i) {
                FrameLayout layout = layouts.get(i);
                ImageView imageView = ViewUtils.findViewByType(layout, ImageView.class);
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
            mNavItems = navItems;

            new Handler().post(() -> {
                try {
                    mNavHeader = (LinearLayout) ViewUtils.findViewByName(activity, "navigation_header_container");
                    mListView = new ListView(activity);
                    ButtonListAdapter listAdapter = new ButtonListAdapter(activity, navItems, mHomeTextView.getMeasuredHeight());
                    mListView.setAdapter(listAdapter);
                    mListView.setOnItemClickListener(listAdapter);
                    mListView.setDividerHeight(0);
                    mNavHeader.addView(mListView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            mHomeTextView.getMeasuredHeight() * navItems.length));
                    Logger.i("Drawer is created.");

                    ViewGroup contentView = (ViewGroup) ViewUtils.findViewByName(rootView, "content");
                    int height = rootView.getMeasuredHeight();
                    if (contentView.getMeasuredHeight() < height) {
                        Logger.d("Set contentView height to android content height " + height);
                        contentView.getLayoutParams().height = height;
                    }
                } catch (Throwable e) {
                    Logger.e("Drawer create exception.", e);
                }
            });
        } else {
            for (int i = 0; i < layouts.size(); ++i) {
                FrameLayout layout = layouts.get(i);
                mNavItems[i].mClickObj = layout;
            }
        }

        if (hideDefaultHomeFromDrawer()) {
            bottomNav.setVisibility(View.GONE);
        }
        mBottomNav = bottomNav;

        if (mListView != null) {
            ViewGroup contentView = (ViewGroup) ViewUtils.findViewByName(rootView, "content");
            int height = rootView.getMeasuredHeight();
            if (contentView.getMeasuredHeight() < height) {
                Logger.d("Set contentView height to android content height " + height);
                contentView.getLayoutParams().height = height;
            }
        }
    }

    private boolean hideDefaultHomeFromDrawer() {
        return true;
        /*if (mHomeTextView.getVisibility() != View.GONE) {
            Logger.d("Hide default home from drawer.");
            mHomeTextView.setVisibility(View.GONE);
            ViewGroup parent = ((ViewGroup) mHomeTextView.getParent());
            parent.setMinimumHeight(0);
            parent.getLayoutParams().height = 0;
            parent.setBackgroundColor(Color.TRANSPARENT);
            return true;
        }
        return false;*/
    }

    class ButtonListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener, ViewTreeObserver.OnGlobalLayoutListener {

        Activity mActivity;
        ViewGroup mRootView;
        SimpleDrawer.NavItem[] mNavItems;
        View[] mViews;
        int mItemClicked;
        int mItemHeight;

        public ButtonListAdapter(Activity activity, SimpleDrawer.NavItem[] navItems, int itemHeight) {
            mActivity = activity;
            mRootView = (ViewGroup) activity.findViewById(android.R.id.content);
            mNavItems = navItems;
            mViews = new ViewGroup[navItems.length];
            mItemClicked = -1;
            mItemHeight = itemHeight;
        }

        @Override
        public int getCount() {
            return mNavItems.length;
        }

        @Override
        public SimpleDrawer.NavItem getItem(int position) {
            return mNavItems[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = mViews[position];
            if (view != null) {
                return view;
            }

            FrameLayout frameLayout = new FrameLayout(mActivity);
            CheckedTextView textView = new CheckedTextView(mActivity);
            int idTextAppearance = ContextUtils.getThemeId("TextAppearance.AppCompat.Body2");
            textView.setTextAppearance(mActivity, idTextAppearance);
            final SimpleDrawer.NavItem navItem = getItem(position);
            textView.setCompoundDrawables(navItem.mIcon, null, null, null);
            textView.setText(navItem.mText);
            textView.setCompoundDrawablePadding(dp2px(24));
            textView.setPadding(dp2px(20), 0, 0, 0);
            textView.setLines(1);
            textView.setGravity(Gravity.CENTER_VERTICAL);

            frameLayout.addView(textView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight));
            mViews[position] = frameLayout;
            return frameLayout;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Logger.i("Nav item click " + position);
            if (mItemClicked >= 0) {
                Logger.e("Nav item last click is not finished.");
                return;
            }
            ((View) mHomeTextView.getParent()).performClick();
            mItemClicked = position;
            mRootView.getViewTreeObserver().addOnGlobalLayoutListener(this);
        }

        @Override
        public void onGlobalLayout() {
            int idBottomNav = ContextUtils.getIdId("bottom_navigation");
            ViewGroup bottomNav = (ViewGroup) mActivity.findViewById(idBottomNav);
            if (bottomNav == null) {
                return;
            }
            List<FrameLayout> layouts = ViewUtils.findAllViewByTypeInSameHierarchy(bottomNav, FrameLayout.class, mNavItems.length);
            if (layouts.size() != 0) {
                Logger.d("Click bottom button " + mItemClicked);
                layouts.get(mItemClicked).performClick();
                mItemClicked = -1;
                bottomNav.setVisibility(View.GONE);
            }
            mRootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }

    private void tabClickToTopInDynamicPage(ViewGroup rootView) throws Throwable {
        if (mHookedDynamicPageTab) {
            return;
        }
        if (mNickView == null) {
            mNickView = (TextView) ViewUtils.findViewByName(rootView, "nick_name");
        }
        if (mNickView != null && "动态".equals(mNickView.getText().toString())) {
            ViewGroup tabs = (ViewGroup) ViewUtils.findViewByName(rootView, "tabs");
            List<View> tabRoots = ViewUtils.findAllViewByName(tabs, "tab_root");
            View.OnClickListener listener = ViewUtils.getViewClickListener(tabRoots.get(1));
            /*findAndHookMethod(listener.getClass(), "onClick", View.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    View view = (View) param.args[0];
                    int intValue = ((Integer) view.getTag()).intValue();
                    Object pagerSlidingTabStrip = XposedUtils.getSurroundingThis(param.thisObject);
                    Field field = XposedHelpers.findFirstFieldByExactType(pagerSlidingTabStrip.getClass(), findClass(ClassNames.VIEW_PAGER));
                    Object viewPager = field.get(pagerSlidingTabStrip);
                    int current = (int) XposedHelpers.callMethod(viewPager, "getCurrentItem");
                    if (current == intValue) {
                        Logger.i("Click on same dynamic tab, go to top.");
                        ((View) mNavItems[2].mClickObj).performClick();
                    }
                }
            });*/
            mHookedDynamicPageTab = true;
            Logger.i("Hooked dynamic page tab.");
        }
    }
}
