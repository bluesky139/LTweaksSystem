package li.lingfeng.ltsystem.tweaks.system;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.InetAddress;
import java.util.List;
import java.util.regex.Pattern;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.SHADOWSOCKS, prefs = R.string.key_shadowsocks_ddns_update)
public class ShadowsocksDdnsUpdate extends TweakBase {

    private static final String INET_CONDITION_ACTION = "android.net.conn.INET_CONDITION_ACTION";
    private File mConfigFile;
    private String mDomain;
    private String mIP;
    private Process mProcess;
    private BroadcastReceiver mReceiver;
    private Handler mHandler;
    private ConnectivityManager mConnectivityManager;

    @Override
    public void java_lang_ProcessBuilder__start__(ILTweaks.MethodParam param) {
        param.after(() -> {
            ProcessBuilder processBuilder = (ProcessBuilder) param.thisObject;
            List<String> command = processBuilder.command();
            if (command.get(0).endsWith("libss-local.so")) {
                for (int i = 0; i < command.size() - 1; ++i) {
                    if (command.get(i).equals("-c")) {
                        String path = command.get(i + 1);
                        mConfigFile = new File(path);
                        JSONObject jConfig = JSON.parseObject(FileUtils.readFileToString(mConfigFile));
                        mIP = jConfig.getString("server");
                        Logger.i("ss-local IP " + mIP);
                        break;
                    }
                }
                mProcess = (Process) param.getResult();
                Logger.d("mProcess " + mProcess);
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void android_app_Service__startForeground__int_Notification(ILTweaks.MethodParam param) {
        param.after(() -> {
            if (mReceiver == null) {
                Object data = ReflectUtils.callMethod(param.thisObject, "getData");
                Object profile = ReflectUtils.callMethod(data, "getProfile");
                mDomain = (String) ReflectUtils.callMethod(profile, "getHost");
                if (Pattern.matches("^[\\d\\.]+$", mDomain)) {
                    Logger.i("ss-local not domain " + mDomain);
                    return;
                }
                Logger.i("ss-local domain " + mDomain);

                mHandler = new Handler();
                mConnectivityManager = (ConnectivityManager) LTHelper.currentApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
                mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(INET_CONDITION_ACTION)) {
                            Logger.d("onReceive  " + INET_CONDITION_ACTION);
                            pendingValidation();
                        } else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                            NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                            Logger.d("onReceive  " + ConnectivityManager.CONNECTIVITY_ACTION + ", " + networkInfo);
                            if (networkInfo.isConnected()) {
                                pendingValidation();
                            } else {
                                cancelValidation();
                            }
                        }
                    }
                };
                IntentFilter filter = new IntentFilter();
                filter.addAction(INET_CONDITION_ACTION);
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                filter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY + 1);
                LTHelper.currentApplication().registerReceiver(mReceiver, filter);
            }
        });
    }

    private void pendingValidation() {
        pendingValidation(0);
    }

    private void pendingValidation(int retry) {
        if (mDomain == null) {
            return;
        }
        Logger.d("Pending validation, retry " + retry);
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new Validate(retry), 10000 * (retry * 3 + 1));
    }

    private void cancelValidation() {
        Logger.d("Cancel validation.");
        mHandler.removeCallbacksAndMessages(null);
    }

    private class Validate implements Runnable {

        private int mRetry;

        public Validate(int retry) {
            mRetry = retry;
        }

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
            Network network = mConnectivityManager.getActiveNetwork();
            if (networkInfo != null && network != null && networkInfo.isConnected()) {
                NetworkCapabilities networkCapabilities = mConnectivityManager.getNetworkCapabilities(network);
                if (networkCapabilities != null) {
                    if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                        new UpdateTask().execute(mRetry);
                    } else {
                        Logger.d("Network is validated.");
                    }
                }
            }
        }
    };

    private class UpdateTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected Integer doInBackground(Integer[] retry) {
            try {
                Logger.v("Update DDNS.");
                InetAddress address = InetAddress.getByName(mDomain);
                String ip = address.getHostAddress();
                Logger.d("ip " + ip);
                if (!ip.equals(mIP)) {
                    Logger.i("New ip " + ip + " from " + mDomain);
                    JSONObject jConfig = JSON.parseObject(FileUtils.readFileToString(mConfigFile));
                    jConfig.put("server", ip);
                    FileUtils.writeStringToFile(mConfigFile, jConfig.toString());
                    mProcess.destroyForcibly();
                    return 0;
                }
            } catch (Throwable e) {
                Logger.w("Failed to update DDNS, " + e);
            }
            return retry[0] + 1;
        }

        @Override
        protected void onPostExecute(Integer retry) {
            if (retry > 0 && retry < 5) {
                pendingValidation(retry);
            } else {
                Toast.makeText(LTHelper.currentApplication(), "Shadowsocks DDNS updated.", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void android_app_Service__stopForeground__int(ILTweaks.MethodParam param) {
        param.before(() -> {
            mConfigFile = null;
            mDomain = null;
            mIP = null;
            mProcess = null;
            if (mReceiver != null) {
                LTHelper.currentApplication().unregisterReceiver(mReceiver);
                mReceiver = null;
            }
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
            mConnectivityManager = null;
        });
    }
}
