package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import de.christianleberfinger.melodies2go.utils.CombinedIterator;

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
		if (!destPath.exists())
		{
			throw new FileNotFoundException("Can't find " + destPath);
		}

		long availableCapacityBytes = gigabytes * FileUtils.ONE_GB;
		
		File itunesLibrary = findiTunesLibrary();

		Melodies2Go sync = new Melodies2Go();
		sync.readiTunesLibrary(itunesLibrary);
		
		List<ITrack> filteredTracks = sync.compileSelection(availableCapacityBytes);
		sync.printStatistics(filteredTracks);
		
		FileSync fileSync = new FileSync(filteredTracks, destPath);
		List<SyncedTrack> syncedTracks = fileSync.sync();
		
		M3UWriter.writeRecentlyAdded(destPath, syncedTracks);
	}
	
	public static Comparator<ITrack> orderByRating = (t1, t2) -> Integer
			.compare(t1.getRating(), t2.getRating());

	public static Comparator<ITrack> orderByPlayCount = (t1, t2) -> Integer
			.compare(t1.getPlayCount(), t2.getPlayCount());

	public static Comparator<ITrack> orderByDateAdded = (t1, t2) -> t1
			.getDateAdded().compareTo(t2.getDateAdded());
	
	private List<ITrack> allTracks;

	/**
	 * Find all music tracks in iTunes Library that can be found on disk. Movies
	 * and non existing files are being ignored.
	 * 
	 * @throws SAXException 
	 */
	public void readiTunesLibrary(File itunesLibrary) throws IOException, SAXException
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

		this.allTracks = filteredTracks;
	}

	/**
	 * Compile a list of the best and most recent songs.
	 * 
	 * Uses three intermediate sorted lists (sorted by date, by rating, by play
	 * count) and combine them to get a relevant subset of the full library.
	 * 
	 * @param availableCapacityBytes
	 * @return
	 * @throws IOException
	 */
	public List<ITrack> compileSelection(long availableCapacityBytes) throws IOException
	{
		List<ITrack> bestRated = sortList(allTracks, orderByRating.reversed());
		List<ITrack> mostPlayed = sortList(allTracks, orderByPlayCount.reversed());
		List<ITrack> recentlyAdded = sortList(allTracks, orderByDateAdded.reversed());

		long fileSizeSum = 0;

		CombinedIterator<ITrack> combinedIterator = new CombinedIterator<>(bestRated, recentlyAdded, mostPlayed);
		LinkedHashSet<ITrack> combinedList = new LinkedHashSet<>();
		while(combinedIterator.hasNext())
		{
			ITrack track = combinedIterator.next();
			File trackFile = track.getFile();
			final long fileSize = trackFile.length();

			// skip files that would exceed quota
			if (fileSize + fileSizeSum > availableCapacityBytes)
			{
				continue;
			}
			
			boolean wasAdded = combinedList.add(track);
			if (wasAdded)
			{
				fileSizeSum += fileSize;
			}
		}
		
		return new ArrayList<ITrack>(combinedList);
	}
	
	private static List<ITrack> sortList(
			List<ITrack> trackList,
			Comparator<ITrack> comparator)
	{
		List<ITrack> sorted = new ArrayList<>(trackList);
		sorted.sort(comparator);

		return sorted;
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
		
		throw new FileNotFoundException("iTunes lib wasn't found in " + homeDir);
	}
	
	
}
