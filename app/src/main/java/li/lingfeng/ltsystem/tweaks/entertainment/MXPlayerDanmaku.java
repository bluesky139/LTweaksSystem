package li.lingfeng.ltsystem.tweaks.entertainment;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = { PackageNames.MX_PLAYER_PRO, PackageNames.MX_PLAYER_FREE }, prefs = R.string.key_mxplayer_danmaku)
public class MXPlayerDanmaku extends TweakBase {

    public static final int OP_CREATE       = 0;
    public static final int OP_SHOW_CONTROL = 1;
    public static final int OP_HIDE_CONTROL = 2;
    public static final int OP_SHOW_ALL     = 3;
    public static final int OP_HIDE_ALL     = 4;
    public static final int OP_SEEK_TO      = 5;
    public static final int OP_RESUME       = 6;
    public static final int OP_PAUSE        = 7;
    public static final int OP_DESTROY      = 8;

    private static final String ACTIVITY_SCREEN_PRO = "com.mxtech.videoplayer.ActivityScreen";
    private static final String ACTIVITY_SCREEN_FREE = "com.mxtech.videoplayer.ad.ActivityScreen";

    private boolean mPlaying;
    private boolean mInMXPLayer = false;
    private int mControllerId = -1;

    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(getActivityString(), param, () -> {
            Activity activity = (Activity) param.thisObject;
            ContentValues values = new ContentValues(2);
            values.put("file_path", "file:///sdcard/みなみけ ただいま (Creditless OP) BDrip x264-ank [XV");
            values.put("video_duration", 92);
            sendCommand(OP_CREATE, values);
            mPlaying = true;
            mControllerId = ContextUtils.getIdId("controller");

            TextView posText = ViewUtils.findViewByName(activity, "posText");
            posText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String pos = s.toString();
                    Logger.v("Update pos " + pos);
                    String[] strings = StringUtils.split(pos, ':');
                    int seconds = Integer.parseInt(strings[strings.length - 1]) + Integer.parseInt(strings[strings.length - 2]) * 60
                            + (strings.length == 3 ? Integer.parseInt(strings[0]) * 3600 : 0);
                    ContentValues values = new ContentValues(1);
                    values.put("seconds", seconds);
                    sendCommand(OP_SEEK_TO, values);
                }
            });

            ImageButton playButton = ViewUtils.findViewByName(activity, "playpause");
            View.OnClickListener originalListener = ViewUtils.getViewClickListener(playButton);
            playButton.setOnClickListener(v -> {
                originalListener.onClick(v);
                mPlaying = !mPlaying;
                Logger.v("Play/Pause button clicked, mPlaying " + mPlaying);
                sendCommand(mPlaying ? OP_RESUME : OP_PAUSE);
            });
        });
    }

    @Override
    public void android_view_View__setVisibility__int(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (!mInMXPLayer) {
                return;
            }
            View view = (View) param.thisObject;
            if (view.getId() == mControllerId) {
                int visibility = (int) param.args[0];
                if (visibility == View.VISIBLE) {
                    sendCommand(OP_SHOW_CONTROL);
                } else {
                    sendCommand(OP_HIDE_CONTROL);
                }
            }
        });
    }

    @Override
    public void android_app_Activity__onResume__(ILTweaks.MethodParam param) {
        afterOnClass(getActivityString(), param, () -> {
            Logger.v("In MX Player.");
            mInMXPLayer = true;
            sendCommand(OP_SHOW_ALL);
            if (mPlaying) {
                sendCommand(OP_RESUME);
            }
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        beforeOnClass(getActivityString(), param, () -> {
            Logger.v("Not in MX Player.");
            mInMXPLayer = false;
            sendCommand(OP_HIDE_ALL);
            if (mPlaying) {
                sendCommand(OP_PAUSE);
            }
        });
    }

    private String getActivityString() {
        return PackageNames.MX_PLAYER_PRO.equals(getPackageName()) ? ACTIVITY_SCREEN_PRO : ACTIVITY_SCREEN_FREE;
    }

    private void sendCommand(int op) {
        sendCommand(op, new ContentValues());
    }

    private void sendCommand(int op, ContentValues values) {
        LTHelper.currentApplication().getContentResolver()
                .update(Uri.parse("content://li.lingfeng.mxdanmaku.MainController/" + op), values, null, null);
    }
}
