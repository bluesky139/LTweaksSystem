package li.lingfeng.ltsystem.magisk;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import li.lingfeng.ltsystem.common.Config;
import li.lingfeng.ltsystem.common.Logger;

public class Magisk {

    static {
        Logger.TAG = "Magisk";
    }

    private static final String OUT_PATH = Config.ANDROID_SOURCE_DIR + "/out/target/product/" + Config.DEVICE_CODE_NAME;
    private static final String OUT_SYSTEM_PATH = OUT_PATH + "/system";
    private static final String OUT_SYSTEM_OTHER_PATH = OUT_SYSTEM_PATH + "_other";
    private static final String WORKING_DIR = System.getProperty("user.dir").replace('\\', '/');
    private static final String MODULE_TEMPLATE_PATH = WORKING_DIR + "/magisk-module-template";
    private static final String MODULE_PATH = WORKING_DIR + "/magisk-module";

    public void createModule() throws Throwable {
        Logger.i("Start create magisk module.");
        cloneMagiskTemplate();
        List<String> list = generateFileList();
        Logger.i("Got " + list.size() + " files/dirs.");
        copySystemFiles(list);
        zipModule();
        Logger.i("End create magisk module.");
    }

    private void cloneMagiskTemplate() throws Throwable {
        File srcDir = new File(MODULE_TEMPLATE_PATH);
        File dstDir = new File(MODULE_PATH);
        if (dstDir.exists()) {
            FileUtils.deleteDirectory(dstDir);
        }
        Logger.i("Copy " + srcDir.getAbsolutePath() + " -> " + dstDir.getAbsolutePath());
        FileUtils.copyDirectory(srcDir, dstDir, true);
        FileUtils.forceDelete(new File(MODULE_PATH + "/system/placeholder"));
    }

    private List<String> generateFileList() {
        List<String> list = new ArrayList<>();
        list.add("framework/oat");

        Function<Pair<String, String>, List<String>> collectAllFiles = (folder) -> {
            File dir = new File(folder.getLeft() + "/" + folder.getRight());
            List<String> files = new ArrayList<>();
            for (File file : dir.listFiles()) {
                if (file.isFile() && !file.getName().endsWith(".apk")) {
                    if (file.getName().endsWith(".jar")) {
                        try {
                            ZipFile zipFile = new ZipFile(file);
                            if (zipFile.getEntry("classes.dex") == null) {
                                continue;
                            }
                        } catch (Throwable e) {
                            throw new RuntimeException(e);
                        }
                    }
                    String s = folder.getRight() + "/" + file.getName();
                    files.add(s);
                }
            }
            return files;
        };

        list.addAll(collectAllFiles.apply(Pair.of(OUT_SYSTEM_PATH, "framework/arm")));
        if (new File(OUT_SYSTEM_OTHER_PATH).exists()) {
            list.addAll(collectAllFiles.apply(Pair.of(OUT_SYSTEM_PATH, "framework/arm64")));
        }
        list.addAll(collectAllFiles.apply(Pair.of(OUT_SYSTEM_PATH, "framework")));
        list.add("etc/boot-image.prof");
        if (new File(OUT_SYSTEM_OTHER_PATH).exists()) {
            list.add("apex/com.android.runtime.release.apex");
        } else {
            list.add("apex/com.android.runtime.release/javalib/core-oj.jar");
        }
        list.add("product/priv-app/SystemUI/SystemUI.apk");
        list.add("product/priv-app/Settings/Settings.apk");

        Function<Pair<String, String>, List<String>> collectOatList = (folder) -> {
            File dir = new File(folder.getLeft() + "/" + folder.getRight());
            List<String> oatList = new ArrayList<>();
            for (String name : dir.list()) {
                String s = name + "/oat";
                if (new File(dir.getPath() + "/" + s).exists()) {
                    String s1 = folder.getRight() + "/" + s;
                    oatList.add(s1);
                }
            }
            return oatList;
        };

        list.addAll(collectOatList.apply(Pair.of(OUT_SYSTEM_PATH, "priv-app")));
        list.addAll(collectOatList.apply(Pair.of(OUT_SYSTEM_PATH, "app")));
        if (new File(OUT_SYSTEM_OTHER_PATH).exists()) {
            list.addAll(collectOatList.apply(Pair.of(OUT_SYSTEM_OTHER_PATH, "priv-app")));
            list.addAll(collectOatList.apply(Pair.of(OUT_SYSTEM_OTHER_PATH, "app")));
            list.addAll(collectOatList.apply(Pair.of(OUT_PATH, "product/priv-app")));
            list.addAll(collectOatList.apply(Pair.of(OUT_PATH, "product/app")));
        }
        return list;
    }

    private void copySystemFiles(List<String> list) throws Throwable {
        for (String path : list) {
            Logger.v("Copy " + path);
            File srcFile = new File(OUT_SYSTEM_PATH + "/" + path);
            File srcOtherFile = new File(OUT_SYSTEM_OTHER_PATH + "/" + path);
            File dstFile = new File(MODULE_PATH + "/system/" + path);
            assert !(srcFile.exists() && srcOtherFile.exists()) : "oat exists in both system and system_other.";

            if (path.startsWith("product/") && new File(OUT_SYSTEM_OTHER_PATH).exists()) {
                srcFile = new File(OUT_PATH + "/" + path);
            } else {
                srcFile = srcFile.exists() ? srcFile : srcOtherFile;
            }
            if (srcFile.isDirectory()) {
                FileUtils.copyDirectory(srcFile.exists() ? srcFile : srcOtherFile, dstFile, true);
            } else {
                FileUtils.copyFile(srcFile, dstFile, true);
            }
        }
    }

    private void zipModule() throws Throwable {
        Logger.i("Zip module.");
        File zipFile = new File(WORKING_DIR + "/magisk-ltsystem-" + Config.DEVICE_CODE_NAME + "-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".zip");
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
        new Magisk().createModule();
    }
}
