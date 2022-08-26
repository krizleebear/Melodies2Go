package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.xml.sax.SAXException;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

import de.christianleberfinger.melodies2go.FileSync.SyncedTrack;
import de.christianleberfinger.melodies2go.parser.ITrack;
import de.christianleberfinger.melodies2go.parser.ITunesXMLParser;
import de.christianleberfinger.melodies2go.parser.Tracks;
import de.christianleberfinger.melodies2go.utils.TSVExport;

/**
 * Melodies2Go is a tool to assemble relevant subsets of your iTunes music collection.
 * It helps you to automagically compile your top rated and most recent songs to
 * a portable media, e.g. an SD card or a flash drive. I built this application
 * to automatically assemble my music for my in-car infotainment system.
 * 
 * @author krizleebear
 */
public class Melodies2Go
{
	public static void main(String[] args)
			throws SAXException, IOException
	{
		if (args.length < 2)
		{
			throw new RuntimeException(
					"Usage: Melodies2Go <Size-in-GB> </path/to/destination>");
		}
		
		int gigabytes = Integer.parseInt(args[0]);
		File destPath = new File(args[1]);

		long availableCapacityBytes = gigabytes * FileUtils.ONE_GB;
		
		File itunesLibrary = findiTunesLibrary();

		Melodies2Go sync = new Melodies2Go();
		List<ITrack> allTracks = sync.readiTunesLibrary(itunesLibrary);
		
		List<ITrack> selectedTracks = TrackCompilation.compileSelection(allTracks, availableCapacityBytes);
		selectedTracks = limitTrackNumber(selectedTracks, 10_000);
		
		sync.printStatistics(selectedTracks);
		TSVExport.export(selectedTracks, Paths.get("filtered_melodies.tsv"));
		
		if (!destPath.exists())
		{
			throw new FileNotFoundException("Can't find " + destPath);
		}

		FileSync fileSync = new FileSync(selectedTracks, destPath);
		List<SyncedTrack> syncedTracks = fileSync.sync();
		
		M3UWriter.writeRecentlyAdded(destPath, syncedTracks);
	}
	
	public static <T> List<T> limitTrackNumber(List<T> tracks, int maxCount) {

		int toIndex = Math.min(maxCount, tracks.size());
		return tracks.subList(0, toIndex);
	}

	/**
	 * Find all music tracks in iTunes Library that can be found on disk. Movies
	 * and non existing files are being ignored.
	 * @return 
	 * 
	 * @throws SAXException 
	 */
	public List<ITrack> readiTunesLibrary(File itunesLibrary) throws IOException, SAXException
	{
		Tracks tracks = ITunesXMLParser.parseLibrary(itunesLibrary);
		List<ITrack> filteredTracks = new ArrayList<>(tracks.size());
		for(ITrack ratedTrack : tracks)
		{
			// ignore tracks without valid file information
			File trackFile = ratedTrack.getFile();
			if (trackFile == null || !trackFile.exists()
					|| trackFile.isDirectory())
			{
				continue;
			}

			// ignore movies
			if (ratedTrack.hasVideo())
			{
				continue;
			}
			
			// ignore disabled tracks
			if (ratedTrack.isDisabled())
			{
				continue;
			}

			filteredTracks.add(ratedTrack);
		}

		return filteredTracks;
	}

	private void printStatistics(List<ITrack> filteredTracks)
	{
		System.out.println("Number of tracks: " + filteredTracks.size());
		
		Multiset<String> artists = HashMultiset.create();
		Multiset<String> years = HashMultiset.create();
		Multiset<String> genres = HashMultiset.create();

		for (ITrack track : filteredTracks)
		{
			if (track.getArtist() != null)
			{
				artists.add(track.getArtist());
			}

			if (track.getYear() != null)
			{
				years.add(track.getYear());
			}
			
			if(track.getGenre() != null)
			{
				genres.add(track.getGenre());
			}
		}

		ImmutableMultiset<String> tracksByArtist = Multisets
				.copyHighestCountFirst(artists);
		ImmutableMultiset<String> tracksByYear = Multisets
				.copyHighestCountFirst(years);
		ImmutableMultiset<String> tracksByGenre = Multisets
				.copyHighestCountFirst(genres);

		System.out.println("Artists: " + tracksByArtist);
		System.out.println("Genres : " + tracksByGenre);
		System.out.println("Years  : " + tracksByYear);
	}
	
	public static File findiTunesLibrary() throws FileNotFoundException
	{
		File homeDir = new File(System.getProperty("user.home"));
		File musicDir = new File(homeDir, "Music");
		File iTunesDir = new File(musicDir, "iTunes");
		
		Iterator<File> files = FileUtils.iterateFiles(iTunesDir, new String[] {"xml"}, true);
		
		while(files.hasNext())
		{
			File file = files.next();
			if(file.getName().equals("iTunes Library.xml"))
			{
				return file;
			}
		}
		
		throw new FileNotFoundException("iTunes lib wasn't found in " + homeDir + ". " +
				"Note: Apple Music doesn't automatically export the library as XML file. " +
				"You have to do so manually.");
	}
	
	
}
