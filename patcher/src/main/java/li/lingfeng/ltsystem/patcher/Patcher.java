package li.lingfeng.ltsystem.patcher;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.VoidType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaksMethods;
import li.lingfeng.ltsystem.common.Config;
import li.lingfeng.ltsystem.common.Logger;
import li.lingfeng.ltsystem.common.Utils;

public class Patcher {

    static {
        Logger.TAG = "Patcher";
    }

    private static final boolean SIMULATE = false;

    private static final Map<String, String> PACKAGE_PATH_MAP = new HashMap<String, String>() {{
        put("java.lang", "/libcore/ojluni/src/main/java/");
        put("java.util", "/libcore/ojluni/src/main/java/");
        put("android.telephony", "/frameworks/base/telephony/java/");
        put("android.media", "/frameworks/base/media/java/");
        put("com.android.server", "/frameworks/base/services/core/java/");
        put("com.android.internal.telephony", "/frameworks/opt/telephony/src/java/");
        put("com.android.systemui", "/frameworks/base/packages/SystemUI/src/");
    }};
    private static final String PACKAGE_CORE_PATH = "/frameworks/base/core/java/";

    static class MethodInfo {
        String fullClass;
        String methodName;
        String paramTypes;

        public MethodInfo(String fullClass, String methodName, String paramTypes) {
            this.fullClass = fullClass;
            this.methodName = methodName;
            this.paramTypes = paramTypes;
        }

        public String getClassFullName() {
            return fullClass.replace('_', '.');
        }

        public String getClassSimpleName() {
            String[] strings = fullClass.split("_");
            return strings[strings.length - 1];
        }

        public String getClassPackageName() {
            return Utils.removeEndWithRIndexOf(getClassFullName(), '.');
        }

        public String getFilePath() {
            String name = getClassFullName();
            String path = null;
            while (name.contains(".")) {
                name = Utils.removeEndWithRIndexOf(name, '.');
                path = PACKAGE_PATH_MAP.get(name);
                if (path != null) {
                    break;
                }
            }
            if (path == null) {
                path = PACKAGE_CORE_PATH;
            }
            return Config.ANDROID_SOURCE_DIR + path + Utils.removeEndWithRIndexOf(fullClass, '$').replace('_', '/') + ".java";
        }

        public String[] getParamTypes() {
            if (paramTypes.length() == 0) {
                return new String[0];
            }
            return paramTypes.split("_");
        }
    }

    public void startPatch() throws Throwable {
        Logger.i("Start patch.");
        MethodInfo[] methods = readMethodList();
        Logger.i(methods.length + " methods in ILTweaksMethods.");
        for (MethodInfo method : methods) {
            patchMethod(method);
        }

        patchZygote();
        copyFiles();
        copyAdditionalFiles();
        Logger.i("End patch.");
    }

    private MethodInfo[] readMethodList() {
        Method[] methods = ILTweaksMethods.class.getDeclaredMethods();
        MethodInfo[] infos = new MethodInfo[methods.length];
        for (int i = 0; i < methods.length; ++i) {
            Method method = methods[i];
            String[] strings = method.getName().split("__");
            assert strings.length == 3 || strings.length == 2 : "ILTweaksMethods format error, " + method.getName();
            infos[i] = new MethodInfo(strings[0], strings[1], strings.length == 3 ? strings[2] : "");
        }
        return infos;
    }

