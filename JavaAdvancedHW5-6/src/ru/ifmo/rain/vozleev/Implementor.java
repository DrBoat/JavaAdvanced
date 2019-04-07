package ru.ifmo.rain.vozleev;

import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

import static java.lang.String.format;

/**
 * Implementation class for {@link JarImpler} interface
 */
public class Implementor implements JarImpler {

    /**
     * Intended for generated classes.
     */
    private static final String INDENT = "    ";

    /**
     * Creates new instance of {@link Implementor}
     */
    public Implementor() {
    }

    /**
     * @throws ImplerException if the given class cannot be generated for one of such reasons:
     *                         <ul>
     *                         <li> Some arguments are null</li>
     *                         <li> Given class is primitive or array. </li>
     *                         <li> Given class is final class or {@link Enum}. </li>
     *                         <li> class isn't an interface and contains only private constructors. </li>
     *                         <li> The problems with I/O occurred during implementation. </li>
     *                         </ul>
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface()) {
            throw new ImplerException("ERROR! Argument is not interface");
        }

        if (root == null) {
            throw new ImplerException("ERROR! Contains null argument");
        }

        try (var writer = Files.newBufferedWriter(generateImplInterfacePath(token, root, true, "Impl.java"))) {

            writer.write(toUnicode(generatePackage(token)));
            writer.write(toUnicode(generateTitle(token)));

            writer.write(toUnicode(" {" + System.lineSeparator()));
            for (var method : token.getMethods()) {
                if (method.isDefault()) {
                    continue;
                }
                var modifiers = method.getModifiers();
                if (Modifier.isAbstract(modifiers)) {
                    modifiers -= Modifier.ABSTRACT;
                }
                if (Modifier.isTransient(modifiers)) {
                    modifiers -= Modifier.TRANSIENT;
                }

                writer.write(toUnicode(generateAnnotations(method)));
                writer.write(toUnicode(generateModifiers(method, modifiers)));
                writer.write(toUnicode(generateArguments(method)));
                writer.write(toUnicode(generateExceptions(method)));
                writer.write(toUnicode(generateInnerCode(method)));
            }
            writer.write("}");
        } catch (IOException e) {
            throw new ImplerException("ERROR! Can't create output file directories: " + e.getMessage());
        }
    }

    /**
     * Generates {@link Path} to realization of token.
     *
     * @param token     {@link Class} needed to be realized
     * @param root      {@link Path} to root of realization of interface
     * @param createDir true if creates directories, false else
     * @param suffix    suffix of file to create
     * @return {@link Path} to realization of interface
     * @throws IOException if couldn't create directories to realization of interface or get {@link Path} to it
     */
    private Path generateImplInterfacePath(Class<?> token, Path root, boolean createDir, String suffix) throws IOException {
        root = generateImplInterfaceDirectoryPath(token, root, createDir);
        return root.resolve(token.getSimpleName() + suffix);
    }

    /**
     * Gets {@link Path} to lowest directory from root to token's package
     *
     * @param token     {@link Class} which package is used to get directory names
     * @param root      {@link Path} uses to start from this point
     * @param createDir true if creates directories, false else
     * @return {@link Path} to lowest directory from root to token's package
     * @throws IOException if couldn't create directories or resolve path to token's package
     */
    private Path generateImplInterfaceDirectoryPath(Class<?> token, Path root, boolean createDir) throws IOException {
        var directoryPath = root;
        if (token.getPackage() != null) {
            directoryPath = root.resolve(token.getPackage().getName().replace(".", File.separator) + File.separator);
            if (createDir) {
                Files.createDirectories(directoryPath);
            }
        }
        return directoryPath;
    }

    /**
     * Converts given string to unicode escaping
     *
     * @param in {@link String} to convert
     * @return converted string
     */
    private static String toUnicode(String in) {
        StringBuilder b = new StringBuilder();
        for (char c : in.toCharArray()) {
            if (c >= 128) {
                b.append(String.format("\\u%04X", (int) c));
            } else {
                b.append(c);
            }
        }
        return b.toString();
    }

    /**
     * Returns package, that containing the class
     *
     * @param token base class or implemented interface
     * @return {@link String} representing name of the package
     */
    private String generatePackage(Class<?> token) {
        var packageName = token.getPackage().getName();
        return (packageName != null ? ("package " + packageName + ";") : "") +
                System.lineSeparator() + System.lineSeparator();
    }

    /**
     * Returns beginning declaration of the class, name, base class or implemented interface
     *
     * @param token base class or implemented interface
     * @return {@link String} representing beginning of class declaration
     */
    private String generateTitle(Class<?> token) {
        var interfaceName = token.getSimpleName();
        var className = interfaceName + "Impl";
        var mod = token.getModifiers();
        if (Modifier.isAbstract(mod)) {
            mod -= Modifier.ABSTRACT;
        }
        if (Modifier.isInterface(mod)) {
            mod -= Modifier.INTERFACE;
        }
        return (format("%s class %s implements %s", Modifier.toString(mod), className, interfaceName));
    }

    /**
     * Returns annotations of the {@link Method}
     *
     * @param method current method
     * @return {@link String} representing list of annotations in current {@link Method}
     */
    private String generateAnnotations(Method method) {
        return (Arrays.stream(method.getAnnotations()).map(p -> "@" + p.annotationType().getCanonicalName())
                .collect(Collectors.joining(System.lineSeparator())) + System.lineSeparator());
    }

