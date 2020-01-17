package li.lingfeng.ltsystem.tweaks.system;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.SOLID_EXPLORER, prefs = {})
public class SolidExplorerReplaceStreamingUrl extends TweakBase {

    private static final String MAIN_ACTIVITY = "pl.solidexplorer.SolidExplorer";
    private static final String STREAMING_SERVICE = "pl.solidexplorer.files.stream.MediaStreamingService";
    private List<View> mHeaderViews;
    private Map<String, String> mServerMap;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MAIN_ACTIVITY, param, () -> {
            String value = Prefs.instance().getString(R.string.key_solid_explorer_replace_streaming_url, "");
            String[] values = StringUtils.split(value, ',');
            if (values.length == 0) {
                return;
            }
            mServerMap = new HashMap<>(values.length);
            for (int i = 0; i < values.length; ++i) {
                String[] map = StringUtils.split(values[i], ':');
                mServerMap.put(map[0], map[1]);
                Logger.i("Streaming url replace: " + map[0] + " -> " + map[1]);
            }

            Activity activity = (Activity) param.thisObject;
            ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
            rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        mHeaderViews = ViewUtils.findAllViewByName(rootView, "smart_header");
                        if (mHeaderViews.size() == 2) {
                            rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    } catch (Throwable e) {
                        Logger.e("onGlobalLayout exception.", e);
                    }
                }
            });
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        beforeOnClass(MAIN_ACTIVITY, param, () -> {
            mHeaderViews = null;
            mServerMap = null;
        });
    }

    @Override
    public void android_app_Activity__startActivityForResult__Intent_int_Bundle(ILTweaks.MethodParam param) {
        beforeOnClass(MAIN_ACTIVITY, param, () -> {
            if (mServerMap == null) {
                return;
            }
            Intent intent = (Intent) param.args[0];
            if (intent.getBooleanExtra("streaming", false)) {
                String rootFolder = getCurrentRootFolder();
                String server = mServerMap.get(rootFolder);
                if (server != null) {
                    String url = intent.getDataString();
                    String newUrl = url.replaceFirst("^http:\\/\\/127\\.0\\.0\\.1:\\d+\\/", "http://" + server + "/");
                    intent.setData(Uri.parse(newUrl));
                    Logger.v("Streaming url: " + newUrl);

                    // Stop streaming service
                    Activity activity = (Activity) param.thisObject;
                    intent = new Intent();
                    intent.setClassName(PackageNames.SOLID_EXPLORER, STREAMING_SERVICE);
                    intent.putExtra("extra_id", 1);
                    activity.startService(intent);
                }
            }
        });
    }

    private String getCurrentRootFolder() {
        int[] location = new int[2];
        mHeaderViews.get(0).getLocationOnScreen(location);
        return getRootFolderByPannelId(location[0] == 0  ? 0 : 1);
    }

    private String getRootFolderByPannelId(int panelId) {
        ViewGroup headerView = (ViewGroup) mHeaderViews.get(panelId);
        View rootSwitchView = ViewUtils.findViewByName(headerView, "root_switch");
        TextView rootTextView = (TextView) ViewUtils.nextView(rootSwitchView);
        return rootTextView.getText().toString();
    }
}
