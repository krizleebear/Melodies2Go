package de.christianleberfinger.melodies2go;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import de.christianleberfinger.melodies2go.parser.ITrack;
import de.christianleberfinger.melodies2go.parser.Track;
import de.christianleberfinger.melodies2go.utils.TSVExport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTSVExport {

	@Test
	public void test() throws IOException {

		Track t = new Track.TrackBuilder().artist("artist").title("title").build();

		List<ITrack> tracks = Lists.newArrayList(t);

		Path tempFile = Files.createTempFile(Paths.get("."), "export", ".tsv");
		tempFile.toFile().deleteOnExit();

		TSVExport.export(tracks, tempFile);
		assertTrue(Files.size(tempFile) > 0);

		List<String> lines = Files.readAllLines(tempFile);
		assertEquals(1, lines.size());
	}

}
