package li.lingfeng.ltsystem.tweaks.system;

import android.net.IpPrefix;
import android.net.LinkProperties;
import android.net.RouteInfo;
import android.net.VpnService;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.ReflectUtils;

public class ShadowsocksLinuxKernelRoute {

    @MethodsLoad(packages = PackageNames.SHADOWSOCKS, prefs = R.string.key_shadowsocks_linux_kernel_route)
    public static class Shadowsocks extends TweakBase {
        @Override
        public void java_lang_ProcessBuilder__start__(ILTweaks.MethodParam param) {
            param.before(() -> {
                ProcessBuilder processBuilder = (ProcessBuilder) param.thisObject;
                List<String> command = processBuilder.command();
                if (command.get(0).endsWith("libss-local.so")) {
                    for (int i = command.size() - 2; i > 0; --i) {
                        if (command.get(i).equals("--acl")) {
                            command.remove(i);
                            command.remove(i);
                            Logger.d("Remove ss-local acl for linux kernel route, " + StringUtils.join(command, ' '));
                            break;
                        }
                    }
                }
            });
        }

        @Override
        public void android_net_VpnService$Builder__establish__(ILTweaks.MethodParam param) {
            param.before(() -> {
                Logger.i("Shadowsocks establish.");
                VpnService.Builder builder = (VpnService.Builder) param.thisObject;
                List<RouteInfo> routes = (List<RouteInfo>) ReflectUtils.getObjectField(builder, "mRoutes");
                routes.clear();
                builder.addRoute("8.8.8.8", 32);
            });
        }
    }

    @MethodsLoad(packages = PackageNames.ANDROID, prefs = R.string.key_shadowsocks_linux_kernel_route)
    public static class Android extends TweakBase {
        // ip route show table tun0
        @Override
        public void com_android_server_ConnectivityService__updateRoutes__LinkProperties_LinkProperties_int(ILTweaks.MethodParam param) {
            param.before(() -> {
                LinkProperties newLp = (LinkProperties) param.args[0];
                LinkProperties oldLp = (LinkProperties) param.args[1];
                if (oldLp == null && newLp.getInterfaceName().equals("tun0")) {
                    newLp = (LinkProperties) ReflectUtils.newInstance(LinkProperties.class, newLp);
                    List<RouteInfo> routes = (List<RouteInfo>) ReflectUtils.getObjectField(newLp, "mRoutes");
                    String[] ip_list = ContextUtils.getLStringArray(R.array.shadowsocks_route_ip_list);
                    Logger.d("shadowsocks_route_ip_list " + ip_list.length);
                    for (String ip : ip_list) {
                        if (!ip.contains("/")) {
                            ip = ip + "/32";
                        }
                        IpPrefix ipPrefix = (IpPrefix) ReflectUtils.newInstance(IpPrefix.class, ip);
                        RouteInfo routeInfo = (RouteInfo) ReflectUtils.newInstance(RouteInfo.class,
                                new Object[] { ipPrefix, null, "tun0" }, new Class[] { IpPrefix.class, InetAddress.class, String.class});
                        routes.add(routeInfo);
                    }
                    param.setArg(0, newLp);
                }
            });
        }
    }
}
