package li.lingfeng.ltsystem.tweaks.system;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = {}, prefs = R.string.key_text_long_press_to_copy, excludedPackages = {
        PackageNames.ANDROID, PackageNames.ANDROID_SYSTEM_UI, PackageNames.NOVA_LAUNCHER, PackageNames.ELE
})
public class TextLongPressToCopy extends TweakBase {

    private static final int MAX_DOWN_OFFSET = 15;
    private LongPressRunnable mLongPressRunnable;
    private float mDownX;
    private float mDownY;

    @Override
    public void android_widget_TextView__onTouchEvent__MotionEvent(ILTweaks.MethodParam param) {
        param.before(() -> {
            final TextView textView = (TextView) param.thisObject;
            if (textView instanceof EditText || textView instanceof Button || textView.isTextSelectable()) {
                return;
            }
            MotionEvent event = (MotionEvent) param.args[0];
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDownX = event.getRawX();
                    mDownY = event.getRawY();
                    pendingLongPress(textView);
                    break;
            }
        });
    }

    @Override
    public void android_view_View__dispatchPointerEvent__MotionEvent(ILTweaks.MethodParam param) {
        param.before(() -> {
            MotionEvent event = (MotionEvent) param.args[0];
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    cancelLongPress();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (Math.abs(mDownX - event.getRawX()) > MAX_DOWN_OFFSET || Math.abs(mDownY - event.getRawY()) > MAX_DOWN_OFFSET) {
                        cancelLongPress();
                    }
                    break;
            }
        });
    }

    private void pendingLongPress(TextView textView) {
        cancelLongPress();
        mLongPressRunnable = new LongPressRunnable(textView);
        textView.postDelayed(mLongPressRunnable, 1000);
    }

    private void cancelLongPress() {
        if (mLongPressRunnable != null) {
            mLongPressRunnable.selfRemove();
            mLongPressRunnable = null;
        }
    }

    class LongPressRunnable implements Runnable {

        private TextView textView;

        public LongPressRunnable(TextView textView) {
            this.textView = textView;
        }

        @Override
        public void run() {
            Logger.v("Long press to copy text.");
            ClipboardManager clipboardManager = (ClipboardManager) textView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, textView.getText()));
            mLongPressRunnable = null;
        }

        public void selfRemove() {
            textView.removeCallbacks(this);
        }
    }
}
