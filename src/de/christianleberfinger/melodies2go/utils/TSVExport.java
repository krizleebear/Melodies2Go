package de.christianleberfinger.melodies2go.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.christianleberfinger.melodies2go.parser.ITrack;

public class TSVExport {

	public static void export(List<ITrack> tracks, Path path) throws IOException {

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			tracks.forEach(track -> write(writer, track));
		}
	}

	private static void write(BufferedWriter writer, ITrack track) {

		try {
			writer.write(escape(track.getArtist()));
			writer.write("\t");

			writer.write(escape(track.getAlbum()));
			writer.write("\t");

			writer.write(escape(track.getTitle()));
			writer.write("\t");

			writer.write("\n");

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static String escape(String s)
	{
		if(s == null)
		{
			return "";
		}
		
		return s;
	}

}
