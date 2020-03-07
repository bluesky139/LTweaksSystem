package li.lingfeng.ltsystem.tweaks.system;

import android.net.Network;

import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

@MethodsLoad(packages = PackageNames.ANDROID_NETWORK_STACK, prefs = R.string.key_shadowsocks_network_validate)
public class ShadowsocksNetworkValidate extends TweakBase {

    private static final String CAPTIVE_PORTAL_HTTP_URL = "http://www.google.com/generate_204";
    private Network mNetwork;

    @Override
    public void com_android_server_connectivity_NetworkMonitor__getUseHttpsValidation__(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(false);
        });
    }

    @Override
    public void com_android_server_connectivity_NetworkMonitor__getCaptivePortalServerHttpUrl__(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(CAPTIVE_PORTAL_HTTP_URL);
        });
    }

    @Override
    public void com_android_server_connectivity_NetworkMonitor__sendDnsProbe__String(ILTweaks.MethodParam param) {
        param.before(() -> {
            param.setResult(null);
        });
    }

    @Override
    public void com_android_server_connectivity_NetworkMonitor__sendHttpProbe__URL_int_CaptivePortalProbeSpec(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (NetworkInterface.getByName("tun0") != null) {
                mNetwork = (Network) ReflectUtils.getObjectField(param.thisObject, "mCleartextDnsNetwork");
            }
        });
        param.after(() -> {
            mNetwork = null;
        });
    }

    @Override
    public void android_net_Network__openConnection__URL(ILTweaks.MethodParam param) {
        param.before(() -> {
            if (mNetwork == param.thisObject) {
                Network network = (Network) param.thisObject;
                URL url = (URL) param.args[0];
                Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
                Logger.i("Network validate by " + url + " through " + proxy);
                URLConnection connection = network.openConnection(url, proxy);
                param.setResult(connection);
            }
        });
    }
}