    private void patchMethod(MethodInfo info) throws Throwable {
        Logger.i("Patching " + info.getFilePath() + " - " + info.getClassSimpleName() + "." +
                info.methodName + "(" + StringUtils.join(info.getParamTypes(), ", ") + ")");

        File file = new File(info.getFilePath());
        String content = FileUtils.readFileToString(file);
        CompilationUnit unit = JavaParser.parse(content);

        ClassOrInterfaceDeclaration cls;
        if (!info.getClassSimpleName().contains("$")) {
            cls = unit.getClassByName(info.getClassSimpleName()).get();
        } else {
            String[] names = StringUtils.split(info.getClassSimpleName(), '$');
            assert names.length == 2 : "Multi layers of nested class is not implemented.";
            ClassOrInterfaceDeclaration cls0 = unit.getClassByName(names[0]).get();
            cls = (ClassOrInterfaceDeclaration) cls0.getMembers()
                    .stream()
                    .filter(member -> member instanceof ClassOrInterfaceDeclaration
                            && ((ClassOrInterfaceDeclaration) member).getNameAsString().equals(names[1]))
                    .findFirst()
                    .get();
        }

        List<CallableDeclaration> methods = (List) cls.getMethodsBySignature(info.methodName + "_Original", info.getParamTypes());
        if (methods.size() > 0) {
            throw new RuntimeException(info.methodName + "_Original method is already in it, you should revert changes first.");
        }

        BodyDeclaration method;
        if (!info.methodName.equals("static")) {
            String[] paramTypes = Arrays.stream(info.getParamTypes())
                    .map(type -> Utils.replace$ToAngleBrackets(type))
                    .map(type -> type.replace("$array", "[]"))
                    .map(type -> type.replace('$', '.'))
                    .toArray(String[]::new);
            if (info.getClassSimpleName().equals(info.methodName)) {
                CallableDeclaration constructor = cls.getConstructorByParameterTypes(paramTypes).get();
                methods = new ArrayList<>(1);
                methods.add(constructor);
            } else {
                methods = (List) cls.getMethodsBySignature(info.methodName, paramTypes);
            }

            if (methods.size() != 1) {
                throw new RuntimeException("getMethodsBySignature return " + methods.size() + " methods, "
                        + info.methodName + "(" + StringUtils.join(paramTypes, ", ") + ")");
            }
            method = methods.get(0);
        } else {
            method = (InitializerDeclaration) cls.getMembers().stream()
                    .filter(InitializerDeclaration.class::isInstance)
                    .filter(member -> ((InitializerDeclaration) member).isStatic())
                    .findFirst()
                    .get();
        }

        Iterator<JavaToken> it = method.getTokenRange().get().iterator();
        while (it.hasNext()) {
            JavaToken token = it.next();
            if ((token.getCategory() == JavaToken.Category.IDENTIFIER && method instanceof CallableDeclaration)
                    || (token.getCategory() == JavaToken.Category.KEYWORD && method instanceof InitializerDeclaration)) {
                if (token.getText().equals(info.methodName)) {
                    Position lineCol = token.getRange().get().begin;
                    int pos = getPositionFromLineCol(content, lineCol.line, lineCol.column);
                    String substring = content.substring(pos, pos + token.getText().length());
                    if (!substring.equals(token.getText())) {
                        throw new RuntimeException("Expected substring method name " + token.getText() +
                                ", but " + substring + ", at line " + lineCol.line + ", col " + lineCol.column + ", pos " + pos);
                    }

                    String generatedMethod = method instanceof InitializerDeclaration ?
                            generateClassStaticInitializer(info) : generateHookMethod((CallableDeclaration) method, info);
                    if (SIMULATE) {
                        Logger.v(generatedMethod);
                    }
                    content = content.substring(0, pos + token.getText().length()) + generatedMethod + content.substring(pos + token.getText().length());
                    if (method instanceof ConstructorDeclaration) {
                        content = removeFieldsFinalWord(content);
                    }
                    if (!SIMULATE) {
                        FileUtils.writeStringToFile(file, content);
                    }
                    return;
                }
            }
        }
        throw new RuntimeException("Can't find method name after method walk.");
    }

    private String generateClassStaticInitializer(MethodInfo info) {
        StringBuilder builder = new StringBuilder();

        builder.append(" {\n");
        builder.append("        if (li.lingfeng.ltsystem.ILTweaksBridge.loader != null) {\n");
        builder.append("            li.lingfeng.ltsystem.ILTweaks.MethodParam param = li.lingfeng.ltsystem.ILTweaksBridge.paramCreator.create(null);\n");
        builder.append("            li.lingfeng.ltsystem.ILTweaksBridge.loader.methods." + info.fullClass + "__static__(param);\n");
        builder.append("            if (param.hasHook()) {\n");
        builder.append("                param.hookBefore();\n");
        builder.append("                if (!param.hasResult()) {\n");
        builder.append("                    static_Original();\n");
        builder.append("                    param.hookAfter();\n");
        builder.append("                }\n");
        builder.append("            } else {\n");
        builder.append("                static_Original();\n");
        builder.append("            }\n");
        builder.append("        } else {\n");
        builder.append("            static_Original();\n");
        builder.append("        }\n");
        builder.append("    }\n");
        builder.append("    private static void static_Original()");

        return builder.toString();
    }

