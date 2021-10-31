package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_expand_subscribed_list)
public class WeChatExpandSubscribedList extends TweakBase {

    private static final String BIZ_TIMELINE_UI = "com.tencent.mm.plugin.brandservice.ui.timeline.BizTimeLineUI";
    private static final String STORY_LIST_VIEW = "com.tencent.mm.plugin.bizui.widget.StoryListView";
    private Handler mHandler;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(BIZ_TIMELINE_UI, param, () -> {
            mHandler = new Handler();
            Activity activity = (Activity) param.thisObject;
            new Handler().post(() -> {
                try {
                    handleList(activity);
                } catch (Throwable e) {
                    Logger.e("Handle StoryListView exception.", e);
                }
            });
        });
    }

    private void handleList(Activity activity) throws Throwable {
        ViewGroup listView = (ViewGroup) ViewUtils.findViewByType(activity, findClass(STORY_LIST_VIEW));
        for (int i = 0; i < listView.getChildCount(); ++i) {
            checkListItem(listView.getChildAt(i));
        }
        listView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View child) {
                checkListItem(child);
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
            }
        });
    }

    private void checkListItem(View view) {
        try {
            if (view instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) view;
                view = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
                if (view instanceof ViewGroup) {
                    ViewGroup expandableView = (ViewGroup) view;
                    TextView textView = ViewUtils.findViewByType(expandableView, TextView.class);
                    if (textView.getText().toString().endsWith(" article(s) remaining")) {
                        mHandler.post(() -> {
                            Logger.v("Expand article list.");
                            expandableView.performClick();
                        });
                    }
                }
            }
        } catch (Throwable e) {
            Logger.e("checkListItem exception.", e);
        }
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        afterOnClass(BIZ_TIMELINE_UI, param, () -> {
            mHandler = null;
        });
    }
}
