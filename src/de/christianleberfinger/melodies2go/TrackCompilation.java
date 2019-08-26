package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import de.christianleberfinger.melodies2go.parser.ITrack;
import de.christianleberfinger.melodies2go.utils.CombinedIterator;

public class TrackCompilation {

	public static Comparator<ITrack> orderByRating = (t1, t2) -> Integer
			.compare(t1.getRating(), t2.getRating());

	public static Comparator<ITrack> orderByPlayCount = (t1, t2) -> Integer
			.compare(t1.getPlayCount(), t2.getPlayCount());

	public static Comparator<ITrack> orderByDateAdded = (t1, t2) -> t1
			.getDateAdded().compareTo(t2.getDateAdded());
	
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
	public static List<ITrack> compileSelection(List<ITrack> allTracks, long availableCapacityBytes) throws IOException
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
	
}
