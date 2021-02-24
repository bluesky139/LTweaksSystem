package li.lingfeng.ltsystem.tweaks.communication;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.provider.Settings;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.AESUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = PackageNames.WE_CHAT, prefs = R.string.key_wechat_fingerprint)
public class WeChatFingerprint extends TweakBase {

    private static final String WALLLET_PAY_UI = ".WalletPayUI";
    private static final String UI_PAGE_FRAGMENT_ACTIVITY = ".UIPageFragmentActivity";
    private static final String EDIT_PASSWORD_VIEW = ".EditHintPasswdView";
    private static final String TENPAY_SECURE_EDITTEXT = ".TenpaySecureEditText";
    private static final String MY_KEYBOARD_WINDOW = ".MyKeyboardWindow";
    private boolean mAtPayment = false;
    private CancellationSignal mCancellationSignal;

    // https://github.com/eritpchy/Xposed-Fingerprint-pay/blob/master/app/src/main/java/com/yyxx/wechatfp/xposed/plugin/XposedWeChatPlugin.java
    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        Logger.d("Activity__performCreate " + param.thisObject);
        afterOnClassEnd(WALLLET_PAY_UI, param, () -> {
            handleActivity(param);
        });
        afterOnClassEnd(UI_PAGE_FRAGMENT_ACTIVITY, param, () -> {
            handleActivity(param);
        });
    }

    @Override
    public void android_app_Dialog__show__(ILTweaks.MethodParam param) {
        Logger.d("Dialog__show " + param.thisObject);
        param.after(() -> {
            if (mAtPayment) {
                handleDialog(param);
            }
        });
    }

    @Override
    public void android_app_Activity__onPause__(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mCancellationSignal != null && !mCancellationSignal.isCanceled()) {
                Logger.i("Cancel fingerprint scan.");
                mCancellationSignal.cancel();
            }
            mCancellationSignal = null;
        });
    }

    @Override
    public void android_app_Activity__onDestroy__(ILTweaks.MethodParam param) {
        Logger.d("Activity__onDestroy " + param.thisObject);
        beforeOnClassEnd(WALLLET_PAY_UI, param, () -> {
            mAtPayment = false;
        });
        beforeOnClassEnd(UI_PAGE_FRAGMENT_ACTIVITY, param, () -> {
            mAtPayment = false;
        });
    }

    private void handleActivity(ILTweaks.MethodParam param) {
        Activity activity = (Activity) param.thisObject;
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        handleRootView(rootView);
        mAtPayment = true;
    }

    private void handleDialog(ILTweaks.MethodParam param) {
        Dialog dialog = (Dialog) param.thisObject;
        ViewGroup rootView = (ViewGroup) dialog.getWindow().getDecorView();
        handleRootView(rootView);
    }

    private void handleRootView(ViewGroup rootView) {
        Logger.d("handleRootView");
        ViewUtils.printChilds(rootView);
        if (tryOnce(rootView)) {
            return;
        }
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (tryOnce(rootView)) {
                    rootView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });
    }

    private boolean tryOnce(ViewGroup rootView) {
        boolean end = false;
        try {
            if (ViewUtils.findViewByTypeEnd(rootView, EDIT_PASSWORD_VIEW) != null) {
                onPayDialogShown(rootView);
                end = true;
            }
        } catch (Throwable e) {
            Logger.e("onGlobalLayout exception.", e);
            end = true;
            ViewUtils.printChilds(rootView);
        }
        if (!end) {
            Logger.d("onGlobalLayout");
            ViewUtils.printChilds(rootView);
        }
        return end;
    }

    private void onPayDialogShown(ViewGroup rootView) throws Throwable {
        Logger.d("onPayDialogShown");
        View passwordLayout = ViewUtils.findViewByTypeEnd(rootView, EDIT_PASSWORD_VIEW);
        EditText inputEditText = ViewUtils.findViewByTypeEnd(rootView, TENPAY_SECURE_EDITTEXT);
        ViewGroup keyboardView = ViewUtils.findViewByTypeEnd(rootView, MY_KEYBOARD_WINDOW);
        Validate.notNull(passwordLayout, "passwordLayout null");
        Validate.notNull(inputEditText, "inputEditText null");
        Validate.notNull(keyboardView, "keyboardView null");

        String password = getPassword(rootView.getContext());
        if (password == null) { // Save password at first time.
            savePassword(keyboardView);
        } else {
            authWithFingerprint(inputEditText, password);
        }
    }

    private void savePassword(ViewGroup keyboardView) throws Throwable {
        Logger.d("keyboardView " + keyboardView);
        List<Integer> password = new ArrayList<>(6);
        for (int i = 0; i <= 9; ++i) {
            View button = ViewUtils.findViewByName(keyboardView, "tenpay_keyboard_" + i);
            View.OnClickListener originalListener = ViewUtils.getViewClickListener(button);
            final int code = i;
            button.setOnClickListener((v) -> {
                try {
                    password.add(code);
                    Logger.d("Got one code, size " + password.size());
                    if (password.size() == 6) {
                        Logger.i("Save password.");
                        putPassword(keyboardView.getContext(), StringUtils.join(password, ""));
                        Toast.makeText(keyboardView.getContext(), "Scan fingerprint at next time.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Throwable e) {
                    Logger.e("Save password exception.", e);
                }
                originalListener.onClick(v);
            });
        }
        Toast.makeText(keyboardView.getContext(), "Preparing fingerprint scan for next time.", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("MissingPermission")
    private void authWithFingerprint(EditText inputEditText, String password) {
        Context context = inputEditText.getContext();
        String msg = "Fingerprint scan ready.";
        Logger.i(msg);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

        if (mCancellationSignal != null) {
            Logger.e("Last mCancellationSignal exist.");
        }
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(null, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                String msg = "Fingerprint error " + errorCode + ", " + errString;
                Logger.e(msg);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                String msg = "Fingerprint help " + helpCode + ", " + helpString;
                Logger.w(msg);
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                String msg = "Fingerprint ok.";
                Logger.i(msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                try {
                    inputDigitalPassword(inputEditText, password);
                } catch (Throwable e) {
                    Logger.e("inputDigitalPassword error.", e);
                    Toast.makeText(context, "Input digital password error.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                String msg = "Fingerprint failed.";
                Logger.e(msg);
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void inputDigitalPassword(EditText inputEditText, String password) throws Throwable {
        Logger.d("inputEditText " + inputEditText);
        Logger.i("Input digital password, length " + password.length());
        inputEditText.setText(password);
    }

    private String getPassword(Context context) throws Throwable {
        String str = context.getSharedPreferences("ltweaks_wechat", 0).getString("payment_input", null);
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        String androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return AESUtils.decrypt(str, androidId);
    }

    private void putPassword(Context context, String password) throws Throwable {
        String androidId = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        String str = AESUtils.encrypt(password, androidId);
        context.getSharedPreferences("ltweaks_wechat", 0).edit().putString("payment_input", str).commit();
    }
}
