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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.gps.itunes.lib.exceptions.LibraryParseException;
import com.gps.itunes.lib.exceptions.NoChildrenException;
import com.gps.itunes.lib.items.tracks.Track;
import com.gps.itunes.lib.parser.ItunesLibraryParsedData;
import com.gps.itunes.lib.parser.ItunesLibraryParser;
import com.gps.itunes.lib.parser.utils.PropertyManager;
import com.gps.itunes.lib.tasks.LibraryParser;

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
			throws LibraryParseException, NoChildrenException, IOException
	{
		long availableCapacityBytes = 7 * FileUtils.ONE_GB;
		File itunesLibrary = findiTunesLibrary();

		Melodies2Go sync = new Melodies2Go();
		sync.readiTunesLibrary(itunesLibrary);
		
		List<RatedTrack> filteredTracks = sync.compileSelection(availableCapacityBytes);

		final File listFile = new File("list.txt");
		sync.writeFileList(listFile, filteredTracks);
		
		sync.printStatistics(filteredTracks);
		
		System.out.println("rsync --archive --human-readable --progress --files-from \"" + listFile.getAbsolutePath() + "\" / /Volumes/CHLE");
	}
	
	private Comparator<RatedTrack> orderByRating = (t1, t2) -> Integer
			.compare(t1.getRating(), t2.getRating());

	private Comparator<RatedTrack> orderByPlayCount = (t1, t2) -> Integer
			.compare(t1.getPlayCount(), t2.getPlayCount());

	private Comparator<RatedTrack> orderByDateAdded = (t1, t2) -> t1
			.getDateAdded().compareTo(t2.getDateAdded());
	
	private Comparator<RatedTrack> orderByFileName = (t1, t2) -> t1
			.getFile().compareTo(t2.getFile());
	

	private List<RatedTrack> allTracks;

	/**
	 * Find all music tracks in iTunes Library that can be found on disk. Movies
	 * and non existing files are being ignored.
	 * 
	 * @throws LibraryParseException
	 * @throws NoChildrenException
	 */
	public void readiTunesLibrary(File itunesLibrary) throws LibraryParseException, NoChildrenException
	{
		ItunesLibraryParser itunesLibraryParser = new LibraryParser();

		String libraryFilePath = itunesLibrary.getAbsolutePath();
		itunesLibraryParser.addParseConfiguration(
				PropertyManager.Property.LIBRARY_FILE_LOCATION_PROPERTY
						.getKey(),
				libraryFilePath);
		ItunesLibraryParsedData itunesLibraryParsedData = itunesLibraryParser
				.parse();

		Track[] tracks = itunesLibraryParsedData.getAllTracks();

		List<RatedTrack> trackList = new ArrayList<>(tracks.length);
		for (Track track : tracks)
		{
			final RatedTrack ratedTrack = new RatedTrack(track);

			// ignore tracks without valid file information
			File trackFile = ratedTrack.getFile();
			if (trackFile == null || !trackFile.exists()
					|| trackFile.isDirectory())
			{
				continue;
			}

			// ignore movies
			if (ratedTrack.isMovie())
			{
				continue;
			}
			
			// ignore disabled tracks
			if (ratedTrack.isDisabled())
			{
				continue;
			}

			trackList.add(ratedTrack);
		}

		this.allTracks = trackList;
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
	public List<RatedTrack> compileSelection(long availableCapacityBytes) throws IOException
	{
		List<RatedTrack> bestRated = sortList(allTracks, orderByRating.reversed());
		List<RatedTrack> mostPlayed = sortList(allTracks, orderByPlayCount.reversed());
		List<RatedTrack> recentlyAdded = sortList(allTracks, orderByDateAdded.reversed());

		long fileSizeSum = 0;

		CombinedIterator<RatedTrack> combinedIterator = new CombinedIterator<>(bestRated, recentlyAdded, mostPlayed);
		LinkedHashSet<RatedTrack> combinedList = new LinkedHashSet<>();
		while(combinedIterator.hasNext())
		{
			RatedTrack track = combinedIterator.next();
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
		
		return new ArrayList<RatedTrack>(combinedList);
	}
	
	public void writeFileList(File listFile, List<RatedTrack> tracks) throws IOException
	{
		ArrayList<RatedTrack> orderedByFilename = Lists.newArrayList(tracks);
		orderedByFilename.sort(orderByFileName);
		FileUtils.writeLines(new File("trackinfos.txt"), "UTF-8", orderedByFilename );
		
		List<String> filePaths = new ArrayList<>(tracks.size());
		tracks.stream().forEach(
				track -> filePaths.add(escapeFilename(track.getFile().getAbsolutePath())));

		FileUtils.writeLines(listFile, "UTF-8", filePaths);

		// TODO: copy sorted files to some destination directory, e.g. an SD card
	}

	private static String escapeFilename(String filename)
	{
//		filename = filename.replace("*", "\\*");
//		filename = filename.replace("[", "\\[");
//		filename = filename.replace("?", "\\?");
//		
//		filename = filename.replaceFirst("/Users/krizleebear/Music/", "+ ");
		
		return filename;
//		return "+ " + filename;
//		return Pattern.quote(filename);
	}
	
	private static List<RatedTrack> sortList(
			List<RatedTrack> trackList,
			Comparator<RatedTrack> comparator)
	{
		List<RatedTrack> sorted = new ArrayList<>(trackList);
		sorted.sort(comparator);

		return sorted;
	}

	private void printStatistics(List<RatedTrack> filteredTracks)
	{
		Multiset<String> artists = HashMultiset.create();
		Multiset<String> years = HashMultiset.create();
		Multiset<String> genres = HashMultiset.create();

		for (RatedTrack track : filteredTracks)
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
