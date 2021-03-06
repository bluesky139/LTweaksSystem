package li.lingfeng.ltsystem.tweaks.system;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.LTHelper;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.prefs.Prefs;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.ContextUtils;
import li.lingfeng.ltsystem.utils.Logger;
import li.lingfeng.ltsystem.utils.PackageUtils;
import li.lingfeng.ltsystem.utils.ReflectUtils;
import li.lingfeng.ltsystem.utils.Shell;

@MethodsLoad(packages = PackageNames.SHADOWSOCKS, prefs = R.string.key_shadowsocks_transproxy)
public class ShadowsocksTransproxy extends TweakBase {

    private static final String TRANSPROXY_SERVICE = "com.github.shadowsocks.bg.TransproxyService";
    private static final String CORE = "com.github.shadowsocks.Core";
    private Shell mShellAdding;
    private boolean mStarted = false;

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
                        Logger.d("Remove ss-local acl, " + StringUtils.join(command, ' '));
                        break;
                    }
                }
            }
        });
    }

    // TODO: Create tmp chain while adding, use "iptables -w -t nat -L Shadowsocks -n" to check chain exist.
    @Override
    public void android_app_Service__startForeground__int_Notification(ILTweaks.MethodParam param) {
        if (mStarted) {
            return;
        }
        afterOnClass(TRANSPROXY_SERVICE, param, () -> {
            mStarted = true;
            final long startTime = System.currentTimeMillis();
            String[] cmds;
            if (Prefs.instance().getBoolean(R.string.key_shadowsocks_transproxy_with_ipset, false)) {
                cmds = new String[] {
                        "iptables -w -t nat -D OUTPUT -j Shadowsocks",
                        "iptables -w -t nat -F Shadowsocks",
                        "iptables -w -t nat -N Shadowsocks",
                        "iptables -w -t nat -A Shadowsocks -o lo -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -d 127.0.0.1 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -m owner --uid-owner " + PackageUtils.getUid() + " -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p udp --dport 53 -j REDIRECT --to-port 5450",
                        "iptables -w -t nat -A Shadowsocks -p tcp --dport 53 -j REDIRECT --to-port 5450",
                        "iptables -w -t nat -A Shadowsocks -p all -d 0.0.0.0/8 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 10.0.0.0/8 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 100.64.0.0/10 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 127.0.0.0/8 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 169.254.0.0/16 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 172.16.0.0/12 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 192.0.0.0/29 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 192.0.2.0/24 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 192.88.99.0/24 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 192.168.0.0/16 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 198.18.0.0/15 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 198.51.100.0/24 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 203.0.113.0/24 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -d 224.0.0.0/3 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p all -m set --match-set china_ip_list dst -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p tcp -j REDIRECT --to-port 8200",
                        "iptables -w -t nat -A OUTPUT -j Shadowsocks"
                };
            } else {
                String[] ipList = ContextUtils.getLStringArray(R.array.shadowsocks_bypass_ip_list);
                String[] preCmds = new String[] {
                        "iptables -w -t nat -D OUTPUT -j Shadowsocks",
                        "iptables -w -t nat -F Shadowsocks",
                        "iptables -w -t nat -N Shadowsocks",
                        "iptables -w -t nat -A Shadowsocks -o lo -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -d 127.0.0.1 -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -m owner --uid-owner " + PackageUtils.getUid() + " -j RETURN",
                        "iptables -w -t nat -A Shadowsocks -p udp --dport 53 -j DNAT --to-destination 127.0.0.1:5450",
                        "iptables -w -t nat -A Shadowsocks -p tcp --dport 53 -j DNAT --to-destination 127.0.0.1:5450",
                };
                cmds = new String[preCmds.length + ipList.length + 2];
                System.arraycopy(preCmds, 0, cmds, 0, preCmds.length);
                for (int i = 0; i < ipList.length; ++i) {
                    cmds[i + preCmds.length] = "iptables -w -t nat -A Shadowsocks -p all -d " + ipList[i] + " -j RETURN";
                }
                cmds[cmds.length - 2] = "iptables -w -t nat -A Shadowsocks -p tcp -j DNAT --to-destination 127.0.0.1:8200";
                cmds[cmds.length - 1] = "iptables -w -t nat -A OUTPUT -j Shadowsocks";
            }

            createOngoingNotification();
            synchronized (ShadowsocksTransproxy.this) {
                mShellAdding = new Shell("su", cmds, 0, (isOk, stderr, stdout) -> {
                    synchronized (ShadowsocksTransproxy.this) {
                        mShellAdding = null;
                    }
                    isOk = isOk & (stderr.size() < 20);
                    Logger.d("XposedShadowsocksTransproxy start result " + isOk + ", cost " + (System.currentTimeMillis() - startTime) + "ms");
                    toast("iptables set " + isOk);
                    cancelOngoingNotification();
                    if (!isOk) {
                        createFailureNotification();
                    }
                });
                mShellAdding.execute();
            }
        });
    }

    @Override
    public void android_app_Service__stopForeground__int(ILTweaks.MethodParam param) {
        beforeOnClass(TRANSPROXY_SERVICE, param, () -> {
            synchronized (ShadowsocksTransproxy.this) {
                if (mShellAdding != null) {
                    mShellAdding.forceClean();
                    mShellAdding = null;
                }
            }

            String[] cmds = new String[] {
                    "iptables -w -t nat -D OUTPUT -j Shadowsocks",
                    "iptables -w -t nat -F Shadowsocks",
                    "iptables -w -t nat -X Shadowsocks",
            };
            new Shell("su", cmds, 0, (isOk, stderr, stdout) -> {
                isOk = isOk & (stderr.size() == 0);
                Logger.d("XposedShadowsocksTransproxy stop result " + isOk);
                toast("iptables unset " + isOk);
            }).execute();

            mStarted = false;
        });
    }

    private void createOngoingNotification() {
        cancelNotification(8201);
        createNotification(8200, true, "Adding iptables rules...");
    }

    private void cancelOngoingNotification() {
        cancelNotification(8200);
    }

    private void createFailureNotification() {
        createNotification(8201, false, "Failed to add iptables rules.");
    }

    private void createNotification(int id, boolean isOngoing, String text) {
        NotificationManager notificationManager = (NotificationManager) LTHelper.currentApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(LTHelper.currentApplication())
                .setSmallIcon(ContextUtils.getDrawableId("ic_service_active"))
                .setWhen(0)
                .setOngoing(isOngoing)
                .setTicker(text)
                .setDefaults(0)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentTitle("Shadowsocks transproxy")
                .setContentText(text)
                .setChannelId("Shadowsocks transproxy iptables")
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        if (isOngoing) {
            builder.setProgress(0, 0, true);
        }
        notificationManager.createNotificationChannel(
                new NotificationChannel("Shadowsocks transproxy iptables", "Shadowsocks transproxy iptables", NotificationManager.IMPORTANCE_LOW));
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    private void cancelNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) LTHelper.currentApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private void toast(final String msg) {
        try {
            Object core = ReflectUtils.getStaticObjectField(findClass(CORE), "INSTANCE");
            Handler handler = (Handler) ReflectUtils.callMethod(core, "getHandler");
            handler.post(() -> {
                Toast.makeText(LTHelper.currentApplication(), msg, Toast.LENGTH_SHORT).show();
            });
        } catch (Throwable e) {
            Logger.e("Toast exception.", e);
        }
    }
}
