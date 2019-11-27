package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

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

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            Logger.v("Remove popupWebview bannerWebview");
            Activity activity = (Activity) param.thisObject;
            View popupWebview = ViewUtils.findViewByName(activity, "popupWebview");
            ViewUtils.removeView(popupWebview);
            View bannerWebview = ViewUtils.findViewByName(activity, "bannerWebview");
            ViewUtils.removeView(bannerWebview);
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
