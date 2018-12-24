package li.lingfeng.ltsystem.patcher;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.VoidType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import li.lingfeng.ltsystem.ILTweaksMethods;

public class Patcher {

    private static final boolean SIMULATE = false;

    private static final Map<String, String> PACKAGE_PATH_MAP = new HashMap<String, String>() {{
        put("com.android.server", "/frameworks/base/services/core/java/");
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
            return Config.ANDROID_SOURCE_DIR + path + fullClass.replace('_', '/') + ".java";
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
        ClassOrInterfaceDeclaration cls = unit.getClassByName(info.getClassSimpleName()).get();

        List<MethodDeclaration> methods = cls.getMethodsBySignature(info.methodName + "_Original", info.getParamTypes());
        if (methods.size() > 0) {
            throw new RuntimeException(info.methodName + "_Original method is already in it, you should revert changes first.");
        }

        String[] paramTypes = Arrays.stream(info.getParamTypes()).map(type -> Utils.replace$ToAngleBrackets(type)).toArray(String[]::new);
        methods = cls.getMethodsBySignature(info.methodName, paramTypes);
        if (methods.size() != 1) {
            throw new RuntimeException("getMethodsBySignature return " + methods.size() + " methods, "
                    + info.methodName + "(" + StringUtils.join(paramTypes, ", ") + ")");
        }
        MethodDeclaration method = methods.get(0);

        Iterator<JavaToken> it = method.getTokenRange().get().iterator();
        while (it.hasNext()) {
            JavaToken token = it.next();
            if (token.getCategory() == JavaToken.Category.IDENTIFIER) {
                if (token.getText().equals(info.methodName)) {
                    Position lineCol = token.getRange().get().begin;
                    int pos = getPositionFromLineCol(content, lineCol.line, lineCol.column);
                    String substring = content.substring(pos, pos + token.getText().length());
                    if (!substring.equals(token.getText())) {
                        throw new RuntimeException("Expected substring method name " + token.getText() +
                                ", but " + substring + ", at line " + lineCol.line + ", col " + lineCol.column + ", pos " + pos);
                    }

                    String generatedMethod = generateHookMethod(method, info);
                    if (SIMULATE) {
                        Logger.v(generatedMethod);
                    }
                    content = content.substring(0, pos) + generatedMethod + content.substring(pos + token.getText().length());
                    if (!SIMULATE) {
                        FileUtils.writeStringToFile(file, content);
                    }
                    return;
                }
            } else if (token.getCategory() == JavaToken.Category.SEPARATOR) {
                if (token.getText().equals("{")) {
                    throw new Exception("Can't find method name position.");
                }
            }
        }
        throw new RuntimeException("Can't find method name after method walk.");
    }

    private String generateHookMethod(MethodDeclaration method, MethodInfo info) {
        StringBuilder builder = new StringBuilder();
        String commaParamsWithType = Utils.joinT(method.getParameters(), ", ",
                ((param, i) -> ((Parameter) param).getTypeAsString() + ' ' + ((Parameter) param).getNameAsString()));
        String commaThrows = Utils.joinT(method.getThrownExceptions(), ", ", ((type, i) -> ((ReferenceType) type).getElementType().asString()));
        builder.append(info.methodName + "(" + commaParamsWithType + ") " + (method.getThrownExceptions().size() > 0 ? "throws " + commaThrows : "") + " {\n");

        String commaParams = Utils.joinT(method.getParameters(), ", ", ((param, i) -> ((Parameter) param).getNameAsString()));
        String commaModifiedParams = Utils.joinT(method.getParameters(), ", ", ((param, i) -> "(" + ((Parameter) param).getTypeAsString() + ") param.args[" + i + "]"));
        boolean isVoidReturn = method.getType().getClass() == VoidType.class;
        String returnKeyword = isVoidReturn ? "" : "return ";
        String callOriginal = info.methodName + "_Original(" + commaParams + ")";
        String callOriginalWithModifiedParams = info.methodName + "_Original(" + commaModifiedParams + ")";
        String callOriginalWithReturn = returnKeyword + callOriginal;
        String returnHookedResult = "return " + (isVoidReturn ? "" : "(" + method.getTypeAsString() + ") param.getResult" + (method.getThrownExceptions().size() == 0 ? "" : "OrThrowable") + "()") + ";\n";

        builder.append("        if (li.lingfeng.ltsystem.LTweaksBridge.loader != null) {\n");
        builder.append("            li.lingfeng.ltsystem.ILTweaks.MethodParam param = new li.lingfeng.ltsystem.ILTweaks.MethodParam(" + (method.isStatic() ? "null": "this") + (info.getParamTypes().length != 0 ? ", ": "") + commaParams + ");\n");
        builder.append("            li.lingfeng.ltsystem.LTweaksBridge.loader.methods." + info.fullClass + "__" + info.methodName + "__" + info.paramTypes + "(param);\n");
        builder.append("            if (param.hasHook()) {\n");
        builder.append("                param.hookBefore();\n");
        builder.append("                if (param.hasResult()) {\n");
        builder.append("                    " + returnHookedResult);
        builder.append("                }\n");

        if (isVoidReturn) {
            builder.append("                if (param.isArgsModified()) " + callOriginalWithModifiedParams + "; else " + callOriginal + ";\n");
        } else {
            builder.append("                " + method.getTypeAsString() + " originalResult = " + "param.hasResult() ? " + callOriginalWithModifiedParams + " : " + callOriginal + ";\n");
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
        builder.append("    private " + (method.isStatic() ? "static " : "") + method.getTypeAsString() + " " + info.methodName + "_Original");

        return builder.toString();
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
        File dstDir = new File(Config.ANDROID_SOURCE_DIR + "/frameworks/base/core/java/li");
        if (dstDir.exists()) {
            FileUtils.deleteDirectory(dstDir);
        }
        File srcDir = new File("./api/src/main/java/li");
        Logger.i("Copy " + srcDir.getAbsolutePath() + " -> " + dstDir.getAbsolutePath());
        FileUtils.copyDirectory(srcDir, dstDir);
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
