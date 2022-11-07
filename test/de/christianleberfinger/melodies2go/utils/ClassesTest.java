package de.christianleberfinger.melodies2go.utils;

import de.christianleberfinger.melodies2go.M3UWriter;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class ClassesTest {

    @Test
    public void getClassPaths() {
        Set<Path> paths = Classes.getClassPaths();
        assertNotNull(paths);
        assertNotEquals(0, paths.size());
    }

    @Test
    public void getClassNames() {
        Set<String> classNames = Classes.findAllClassNamesInClassPath();
        assertNotNull(classNames);
        assertNotEquals(0, classNames.size());

        assertTrue(classNames.contains(Object.class.getName()));
        assertTrue(classNames.contains(M3UWriter.class.getName()));
        assertTrue(classNames.contains(Classes.class.getName()));
        assertFalse(classNames.contains(""));
    }

}
