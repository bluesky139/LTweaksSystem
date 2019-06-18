package li.lingfeng.ltsystem.magisk;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import li.lingfeng.ltsystem.common.Logger;

public class MagiskApp {

    static {
        Logger.TAG = "Magisk";
    }

    private static final String WORKING_DIR = System.getProperty("user.dir").replace('\\', '/');
    private static final String MODULE_TEMPLATE_PATH = WORKING_DIR + "/magisk-module-app-template";
    private static final String MODULE_PATH = WORKING_DIR + "/magisk-module-app";

    public void createModule() throws Throwable {
        Logger.i("Start create magisk app module.");
        cloneMagiskTemplate();
        copyReleaseApp();
        zipModule();
        Logger.i("End create magisk app module.");
    }

    private void cloneMagiskTemplate() throws Throwable {
        File srcDir = new File(MODULE_TEMPLATE_PATH);
        File dstDir = new File(MODULE_PATH);
        if (dstDir.exists()) {
            FileUtils.deleteDirectory(dstDir);
        }
        Logger.i("Copy " + srcDir.getAbsolutePath() + " -> " + dstDir.getAbsolutePath());
        FileUtils.copyDirectory(srcDir, dstDir, true);
    }

    private void copyReleaseApp() throws Throwable {
        String srcPath = WORKING_DIR + "/app/release/app-release.apk";
        String dstPath = MODULE_PATH + "/system/priv-app/LTweaks/LTweaks.apk";
        Logger.i("Copy " + srcPath + " -> " + dstPath);
        FileUtils.copyFile(new File(srcPath), new File(dstPath));
    }

    private void zipModule() throws Throwable {
        Logger.i("Zip module.");
        File zipFile = new File(WORKING_DIR + "/magisk-ltsystem-app-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip");
        if (zipFile.exists()) {
            FileUtils.forceDelete(zipFile);
        }

        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        Collection<File> files = FileUtils.listFiles(new File(MODULE_PATH), null, true);
        for (File file : files) {
            String name = file.getAbsolutePath().replace('\\', '/').substring(MODULE_PATH.length() + 1);
            out.putNextEntry(new ZipEntry(name));
            out.write(FileUtils.readFileToByteArray(file));
            out.closeEntry();
        }
        out.close();
        Logger.i("Output: " + zipFile.getAbsolutePath());
    }

    public static void main(String[] args) throws Throwable {
        new MagiskApp().createModule();
    }
}
