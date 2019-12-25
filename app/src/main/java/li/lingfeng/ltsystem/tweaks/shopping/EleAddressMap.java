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
                                TextView addressTextView = (TextView) ViewUtils.findViewByName(listView, "address");
                                if (addressTextView != null) {
                                    addressTextView.setOnClickListener((v) -> {
                                        String address = addressTextView.getText().toString();
                                        Logger.v("Shop address " + address);
                                        openMap(activity, address);
                                    });
                                    listView.setOnHierarchyChangeListener(null);

                                    TextView nameTextView = (TextView) ViewUtils.findViewByName(listView, "shop_name");
                                    if (nameTextView != null) {
                                        nameTextView.setOnClickListener((v) -> {
                                            String name = nameTextView.getText().toString();
                                            Logger.v("Shop name " + name);
                                            openMap(activity, name);
                                        });
                                    } else {
                                        Logger.w("No shop name.");
                                    }
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

    private void openMap(Activity activity, String address) {
        try {
            Intent intent = new Intent();
            intent.setData(Uri.parse("baidumap://map/geocoder?src=" + PackageNames.ELE + "&address=" + address));
            activity.startActivity(intent);
        } catch (Throwable e) {
            Logger.e("Start baidumap exception.", e);
        }
    }
}