    private String generateHookMethod(CallableDeclaration method, MethodInfo info) {
        StringBuilder builder = new StringBuilder();
        String commaParamsWithType = Utils.joinT(method.getParameters(), ", ",
                ((param, i) -> ((Parameter) param).getTypeAsString() + ' ' + ((Parameter) param).getNameAsString()));
        String commaThrows = Utils.joinT(method.getThrownExceptions(), ", ", ((type, i) -> ((ReferenceType) type).getElementType().asString()));
        builder.append("(" + commaParamsWithType + ") " + (method.getThrownExceptions().size() > 0 ? "throws " + commaThrows : "") + " {\n");

        String commaParams = Utils.joinT(method.getParameters(), ", ", ((param, i) -> ((Parameter) param).getNameAsString()));
        String commaModifiedParams = Utils.joinT(method.getParameters(), ", ", ((param, i) -> "(" + ((Parameter) param).getTypeAsString() + ") param.args[" + i + "]"));
        boolean isVoidReturn = method instanceof ConstructorDeclaration || ((MethodDeclaration) method).getType().getClass() == VoidType.class;
        String returnKeyword = isVoidReturn ? "" : "return ";
        String callOriginal = info.methodName + "_Original(" + commaParams + ")";
        String callOriginalWithModifiedParams = info.methodName + "_Original(" + commaModifiedParams + ")";
        String callOriginalWithReturn = returnKeyword + callOriginal;
        String returnHookedResult = method.getThrownExceptions().size() == 0
                ?
                "return " + (isVoidReturn ? "" : "(" + ((MethodDeclaration) method).getTypeAsString() + ") param.getResult()") + ";\n"
                :
                "try {\n" +
                "   return " + (isVoidReturn ? "" : "(" + ((MethodDeclaration) method).getTypeAsString() + ") param.getResultOrThrowable()") + ";\n" +
                "} catch (Throwable e) {\n" +
                "   throw (" + method.getThrownException(0).asString() + ") e;\n" +
                "}\n"
                ;

        builder.append("        if (li.lingfeng.ltsystem.ILTweaksBridge.loader != null) {\n");
        builder.append("            li.lingfeng.ltsystem.ILTweaks.MethodParam param = li.lingfeng.ltsystem.ILTweaksBridge.paramCreator.create(" + (method.isStatic() ? "null": "this") + (info.getParamTypes().length != 0 ? ", ": "") + commaParams + ");\n");
        builder.append("            li.lingfeng.ltsystem.ILTweaksBridge.loader.methods." + info.fullClass + "__" + info.methodName + "__" + info.paramTypes + "(param);\n");
        builder.append("            if (param.hasHook()) {\n");
        builder.append("                param.hookBefore();\n");
        builder.append("                if (param.hasResult()) {\n");
        builder.append("                    " + returnHookedResult);
        builder.append("                }\n");

        if (isVoidReturn) {
            builder.append("                if (param.isArgsModified()) " + callOriginalWithModifiedParams + "; else " + callOriginal + ";\n");
        } else {
            builder.append("                " + ((MethodDeclaration) method).getTypeAsString() + " originalResult = " + "param.isArgsModified() ? " + callOriginalWithModifiedParams + " : " + callOriginal + ";\n");
        }

        if (!isVoidReturn) {
            builder.append("                param.setResultSilently(originalResult);\n");
        }
        builder.append("                param.hookAfter();\n");

        if (!isVoidReturn) {
            builder.append("                if (param.hasResult()) {\n");
            builder.append("                    " + returnHookedResult);
            builder.append("                } else {\n");
            builder.append("                    return originalResult;\n");
            builder.append("                }\n");
        }

        builder.append("            } else {\n");
        builder.append("                " + callOriginalWithReturn + ";\n");
        builder.append("            }\n");
        builder.append("        } else {\n");
        builder.append("            " + callOriginalWithReturn + ";\n");
        builder.append("        }\n");
        builder.append("    }\n");
        builder.append("    private " + (method.isStatic() ? "static " : "") + (method.isGeneric() ? "<" + method.getTypeParameter(0).asString() + "> " : "") + (method instanceof MethodDeclaration ? ((MethodDeclaration) method).getTypeAsString() : "void") + " " + info.methodName + "_Original");

        return builder.toString();
    }

    private String removeFieldsFinalWord(final String content) {
        List<Integer> positions = new ArrayList<>();
        CompilationUnit unit = JavaParser.parse(content);
        unit.getTypes().forEach((type) -> {
            type.getFields().forEach((field) -> {
                if (field.isFinal() && !field.getVariable(0).getInitializer().isPresent()) {
                    Iterator<JavaToken> it = field.getTokenRange().get().iterator();
                    while (it.hasNext()) {
                        JavaToken token = it.next();
                        if (token.getCategory() == JavaToken.Category.KEYWORD && token.getText().equals("final")) {
                            Position lineCol = token.getRange().get().begin;
                            int pos = getPositionFromLineCol(content, lineCol.line, lineCol.column);
                            positions.add(pos);
                            break;
                        }
                    }
                }
            });
        });
        positions.sort(Comparator.<Integer>naturalOrder().reversed());

        String result = content;
        for (Integer pos : positions) {
            if (SIMULATE) {
                Logger.v("Remove fields final word at pos " + pos);
            }
            result = result.substring(0, pos) + result.substring(pos + 5);
        }
        return result;
    }

