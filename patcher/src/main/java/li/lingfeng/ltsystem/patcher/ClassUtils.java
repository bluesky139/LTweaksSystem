package li.lingfeng.ltsystem.patcher;

import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class ClassUtils {

    public static Optional<ConstructorDeclaration> classGetConstructorByParameterTypes(ClassOrInterfaceDeclaration cls, String... paramTypes) {
        return cls.getConstructors().stream().filter(m -> methodHasParametersOfType(m, paramTypes)).findFirst();
    }

    public static List<MethodDeclaration> classGetMethodsBySignature(ClassOrInterfaceDeclaration cls, String name, String... paramTypes) {
        return unmodifiableList(cls.getMethodsByName(name).stream()
                .filter(m -> methodHasParametersOfType(m, paramTypes))
                .collect(toList()));
    }

    public static boolean methodHasParametersOfType(CallableDeclaration method, String... paramTypes) {
        return method.getParameters().size() == paramTypes.length && method.getParameters().stream()
                .map(p -> {
                    Type type = ((Parameter) p).getType();
                    return type instanceof ClassOrInterfaceType && ((ClassOrInterfaceType) type).getTypeArguments().isPresent()
                            ? ((ClassOrInterfaceType) type).getName().toString() : type.toString();
                })
                .collect(toSet())
                .equals(Stream.of(paramTypes).collect(toSet()));
    }
}
