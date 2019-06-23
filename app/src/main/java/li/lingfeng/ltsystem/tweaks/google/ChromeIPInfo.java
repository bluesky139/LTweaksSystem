package li.lingfeng.ltsystem.tweaks.google;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Pair;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ViewUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@MethodsLoad(packages = {
        PackageNames.CHROME,
        PackageNames.CHROME_BETA,
        PackageNames.CHROME_DEV,
        PackageNames.CHROME_CANARY,
}, prefs = R.string.key_chrome_ip_info)
public class ChromeIPInfo extends ChromeBase {

    @Override
    protected Map<String, MenuInfo> newMenus() {
        Map<String, MenuInfo> infos = new HashMap<>(1);
        String title = ContextUtils.getLString(R.string.chrome_ip_info);
        infos.put(title, new MenuInfo(title, 1007, (activity, url, isCustomTab) -> {
            new GetIpInfoTask(activity).execute(url);
        }));
        return infos;
    }

    static class GetIpInfoTask extends AsyncTask<String, Void, Pair<Boolean, String>> {

        Activity mActivity;
        AlertDialog mProgressingDialog;
        OkHttpClient mHttpClient;

        public GetIpInfoTask(Activity activity) {
            super();
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            mHttpClient = new OkHttpClient();
            mProgressingDialog = ViewUtils.showProgressingDialog(mActivity, true, () -> {
                Logger.i("Cancelled by user.");
                abort();
            });
        }

        @Override
        protected Pair<Boolean, String> doInBackground(String... params) {
            try {
                String url = params[0];
                Logger.i("Try to get ip info " + url);
                final String host = new URL(url).getHost();
                InetAddress address = InetAddress.getByName(host);
                final String ip = address.getHostAddress();
                String lang = Locale.getDefault().getLanguage().equals(new Locale("zh")) ? "zh-CN" : "en";

                Request request = new Request.Builder()
                        .url("http://ip-api.com/json/" + ip + "?&lang=" + lang)
                        .build();

                if (mHttpClient == null) {
                    return Pair.create(false, "Abort.");
                }

                Response response = mHttpClient.newCall(request).execute();
                if (response.code() == 200) {
                    final String str = response.body().string();
                    Logger.i("Got ip info, " + str);
                    String html = parseIpInfo(host, ip, str);
                    return Pair.create(true, html);
                } else {
                    return Pair.create(false, "Response code " + response.code());
                }
            } catch (Throwable e) {
                return Pair.create(false, "GetIpInfoTask error, " + e);
            }
        }

        private String parseIpInfo(String host, String ip, String str) throws Throwable {
            JSONObject jData = (JSONObject) JSON.parse(str);
            String ret = jData.getString("status");
            if (!"success".equals(ret)) {
                throw new RuntimeException("parseIpInfo error, ret is not success.");
            }

            LinkedHashMap<Integer, String> data = new LinkedHashMap<>(5);
            data.put(R.string.chrome_ip_domain, host);
            data.put(R.string.chrome_ip_ip, ip);

            data.put(R.string.chrome_ip_geo, jData.getString("country") + " " + jData.getString("regionName") + " " + jData.getString("city"));
            data.put(R.string.chrome_ip_isp, jData.getString("isp"));
            data.put(R.string.chrome_ip_asn, jData.getString("as"));

            final StringBuilder stringBuilder = new StringBuilder();
            for (LinkedHashMap.Entry<Integer, String> kv : data.entrySet()) {
                stringBuilder.append("<b>");
                stringBuilder.append(ContextUtils.getLString(kv.getKey()));
                stringBuilder.append(":</b><br>&nbsp;&nbsp;");
                stringBuilder.append(kv.getValue());
                stringBuilder.append("<br><br>");
            }
            return stringBuilder.toString();
        }

        @Override
        protected void onPostExecute(Pair<Boolean, String> pair) {
            if (mActivity == null) {
                return;
            }
            if (pair.first) {
                ViewUtils.showDialog(mActivity, Html.fromHtml(pair.second));
            } else {
                Logger.e("Get IP info error, " + pair.second);
                Toast.makeText(mActivity, R.string.error, Toast.LENGTH_SHORT).show();
            }
            cleanup();
        }

        private void abort() {
            cleanup();
        }

        private void cleanup() {
            mActivity = null;
            if (mProgressingDialog != null) {
                mProgressingDialog.dismiss();
                mProgressingDialog = null;
            }
            if (mHttpClient != null) {
                mHttpClient.dispatcher().cancelAll();
                mHttpClient = null;
            }
        }
    }
}
