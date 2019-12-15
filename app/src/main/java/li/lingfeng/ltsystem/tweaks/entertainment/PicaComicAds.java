package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.PICA_COMIC, prefs = R.string.key_pica_comic_ads)
public class PicaComicAds extends TweakBase {

    private static final String MAIN_ACTIVITY = "com.picacomic.fregata.activities.MainActivity";
    private static final String COMIC_VIEWER_ACTIVITY = "com.picacomic.fregata.activities.ComicViewerActivity";
    private WeakReference<ViewGroup> mComicList;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();

            Logger.v("Remove popupWebview bannerWebview");
            View popupWebview = ViewUtils.findViewByName(rootView, "popupWebview");
            ViewUtils.removeView(popupWebview);
            View bannerWebview = ViewUtils.findViewByName(rootView, "bannerWebview");
            ViewUtils.removeView(bannerWebview);

            rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                ViewGroup comicList = (ViewGroup) ViewUtils.findViewByName(rootView, "recyclerView_comic_list");
                if (comicList != null && (mComicList == null || mComicList.get() != comicList)) {
                    Logger.d("comicList " + comicList);
                    mComicList = new WeakReference<>(comicList);
                    comicList.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            if (child instanceof ViewGroup) {
                                ViewGroup viewGroup = (ViewGroup) child;
                                child = viewGroup.getChildAt(0);
                                if (child instanceof WebView) {
                                    Logger.v("Remove " + child);
                                    ViewUtils.removeView(child);
                                    TextView textView = new TextView(activity);
                                    textView.setText("   ↓");
                                    textView.setTextColor(Color.BLACK);
                                    textView.setTextSize(24);
                                    viewGroup.addView(textView);
                                }
                            }
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                        }
                    });
                }
            });
        });

        afterOnClass(COMIC_VIEWER_ACTIVITY, param, () -> {
            new Handler().post(() -> {
                try {
                    Activity activity = (Activity) param.thisObject;
                    ViewGroup listView = (ViewGroup) ViewUtils.findViewByName(activity, "recyclerView_comic_viewer");
                    listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                        @Override
                        public void onChildViewAdded(View parent, View child) {
                            if (child instanceof ViewGroup) {
                                ViewGroup viewGroup = (ViewGroup) child;
                                child = viewGroup.getChildAt(0);
                                if (child instanceof WebView) {
                                    Logger.v("Remove " + child);
                                    ViewUtils.removeView(child);
                                    TextView textView = new TextView(activity);
                                    textView.setText("   ↓");
                                    textView.setTextColor(Color.BLACK);
                                    textView.setTextSize(24);
                                    viewGroup.addView(textView);
                                }
                            }
                        }

                        @Override
                        public void onChildViewRemoved(View parent, View child) {
                        }
                    });
                } catch (Throwable e) {
                    Logger.e("Remove ads from comic viewer exception.", e);
                }
            });
        });
    }
}
