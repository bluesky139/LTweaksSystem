package li.lingfeng.ltsystem.tweaks.system;

import android.content.Context;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.ANDROID_SYSTEM_UI, prefs = R.string.key_quick_settings_brightness_by_volume_buttons)
public class VolumeButtonOnBrightness extends TweakBase {

    private static final float BRIGHTNESS_ADJ_STEPS = 1f / 128f;
    private Object mBrightnessMirrorController;
    private Handler mHandler;
    private int mMinBrightness;
    private int mMaxBrightness;
    private boolean mQsFullyExpanded = false;

    @Override
    public void com_android_systemui_statusbar_phone_StatusBar__makeStatusBarView__(ILTweaks.MethodParam param) {
        param.after(() -> {
            try {
                mBrightnessMirrorController = ReflectUtils.getObjectField(param.thisObject, "mBrightnessMirrorController");
            } catch (Throwable _) {}
            mHandler = (Handler) ReflectUtils.getObjectField(param.thisObject, "mHandler");

            PowerManager powerManager = (PowerManager) LTHelper.currentApplication().getSystemService(Context.POWER_SERVICE);
            mMinBrightness = (int) ReflectUtils.callMethod(powerManager, "getMinimumScreenBrightnessSetting");
            mMaxBrightness = (int) ReflectUtils.callMethod(powerManager, "getMaximumScreenBrightnessSetting");
        });
    }

    @Override
    public void com_android_systemui_statusbar_phone_StatusBarWindowView__dispatchKeyEvent__KeyEvent(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (!mQsFullyExpanded) {
                return;
            }
            KeyEvent keyEvent = (KeyEvent) param.args[0];
            int keyCode = keyEvent.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    Context context = LTHelper.currentApplication();
                    boolean automatic = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                            Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) != Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;

                    if (automatic) {
                        float adj = Settings.System.getFloat(context.getContentResolver(), "screen_auto_brightness_adj", 0f);
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            adj -= BRIGHTNESS_ADJ_STEPS;
                            if (adj < -1f) {
                                adj = -1f;
                            }
                        } else {
                            adj += BRIGHTNESS_ADJ_STEPS;
                            if (adj > 1f) {
                                adj = 1f;
                            }
                        }
                        Logger.v("Set brightness adj " + adj + " by volume button.");
                        Settings.System.putFloat(context.getContentResolver(), "screen_auto_brightness_adj", adj);

                    } else {
                        int brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
                        if (brightness > 0) {
                            int oldBrightness = brightness;
                            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                                if (brightness - 1 >= mMinBrightness) {
                                    --brightness;
                                }
                            } else {
                                if (brightness + 1 <= mMaxBrightness) {
                                    ++brightness;
                                }
                            }
                            if (brightness != oldBrightness) {
                                Logger.v("Set brightness " + brightness + " by volume button.");
                                Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
                            }

                            if (mBrightnessMirrorController != null) {
                                ReflectUtils.callMethod(mBrightnessMirrorController, "showMirror");
                                ViewGroup statusBarWindow = (ViewGroup) param.thisObject;
                                ViewGroup qsPanel = (ViewGroup) ViewUtils.findViewByName(statusBarWindow, "quick_settings_panel");
                                View toggleSlider = ViewUtils.findViewByName(qsPanel, "brightness_slider");
                                ReflectUtils.callMethod(mBrightnessMirrorController, "setLocation",
                                        new Object[] { toggleSlider.getParent() },
                                        new Class[] { View.class });
                                mHandler.removeCallbacks(mHideMirrorRunnable);
                                mHandler.postDelayed(mHideMirrorRunnable, 1000);
                            }
                        }
                    }
                }
                param.setResult(true);
            }
        });
    }

    @Override
    public void com_android_systemui_statusbar_phone_NotificationPanelView__setQsExpansion__float(ILTweaks.MethodParam param) {
        param.after(() -> {
            mQsFullyExpanded = ReflectUtils.getBooleanField(param.thisObject, "mQsFullyExpanded");
        });
    }

    private Runnable mHideMirrorRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                ReflectUtils.callMethod(mBrightnessMirrorController, "hideMirror");
            } catch (Throwable e) {
                Logger.e("Call hideMirror() exception.", e);
            }
        }
    };
}
