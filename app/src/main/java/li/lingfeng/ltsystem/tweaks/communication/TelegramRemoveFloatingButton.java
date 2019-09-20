package li.lingfeng.ltsystem.tweaks.communication;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.TELEGRAM, prefs = R.string.key_telegram_remove_floating_button)
public class TelegramRemoveFloatingButton extends TweakBase {

    private static final String LAUNCH_ACTIVITY = "org.telegram.ui.LaunchActivity";
    private static final String ACTION_BAR_LAYOUT = "org.telegram.ui.ActionBar.ActionBarLayout";
    private static final String DIALOGS_ACTIVITY = "org.telegram.ui.DialogsActivity"; // main fragment
    private static final String DIALOGS_ACTIVITY_VIEW = "org.telegram.ui.DialogsActivity$ContentView";

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(LAUNCH_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            ViewGroup actionBarLayout = (ViewGroup) ViewUtils.findViewByType(activity, findClass(ACTION_BAR_LAYOUT));
            removeFloatingButton(actionBarLayout);

            ViewGroup containerViewBack = (ViewGroup) ReflectUtils.getObjectField(actionBarLayout, "containerViewBack");
            containerViewBack.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
                @Override
                public void onChildViewAdded(View parent, View child) {
                    if (child.getClass().getName().equals(DIALOGS_ACTIVITY_VIEW)) {
                        try {
                            removeFloatingButton(actionBarLayout);
                        } catch (Throwable e) {
                            Logger.e("removeFloatingButton exception.", e);
                        }
                        containerViewBack.setOnHierarchyChangeListener(null);
                    }
                }

                @Override
                public void onChildViewRemoved(View parent, View child) {
                }
            });
        });
    }

    private void removeFloatingButton(ViewGroup actionBarLayout) throws Throwable {
        ArrayList fragmentsStack = (ArrayList) ReflectUtils.getObjectField(actionBarLayout, "fragmentsStack");
        Object fragment = fragmentsStack.stream()
                .filter(f -> f.getClass().getName().equals(DIALOGS_ACTIVITY))
                .findFirst().get();
        Logger.d("fragment " + fragment);

        View floatingButton = (View) ReflectUtils.getObjectField(fragment, "floatingButtonContainer");
        Logger.d("floatingButton " + floatingButton);
        if (floatingButton != null && floatingButton.getParent() != null) {
            ViewUtils.removeView(floatingButton);
        }
    }
}
