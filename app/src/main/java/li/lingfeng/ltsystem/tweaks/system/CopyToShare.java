package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.net.Uri;
import android.os.PersistableBundle;
import android.view.Menu;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

public class CopyToShare extends TweakBase {

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_system_share_copy_to_share)
    public static class Android extends TweakBase {

        @Override
        public void com_android_server_clipboard_ClipboardService__setPrimaryClipInternal__PerUserClipboard_ClipData_int(ILTweaks.MethodParam param) {
            param.before(() -> {
                ClipData clip = (ClipData) param.args[1];
                if (clip == null || clip.getItemCount() == 0) {
                    return;
                }
                ClipDescription desc = clip.getDescription();
                if (desc == null) {
                    return;
                }

                int uid = (int) param.args[2];
                Logger.d("Clipboard uid " + uid);
                PersistableBundle extras = desc.getExtras();
                if (extras == null) {
                    extras = new PersistableBundle(1);
                    extras.putInt("ltweaks_clip_uid", uid);
                    desc.setExtras(extras);
                } else {
                    extras.putInt("ltweaks_clip_uid", uid);
                }
            });
        }
    }


    @MethodsLoad(packages = {}, prefs = R.string.key_system_share_copy_to_share, excludedPackages = {
            PackageNames.ANDROID, PackageNames.ANDROID_SYSTEM_UI
    }, hiddenApiExemptions = "Landroid/widget/Editor$ProcessTextIntentActionsHandler;")
    public static class All extends TweakBase {

        @Override
        public void android_widget_Editor$ProcessTextIntentActionsHandler__onInitializeMenu__Menu(ILTweaks.MethodParam param) {
            param.after(() -> {
                Activity activity = (Activity) ReflectUtils.getObjectField(param.thisObject, "mContext");
                int clipUid = activity.getIntent().getIntExtra("ltweaks_clip_uid", 0);
                if (clipUid > 0) {
                    Menu menu = (Menu) param.args[0];
                    for (int i = 0; i < menu.size(); ++i) {
                        Intent intent = menu.getItem(i).getIntent();
                        if (intent != null && intent.getComponent() != null
                                && PackageNames.L_TWEAKS.equals(intent.getComponent().getPackageName())) {
                            intent.putExtra("ltweaks_clip_uid", clipUid);
                        }
                    }
                    return;
                }

                Uri referrer = activity.getReferrer();
                Menu menu = (Menu) param.args[0];
                for (int i = 0; i < menu.size(); ++i) {
                    Intent intent = menu.getItem(i).getIntent();
                    if (intent != null && intent.getComponent() != null
                            && PackageNames.L_TWEAKS.equals(intent.getComponent().getPackageName())) {
                        intent.putExtra(Intent.EXTRA_REFERRER, referrer);
                    }
                }
            });
        }
    }


}
