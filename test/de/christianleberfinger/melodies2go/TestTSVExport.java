package de.christianleberfinger.melodies2go;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.xml.sax.SAXException;

import de.christianleberfinger.melodies2go.parser.ITrack;
import de.christianleberfinger.melodies2go.utils.TSVExport;

public class TestTSVExport {

	@Test
	public void test() throws IOException, SAXException {
		File itunesLibrary = Melodies2Go.findiTunesLibrary();

		Melodies2Go sync = new Melodies2Go();
		sync.readiTunesLibrary(itunesLibrary);

		List<ITrack> filteredTracks = sync.compileSelection(300 * FileUtils.ONE_MB);

		Path tempFile = Files.createTempFile(Paths.get("."), "export", ".tsv");
		tempFile.toFile().deleteOnExit();
		
		TSVExport.export(filteredTracks, tempFile);
		assertTrue(Files.size(tempFile) > 0);
	}

}