    /**
     * Returns modifiers of the {@link Method}
     *
     * @param method    current {@link Method}
     * @param modifiers current modifiers
     * @return {@link String} representing list of modifiers
     */
    private String generateModifiers(Method method, int modifiers) {
        return (System.lineSeparator() + INDENT + Modifier.toString(modifiers) + " " +
                method.getReturnType().getCanonicalName() + " " + method.getName());
    }

    /**
     * Returns arguments of the {@link Method}
     *
     * @param method current {@link Method}
     * @return {@link String} representing list of arguments
     */
    private String generateArguments(Method method) {
        return ("(" + Arrays.stream(method.getParameters())
                .map(p -> p.getType().getCanonicalName() + " " + p.getName())
                .collect(Collectors.joining(", ")) + ")");
    }

    /**
     * Returns exceptions that could be thrown by the {@link Method}
     *
     * @param method current {@link Method}
     * @return {@link String} representing list of exceptions
     */
    private String generateExceptions(Method method) {
        return (method.getExceptionTypes().length != 0 ?
                (" throws " + Arrays.stream(method.getExceptionTypes()).map(Class::getCanonicalName).
                        collect(Collectors.joining(", "))) :
                "");
    }

    /**
     * Returns inner code for {@link Method}
     *
     * @param method current {@link Method}
     * @return {@link String} representing body
     */
    private String generateInnerCode(Method method) {
        var innerCode = new StringBuilder(" {" + System.lineSeparator());
        if (method.getReturnType() != void.class) {
            innerCode.append(generateReturn(method));
        }
        innerCode.append(System.lineSeparator()).append(INDENT).append("}").append(System.lineSeparator());
        return innerCode.toString();
    }

    /**
     * Returns default value for {@link Method}
     *
     * @param method current {@link Method}
     * @return {@link String} representing value
     */
    private String generateReturn(Method method) {
        var ret = new StringBuilder(INDENT).append(INDENT).append("return ");
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


    /**
     * Produces .jar file implementing class or interface specified by provided token.
     * <p>
     * Generated class full name should be same as full name of the type token with Impl suffix
     * added.
     * <p>
     * During implementation creates temporary folder to store temporary .java and .class files.
     *
     * @throws ImplerException if the given class cannot be generated for one of such reasons:
     *                         <ul>
     *                         <li> Some arguments are null</li>
     *                         <li> Error occurs during implementation via {@link #implement(Class, Path)} </li>
     *                         <li> The process is not allowed to create files or directories. </li>
     *                         <li> {@link JavaCompiler} failed to compile implemented class </li>
     *                         <li> The problems with I/O occurred during implementation. </li>
     *                         </ul>
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        try {
            var rootTemp = Files.createTempDirectory(".");
            var implementor = new Implementor();
            implementor.implement(token, rootTemp);

            var javaTempFilePath = generateImplInterfacePath(token, rootTemp, true, "Impl.java");
            var compiler = ToolProvider.getSystemJavaCompiler();
            var args = new ArrayList<>();
            args.add(javaTempFilePath.toString());
            args.add("-cp");
            args.add(rootTemp + File.pathSeparator + System.getProperty("java.class.path"));
            compiler.run(null, null, null, args.toArray(new String[args.size()]));

            var classTempFilePath = generateImplInterfacePath(token, rootTemp, false, "Impl.class");
            var classFilePathInJar = generateImplInterfacePath(token, Paths.get(""), false, "Impl.class");
            var manifest = new Manifest();
            manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

            try (var out = new JarOutputStream(Files.newOutputStream(jarFile), manifest)) {
                out.putNextEntry(new ZipEntry(classFilePathInJar.normalize().toString()));
                Files.copy(classTempFilePath, out);
                out.closeEntry();
            }


        } catch (IOException e) {
            System.out.print("ERROR: Can't create jar file: " + e.getMessage());
        }
    }


    /**
     * This function is used to choose which way of implementation to execute.
     * Runs {@link Implementor} in two possible ways:
     * <ul>
     * <li> 2 arguments: className rootPath - runs {@link #implement(Class, Path)} with given arguments</li>
     * <li> 3 arguments: -jar className jarPath - runs {@link #implementJar(Class, Path)} with two second arguments</li>
     * </ul>
     * If arguments are incorrect or an error occurs during implementation returns message with information about error
     *
     * @param args arguments for running an application
     * @throws ImplerException if arguments are invalid
     */
    public static void main(String[] args) throws ImplerException {
        if (args == null) {
            throw new ImplerException("ERROR! Seco argumen should be [-jar] className [*.jar]");
        }

        var implementor = new Implementor();
        try {
            if (args.length == 3 && args[0] != null && args[1] != null && args[2] != null) {
                if (args[0].equals("-jar")) {
                    implementor.implementJar(Class.forName(args[1]), Paths.get(args[2]));
                } else {
                    throw new ImplerException("ERROR! First argumen should be [-jar] className [*.jar]");
                }
                return;
            }
            if (args.length == 1 && args[0] != null) {
                implementor.implement(Class.forName(args[0]), Paths.get("./implemented"));
            } else {
                throw new ImplerException("ERROR! Seco argumen should be [-jar] className [*.jar]");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Class: " + args[0] + " not found");
        } catch (InvalidPathException e) {
            System.out.println("Invalid path(" + args[1] + ") to class: " + args[1]);
        } catch (ImplerException e) {
            System.out.println("Error while implementing interface: " + e.getMessage());
        }
    }
}
