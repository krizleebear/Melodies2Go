package de.christianleberfinger.melodies2go;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;

import com.google.common.collect.Lists;

public class TestFileSync {

	@Test
	public void test() {
		
		TreeMap<Path, String> tree = new TreeMap<>();
		
		Path p1 = Paths.get("/Volumes/bla/blub.mp3");
		Path p2 = Paths.get("/Volumes/bla/space blub.mp3");
		
		tree.put(p1, "ok");
		tree.put(p2, "ok");
		
		assertNotNull(tree.get(p1));
		assertNotNull(tree.get(p2));
	}

	@Test
	public void testSanitizeFilename()
	{
		List<String> pathElements = Lists.newArrayList("1/2\\3 4.mp3");
		FileSync.sanitizePathElements(pathElements);
		
		assertEquals(1, pathElements.size());
		assertEquals("1_2_3 4.mp3", pathElements.get(0));
	}
}
