package de.christianleberfinger.melodies2go.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Classes {

    public static Set<String> findAllClassNamesInClassPath() {
        Set<Path> classPathElements = getClassPaths();
        Set<String> classes = new LinkedHashSet<>();
        classPathElements.forEach(classPathElement -> addAllClassesFrom(classPathElement, classes));
        return classes;
    }

    protected static void addAllClassesFrom(Path p, Set<String> classes) {
        if (Files.isDirectory(p)) {
            addClassNamesFromDir(p, classes);
        } else {
            if (isClassFile(p)) {
                String className = classNameFromFilePath(p.toString());
                classes.add(className);
            } else {
                addClassNamesFromJar(p, classes);
            }
        }
    }

    private static void addClassNamesFromDir(Path dir, Set<String> classNames) {
        try (Stream<Path> children = Files.walk(dir)) {
            children.filter(Classes::isClassFile)
                    .forEach(child -> {
                        Path relativePath = dir.relativize(child);
                        String className = classNameFromFilePath(relativePath.toString());
                        classNames.add(className);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void addClassNamesFromJar(Path p, Set<String> classes) {
        try (JarFile jar = new JarFile(p.toFile())) {
            jar.stream()
                    .filter(Classes::isClassEntry)
                    .map(JarEntry::getName)
                    .map(Classes::classNameFromFilePath)
                    .forEach(classes::add);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String classNameFromFilePath(String path) {
        path = path.substring(0, path.lastIndexOf(".class"));
        return path.replaceAll("/", ".");
    }

    protected static Set<Path> getClassPaths() {
        String classpathProp = System.getProperty("java.class.path");
        String[] classPathElements = classpathProp.split(File.pathSeparator);
        Set<Path> classPaths = new HashSet<>(classPathElements.length);
        for (String element : classPathElements) {
            Path path = Paths.get(element);
            if (Files.exists(path)) {
                classPaths.add(path);
            }
        }

        return classPaths;
    }

    private static boolean isClassEntry(JarEntry entry) {
        return entry.getName().endsWith(".class");
    }

    private static boolean isClassFile(Path p) {
        return Files.isRegularFile(p) &&
                p.getFileName().toString().endsWith(".class");
    }
}