    private void patchZygote() throws Throwable {
        String zygoteFilePath = Config.ANDROID_SOURCE_DIR + "/frameworks/base/core/java/com/android/internal/os/ZygoteInit.java";
        Logger.i("Patching " + zygoteFilePath);

        File file = new File(zygoteFilePath);
        String content = FileUtils.readFileToString(file);
        if (content.contains("LTweaksBridge")) {
            throw new RuntimeException("ZygoteInit.java is already be patched, please revert first.");
        }

        CompilationUnit unit = JavaParser.parse(content);
        ClassOrInterfaceDeclaration cls = unit.getClassByName("ZygoteInit").get();
        List<MethodDeclaration> methods = cls.getMethodsBySignature("main", "String[]");
        if (methods.size() != 1) {
            throw new RuntimeException("getMethodsBySignature return " + methods.size() + " methods.");
        }
        MethodDeclaration method = methods.get(0);

        Iterator<JavaToken> it = method.getTokenRange().get().iterator();
        while (it.hasNext()) {
            JavaToken token = it.next();
            if (token.getCategory() == JavaToken.Category.SEPARATOR) {
                if (token.getText().equals("{")) {
                    Position lineCol = token.getRange().get().begin;
                    int pos = getPositionFromLineCol(content, lineCol.line, lineCol.column);
                    String substring = content.substring(pos, pos + token.getText().length());
                    if (!substring.equals(token.getText())) {
                        throw new RuntimeException("Expected substring method name " + token.getText() +
                                ", but " + substring + ", at line " + lineCol.line + ", col " + lineCol.column + ", pos " + pos);
                    }

                    content = content.substring(0, pos + 1) + "\n"
                            + "        li.lingfeng.ltsystem.LTweaksBridge.initInZygote();\n"
                            + content.substring(pos + 1);
                    if (!SIMULATE) {
                        FileUtils.writeStringToFile(file, content);
                    }
                    return;
                }
            }
        }
        throw new RuntimeException("Can't find '{' after method walk.");
    }

    private void copyFiles() throws Throwable {
        if (SIMULATE) {
            return;
        }
        File dstFrameworkDir = new File(Config.ANDROID_SOURCE_DIR + "/frameworks/base/core/java/li/lingfeng/ltsystem");
        File dstLibcoreDir = new File(Config.ANDROID_SOURCE_DIR + "/libcore/ojluni/src/main/java/li/lingfeng/ltsystem");
        if (dstFrameworkDir.exists()) {
            FileUtils.deleteDirectory(dstFrameworkDir);
        }
        if (dstLibcoreDir.exists()) {
            FileUtils.deleteDirectory(dstLibcoreDir);
        }
        File srcDir = new File("./api/src/main/java/li/lingfeng/ltsystem");

        File[] srcFiles = srcDir.listFiles();
        File mkFile = new File(Config.ANDROID_SOURCE_DIR + "/libcore/openjdk_java_files.bp");
        String mkContent = FileUtils.readFileToString(mkFile, "UTF-8");

        for (File srcFile : srcFiles) {
            File dstDir = srcFile.getName().startsWith("ILTweaks") ? dstLibcoreDir : dstFrameworkDir;
            File dstFile = new File(dstDir.getAbsolutePath() + "/" + srcFile.getName());
            if (!dstDir.exists()) {
                FileUtils.forceMkdir(dstDir);
            }
            Logger.v("Copy " + srcFile.getAbsolutePath() + " -> " + dstFile.getAbsolutePath());
            FileUtils.copyFile(srcFile, dstFile);

            if (dstDir == dstLibcoreDir) {
                int i = mkContent.indexOf("srcs: [\n") + 8;
                mkContent = mkContent.substring(0, i) + "        \"ojluni/src/main/java/li/lingfeng/ltsystem/"
                        + dstFile.getName() + "\",\n" + mkContent.substring(i);
            }
        }
        FileUtils.writeStringToFile(mkFile, mkContent, "UTF-8");

        File pkgListFile = new File(Config.ANDROID_SOURCE_DIR + "/build/core/tasks/check_boot_jars/package_whitelist.txt");
        String pkgListContent = FileUtils.readFileToString(pkgListFile, "UTF-8");
        if (!pkgListContent.contains("li\\.lingfeng\\.ltsystem")) {
            Logger.i("Append li.lingfeng.ltsystem into package_whitelist.txt");
            pkgListContent += "\nli\\.lingfeng\\.ltsystem\n";
            FileUtils.writeStringToFile(pkgListFile, pkgListContent);
        }
    }

    private void copyAdditionalFiles() throws Throwable {
        if (SIMULATE) {
            return;
        }
        Logger.i("Copy additional files.");
        String srcDir = "./additional_files";
        FileUtils.copyDirectory(new File(srcDir), new File(Config.ANDROID_SOURCE_DIR), false);
    }

    private int getPositionFromLineCol(String content, int line, int col) {
        int i = Utils.indexOfReach(content, "\n", line - 1);
        if (i < 0) {
            throw new RuntimeException("Can't get position from line " + line + " col " + col);
        }
        return i + col;
    }

    public static void main(String[] args) throws Throwable {
        new Patcher().startPatch();
    }
}
