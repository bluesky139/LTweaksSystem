package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.ELE, prefs = R.string.key_ele_address_map)
public class EleAddressMap extends TweakBase {

    private static final String SHOP_ACTIVITY = "me.ele.shopping.ui.shop.classic.ShopActivity";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(SHOP_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        List<View> loadingLayouts = ViewUtils.findAllViewByName(rootView, "loading_layout");
                        if (loadingLayouts.size() != 3) {
                            return;
                        }
                        rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        ViewGroup loadingLayout = (ViewGroup) loadingLayouts.get(2);
                        ViewGroup listView = (ViewGroup) ViewUtils.findViewByName(loadingLayout, "list");
                        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                            @Override
                            public void onChildViewAdded(View parent, View child) {
                                TextView textView = (TextView) ViewUtils.findViewByName(listView, "address");
                                if (textView != null) {
                                    textView.setOnClickListener((v) -> {
                                        String address = textView.getText().toString();
                                        Logger.v("Shop address " + address);
                                        try {
                                            Intent intent = new Intent();
                                            intent.setData(Uri.parse("baidumap://map/geocoder?src=" + PackageNames.ELE + "&address=" + address));
                                            activity.startActivity(intent);
                                        } catch (Throwable e) {
                                            Logger.e("Start baidumap exception.", e);
                                        }
                                    });
                                    listView.setOnHierarchyChangeListener(null);
                                }
                            }

                            @Override
                            public void onChildViewRemoved(View parent, View child) {
                            }
                        });
                    } catch (Throwable e) {
                        Logger.e("ele address map exception.", e);
                    }
                }
            });
        });
    }
}
