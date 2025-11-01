package org.example.runners;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MethodSignatureRetriever {

    // New method for retrieving the signature using a parser, but missing fully qualified name of method parameters
    public static String getMethodSignatureParsed(int methodLine, String className) {
        String classSourceDir = "src/main/java/";

        String classPath = classSourceDir + className +  ".java";

        CompilationUnit cu = null;
        try {
            cu = StaticJavaParser.parse(Files.newInputStream(Paths.get(classPath)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        StringBuilder methodSignature = new StringBuilder();

        cu.findAll(MethodDeclaration.class).forEach((methodDeclaration) -> {
            methodDeclaration.getBegin().ifPresent(begin -> {
                if (begin.line == methodLine) {
                    methodDeclaration.getParameters().forEach(p -> {
                        methodSignature.append(p.getType()).append(",");
                    });
                }
            });
        });

        cu.findAll(ConstructorDeclaration.class).forEach(constructorDeclaration -> {
            constructorDeclaration.getBegin().ifPresent(begin -> {
                if (begin.line == methodLine) {
                    constructorDeclaration.getParameters().forEach(p -> {
                        methodSignature.append(p.getType()).append(",");
                    });
                }
            });
        });

        int lastColumn = methodSignature.lastIndexOf(",");

        if (lastColumn != -1) {
            String sig = methodSignature.substring(0,lastColumn);
            return sig;
        } else {
            return methodSignature.toString();
        }
    }
}
