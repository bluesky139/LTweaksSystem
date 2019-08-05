package li.lingfeng.ltsystem.tweaks.shopping;

import android.app.Activity;
import android.content.Intent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.ELE, prefs = R.string.key_ele_route_share_link)
public class EleRouteShareLink extends TweakBase {

    private static final String ROUTE_ACTIVITY = "me.ele.application.ui.Launcher.SchemeRouteActivity";
    private static final String HOME_ACTIVITY = "me.ele.application.ui.home.HomeActivity";
    private static final String SHOP_ACTIVITY = "me.ele.shopping.ui.shop.classic.ShopActivity";
    private String mShopId;
    private String mFoodId;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        beforeOnClass(ROUTE_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            String url = activity.getIntent().getDataString();
            Logger.d("ele url " + url);
            Pattern pattern = Pattern.compile("\\/#id=(\\w+)(&food_id=(\\w+))?");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                mShopId = matcher.group(1);
                if (matcher.groupCount() == 3) {
                    mFoodId = matcher.group(3);
                } else {
                    mFoodId = null;
                }
                Logger.d("mShopId " + mShopId + ", mFoodId " + mFoodId);
            }
        });
        afterOnClass(HOME_ACTIVITY, param, () -> {
            if (mShopId != null) {
                Logger.i("Start shop activity with shopId " + mShopId + ", foodId " + mFoodId);
                Activity activity = (Activity) param.thisObject;
                Intent intent = new Intent();
                intent.setClassName(activity, SHOP_ACTIVITY);
                intent.putExtra("restaurant_id", mShopId);
                if (mFoodId != null) {
                    intent.putExtra("target_food_id", mFoodId);
                }
                activity.startActivity(intent);
                mShopId = null;
                mFoodId = null;
            }
        });
    }
}
