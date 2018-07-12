package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import de.christianleberfinger.melodies2go.FileSync.SyncedTrack;

public class M3UWriter
{
	private File m3uFile;
	private List<SyncedTrack> tracks;

	public static void writeRecentlyAdded(File deviceRoot,
			List<SyncedTrack> syncedTracks) throws IOException
	{
		int expectedTracks = 500;

		SortedSet<SyncedTrack> recentlyAdded = new TreeSet<>(
				ORDER_BY_RECENTLY_ADDED);
		
		recentlyAdded.addAll(syncedTracks);

		List<SyncedTrack> playList = recentlyAdded.stream()
				.limit(expectedTracks).collect(Collectors.toList());
		final File m3uFile = new File(deviceRoot, "recentlyAdded.m3u");
		M3UWriter writer = new M3UWriter(m3uFile, playList);
		writer.write();

		System.out.println("Wrote " + m3uFile);
	}
	
	/**
	 * Orders tracks like iTunes 'last added' playlist: First order by 'date
	 * added', but try to keep the ordering of tracks within the same album
	 * intact. <br/>
	 * <br/>
	 * It's crucial also to order by a unique field (in this case: DestFile)
	 * when sorting into a TreeSet. Elements with same ordering will otherwise
	 * be detected as 'equal' and will be eliminated.
	 */
	public static final Comparator<SyncedTrack> ORDER_BY_RECENTLY_ADDED = new Comparator<SyncedTrack>() {

		@Override
		public int compare(SyncedTrack o1, SyncedTrack o2)
		{
			return Comparator.comparing(SyncedTrack::getDateAdded).reversed().//
			thenComparing(SyncedTrack::getAlbum). //
			thenComparingInt(SyncedTrack::getTrackNumber).//
			thenComparing(SyncedTrack::getDestFile). //
			compare(o1, o2);
		}

	};

	public M3UWriter(File m3uFile, List<SyncedTrack> tracks)
	{
		this.m3uFile = m3uFile;
		this.tracks = tracks;
	}

	public void write() throws IOException
	{
		Path deviceRoot = m3uFile.getParentFile().getAbsoluteFile().toPath();

		List<String> relativePaths = new ArrayList<>(tracks.size());
		for (SyncedTrack track : tracks)
		{
			Path relativePath = deviceRoot.relativize(track.getDestFile().toPath());
			relativePaths.add(relativePath.toString());
		}
		FileUtils.writeLines(m3uFile, "UTF-8", relativePaths);
	}
}
