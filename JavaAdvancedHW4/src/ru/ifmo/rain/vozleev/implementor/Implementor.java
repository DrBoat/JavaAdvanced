package ru.ifmo.rain.vozleev.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class Implementor implements Impler {

    private static final String INDENT = "    ";

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("ERROR! Argument is not interface");
        }

        if (token == null || root == null) {
            throw new ImplerException("ERROR! Contains null argument");
        }

        Path folderPath = root.resolve(token.getPackage() != null ?
                token.getPackage().getName().replace('.', File.separatorChar) :
                "");
        Path filePath = folderPath.resolve(token.getSimpleName() + "Impl.java");

        try {
            Files.createDirectories(folderPath);
        } catch (IOException e) {
            throw new ImplerException("ERROR! Can't create output file directories: " + e.getMessage());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {

            writer.write(generatePackage(token));
            writer.write(generateTitle(token));

            writer.write(" {" + System.lineSeparator());
            for (Method method : token.getMethods()) {
                if (method.isDefault()) {
                    continue;
                }
                int modifiers = method.getModifiers();
                if (Modifier.isAbstract(modifiers)) {
                    modifiers -= Modifier.ABSTRACT;
                }
                if (Modifier.isTransient(modifiers)) {
                    modifiers -= Modifier.TRANSIENT;
                }

                writer.write(generateAnnotations(method));
                writer.write(generateModifiers(method, modifiers));
                writer.write(generateArguments(method));
                writer.write(generateExceptions(method));
                writer.write(generateInnerCode(method));
            }
            writer.write("}");
        } catch (IOException e) {
            throw new ImplerException(e.getMessage());
        }
    }


    private String generatePackage(Class<?> token) {
        String packageName = token.getPackage().getName();
        return (packageName != null ? ("package " + packageName + ";") : "") +
                System.lineSeparator() + System.lineSeparator();
    }

    private String generateTitle(Class<?> token) {
        String interfaceName = token.getSimpleName();
        String className = interfaceName + "Impl";
        int mod = token.getModifiers();
        if (Modifier.isAbstract(mod)) {
            mod -= Modifier.ABSTRACT;
        }
        if (Modifier.isInterface(mod)) {
            mod -= Modifier.INTERFACE;
        }
        return (format("%s class %s implements %s", Modifier.toString(mod), className, interfaceName));
    }

    private String generateAnnotations(Method method) {
        return (Arrays.stream(method.getAnnotations()).map(p -> "@" + p.annotationType().getCanonicalName())
                .collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator());
    }

    private String generateModifiers(Method method, int modifiers) {
        return (System.lineSeparator() + INDENT + Modifier.toString(modifiers) + " " +
                method.getReturnType().getCanonicalName() + " " + method.getName());
    }

    private String generateArguments(Method method) {
        return ("(" + Arrays.stream(method.getParameters())
                .map(p -> p.getType().getCanonicalName() + " " + p.getName())
                .collect(Collectors.joining(", ")) + ")");
    }

    private String generateExceptions(Method method) {
        return (method.getExceptionTypes().length != 0 ?
                (" throws " + Arrays.stream(method.getExceptionTypes()).map(Class::getCanonicalName).
                        collect(Collectors.joining(", "))) :
                "");
    }

    private String generateInnerCode(Method method) {
        StringBuilder innerCode = new StringBuilder(" {" + System.lineSeparator());
        if (method.getReturnType() != void.class) {
            innerCode.append(generateReturn(method));
        }
        innerCode.append(System.lineSeparator()).append(INDENT).append("}").append(System.lineSeparator());
        return innerCode.toString();
    }

    private String generateReturn(Method method) {
        StringBuilder ret = new StringBuilder(INDENT).append(INDENT).append("return ");
        if (method.getReturnType() == boolean.class) {
            ret.append("false");
        } else if (method.getReturnType().isPrimitive()) {
            ret.append("0");
        } else {
            ret.append("null");
        }
        ret.append(";");
        return ret.toString();
    }
}
