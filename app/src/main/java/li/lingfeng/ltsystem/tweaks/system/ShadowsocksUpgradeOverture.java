package li.lingfeng.ltsystem.tweaks.system;

import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import li.lingfeng.ltsystem.ILTweaks;
import li.lingfeng.ltsystem.R;
import li.lingfeng.ltsystem.lib.MethodsLoad;
import li.lingfeng.ltsystem.prefs.PackageNames;
import li.lingfeng.ltsystem.tweaks.TweakBase;
import li.lingfeng.ltsystem.utils.IOUtils;
import li.lingfeng.ltsystem.utils.Logger;

@MethodsLoad(packages = PackageNames.SHADOWSOCKS, prefs = R.string.key_shadowsocks_upgrade_overture)
public class ShadowsocksUpgradeOverture extends TweakBase {

    @Override
    public void java_lang_ProcessBuilder__start__(ILTweaks.MethodParam param) {
        param.before(() -> {
            ProcessBuilder processBuilder = (ProcessBuilder) param.thisObject;
            if (processBuilder.command().get(0).endsWith("liboverture.so")) {
                String workingDir = processBuilder.directory().getAbsolutePath() + "/overture_upgrade";
                Logger.d("overture working dir " + workingDir);
                File workingFile = new File(workingDir);
                if (!workingFile.exists()) {
                    workingFile.mkdir();
                }

                String version = IOUtils.uri2string(Uri.parse("content://li.lingfeng.ltsystem.resourceProvider/assets/overture/version.txt"));
                if (version == null) {
                    throw new RuntimeException("Can't get overture version.");
                }
                boolean updated = false;
                File versionFile = new File(workingDir + "/overture_version.txt");
                if (versionFile.exists()) {
                    String oldVersion = org.apache.commons.io.IOUtils.toString(new FileInputStream(versionFile));
                    if (version.equals(oldVersion)) {
                        updated = true;
                    }
                }

                if (!updated) {
                    Logger.v("Copy new overture files, version " + version);
                    copyFile("overture", workingDir);
                    new File(workingDir + "/overture").setExecutable(true);
                    copyFile("overture.conf", workingDir);
                    copyFile("china_ip_list.txt", workingDir);
                    copyFile("domain_primary", workingDir);
                    copyFile("domain_alternative", workingDir);
                    org.apache.commons.io.IOUtils.write(version, new FileOutputStream(versionFile));
                }
                ShadowsocksPrimaryDns.modifyPrimaryDNS(workingDir + "/overture.conf");
                ShadowsocksDdnsUpdate.appendDdnsDomainToOvertureConf(workingDir + "/domain_primary");

                Logger.i("Replace overture process command.");
                processBuilder.directory(workingFile);
                processBuilder.command("./overture", "-c", "overture.conf"/*, "-v"*/);
            }
        });
    }

    private void copyFile(String name, String workingDir) throws Throwable {
        if (!IOUtils.saveUriToFile(Uri.parse("content://li.lingfeng.ltsystem.resourceProvider/assets/overture/" + name),
                workingDir + "/" + name)) {
            throw new RuntimeException("copyFile fail, " + name + ", " + workingDir);
        }
    }
}
