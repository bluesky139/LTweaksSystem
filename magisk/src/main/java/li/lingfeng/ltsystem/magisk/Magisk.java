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
import java.util.zip.ZipOutputStream;

import li.lingfeng.ltsystem.common.Config;
import li.lingfeng.ltsystem.common.Logger;

public class Magisk {

    static {
        Logger.TAG = "Magisk";
    }

    private static final String OUT_SYSTEM_PATH = Config.ANDROID_SOURCE_DIR + "/out/target/product/" + Config.DEVICE_CODE_NAME + "/system";
    private static final String OUT_SYSTEM_OTHER_PATH = OUT_SYSTEM_PATH + "_other";
    private static final String WORKING_DIR = System.getProperty("user.dir").replace('\\', '/');
    private static final String MODULE_TEMPLATE_PATH = WORKING_DIR + "/magisk-module-template";
    private static final String MODULE_PATH = WORKING_DIR + "/magisk-module";
    private static final String MODULE_CONFIG_PATH = MODULE_PATH + "/config.sh";

    public void createModule() throws Throwable {
        Logger.i("Start create magisk module.");
        cloneMagiskTemplate();
        List<String> list = generateFileList();
        Logger.i("Got " + list.size() + " files/dirs.");
        copySystemFiles(list);
        replaceFileListInConfig(list);
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
        list.add("framework/arm");
        list.add("framework/oat");
        if (new File(OUT_SYSTEM_OTHER_PATH).exists()) {
            list.add("framework/arm64");
        }
        list.add("priv-app/SystemUI/SystemUI.apk");

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
            srcFile = srcFile.exists() ? srcFile : srcOtherFile;
            if (srcFile.isDirectory()) {
                FileUtils.copyDirectory(srcFile.exists() ? srcFile : srcOtherFile, dstFile, true);
            } else {
                FileUtils.copyFile(srcFile, dstFile, true);
            }
        }
    }

    private void replaceFileListInConfig(List<String> list) throws Throwable {
        Logger.i("Replace file list in config.");
        File file = new File(MODULE_CONFIG_PATH);
        String content = FileUtils.readFileToString(file, "UTF-8");
        content = content.replace("#REPLACE#", list.stream()
                .map(path -> "/system/" + path)
                .reduce((all, path) -> all + '\n' + path).get());
        FileUtils.writeStringToFile(file, content, "UTF-8");
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
