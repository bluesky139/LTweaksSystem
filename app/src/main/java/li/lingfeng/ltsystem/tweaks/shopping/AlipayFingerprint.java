package li.lingfeng.ltsystem.tweaks.shopping;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import java.util.List;
import java.util.Optional;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.AESUtils;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;

@MethodsLoad(packages = { PackageNames.ALIPAY, PackageNames.TAOBAO }, prefs = R.string.key_alipay_fingerprint)
public class AlipayFingerprint extends TweakBase {

    private static final String MSP_CONTAINER_ACTIVITY = "com.alipay.android.msp.ui.views.MspContainerActivity";
    private static final String FLY_BIRD_WINDOW_ACTIVITY = "com.alipay.android.app.flybird.ui.window.FlyBirdWindowActivity";
    private static final String PAY_PWD_HALF_ACTIVITY = "com.alipay.mobile.verifyidentity.module.password.pay.ui.PayPwdHalfActivity";
    private CancellationSignal mCancellationSignal;

    // https://github.com/eritpchy/Xposed-Fingerprint-pay/blob/master/app/src/main/java/com/yyxx/wechatfp/xposed/plugin/XposedAlipayPlugin.java
    @Override
    public void android_app_Activity__performCreate__Bundle_PersistableBundle(ILTweaks.MethodParam param) {
        afterOnClass(MSP_CONTAINER_ACTIVITY, param, () -> {
            handleMspContainerActivity(param);
        });

        afterOnClass(FLY_BIRD_WINDOW_ACTIVITY, param, () -> {
            handleMspContainerActivity(param);
        });

        afterOnClass(PAY_PWD_HALF_ACTIVITY, param, () -> {
            Activity activity = (Activity) param.thisObject;
            activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    try {
                        Logger.d("In PAY_PWD_HALF_ACTIVITY");
                        if (activity.isFinishing() || activity.isDestroyed()) {
                            return;
                        }
                        int id1 = ContextUtils.getIdId("key_num_1",
                                isAlipay() ? "com.alipay.android.phone.safepaybase" : "com.taobao.taobao");
                        if (id1 == 0) {
                            return;
                        }
                        View view1 = activity.findViewById(id1);
                        if (view1 == null) {
                            return;
                        }

                        String password = getPassword(activity);
                        if (password == null) { // Save password at first time.
                            savePassword(activity);
                        } else {
                            authWithFingerprint(activity, password);
                        }
                        activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } catch (Throwable e) {
                        Logger.e("onGlobalLayout exception.", e);
                    }
                }
            });
        });
    }

    private void handleMspContainerActivity(ILTweaks.MethodParam param) {
        Activity activity = (Activity) param.thisObject;
        activity.getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                try {
                    Logger.d("In MSP_CONTAINER_ACTIVITY");
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        return;
                    }
                    int id1 = isAlipay() ?
                            ContextUtils.getIdId("simplePwdLayout", "com.alipay.android.app") :
                            ContextUtils.getIdId("simplePwdLayout", "com.taobao.taobao");
                    int id2 = isAlipay() ?
                            ContextUtils.getIdId("mini_linSimplePwdComponent", "com.alipay.android.phone.safepaybase") :
                            ContextUtils.getIdId("mini_spwd_input", "com.taobao.taobao");
                    if (id1 == 0 && id2 == 0) {
                        return;
                    }
                    View view1 = activity.findViewById(id1);
                    View view2 = activity.findViewById(id2);
                    if (view1 == null && view2 == null) {
                        return;
                    }

                    String password = getPassword(activity);
                    if (password == null) { // Save password at first time.
                        savePassword(activity);
                    } else {
                        authWithFingerprint(activity, password);
                    }
                    activity.getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } catch (Throwable e) {
                    Logger.e("onGlobalLayout exception.", e);
                }
            }
        });
    }

    private boolean isAlipay() {
        return getPackageName().equals(PackageNames.ALIPAY);
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

    private void savePassword(Activity activity) throws Throwable {
        EditText passwordEditText = getPasswordView(activity);
        if (passwordEditText == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null password edit text.");
        }
        Logger.d("passwordEditText " + passwordEditText);

        View payButton = getPayButton(activity);
        if (payButton == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null pay button.");
        }
        Logger.d("payButton " + payButton);

        View.OnClickListener originalListener = ViewUtils.getViewClickListener(payButton);
        payButton.setOnClickListener((v) -> {
            Logger.i("Save password.");
            try {
                putPassword(activity, passwordEditText.getText().toString());
            } catch (Throwable e) {
                Logger.e("Put password exception.", e);
            }
            originalListener.onClick(v);
        });
    }

    @SuppressLint("MissingPermission")
    private void authWithFingerprint(Activity activity, String password) {
        String msg = "Fingerprint scan ready.";
        Logger.i(msg);
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();

        FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(null, mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                String msg = "Fingerprint error " + errorCode + ", " + errString;
                Logger.e(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                String msg = "Fingerprint help " + helpCode + ", " + helpString;
                Logger.w(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                String msg = "Fingerprint ok.";
                Logger.i(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                try {
                    inputGenericPassword(activity, password);
                } catch (Throwable e) {
                    Logger.e("inputGenericPassword error.", e);
                    Toast.makeText(activity, "Input generic password error.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationFailed() {
                String msg = "Fingerprint failed.";
                Logger.e(msg);
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    private void inputGenericPassword(Activity activity, String password) throws Throwable {
        EditText passwordEditText = getPasswordView(activity);
        if (passwordEditText == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null password edit text.");
        }
        Logger.d("passwordEditText " + passwordEditText);

        View payButton = getPayButton(activity);
        if (payButton == null) {
            ViewUtils.printChilds(activity);
            throw new RuntimeException("Null pay button.");
        }
        Logger.d("payButton " + payButton);

        Logger.i("Input generic password and pay.");
        passwordEditText.setText(password);
        payButton.performClick();
    }

    private EditText getPasswordView(Activity activity) {
        int id = ContextUtils.getIdId("input_et_password",
                isAlipay() ? "com.alipay.android.phone.safepaybase" : "com.taobao.taobao");
        EditText passwordEditText = null;
        if (id > 0) {
            View view = activity.findViewById(id);
            if (view instanceof EditText && view.isShown()) {
                passwordEditText = (EditText) view;
            }
        }
        if (passwordEditText == null) {
            List<EditText> editTexts = ViewUtils.findAllViewByType((ViewGroup) activity.getWindow().getDecorView(), EditText.class);
            for (EditText editText : editTexts) {
                if (editText.getId() == -1 && editText.isShown()) {
                    passwordEditText = editText;
                    break;
                }
            }
        }
        return passwordEditText;
    }

    private View getPayButton(Activity activity) {
        int id = ContextUtils.getIdId("button_ok",
                isAlipay() ? "com.alipay.android.phone.safepaybase" : "com.taobao.taobao");
        View payButton = null;
        if (id > 0) {
            View view = activity.findViewById(id);
            if (view != null && view.isShown()) {
                payButton = view;
            }
        }
        if (payButton == null) {
            Optional<TextView> textView = ViewUtils.findAllViewByType((ViewGroup) activity.getWindow().getDecorView(), TextView.class)
                    .stream()
                    .filter(v -> StringUtils.equalsAny(v.getText().toString(), "付款", "Pay", "确定", "确认"))
                    .findFirst();
            if (textView.isPresent()) {
                payButton = textView.get();
            }
        }
        return payButton;
    }

    private String getPassword(Activity activity) throws Throwable {
        String str = activity.getSharedPreferences("ltweaks_alipay", 0).getString("payment_input", null);
        if (StringUtils.isEmpty(str)) {
            return null;
        }
        String androidId = Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
        return AESUtils.decrypt(str, androidId);
    }

    private void putPassword(Activity activity, String password) throws Throwable {
        String androidId = Settings.System.getString(activity.getContentResolver(), Settings.System.ANDROID_ID);
        String str = AESUtils.encrypt(password, androidId);
        activity.getSharedPreferences("ltweaks_alipay", 0).edit().putString("payment_input", str).commit();
    }
}
