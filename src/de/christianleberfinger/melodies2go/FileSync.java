package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;

public class FileSync
{
	private final List<RatedTrack> tracks;
	private final File destDir;

	private Map<File, RatedTrack> trackFiles = new TreeMap<>();

	public FileSync(List<RatedTrack> tracks, File destDir)
	{
		this.tracks = tracks;
		this.destDir = destDir;
	}

	public static class SyncedTrack
	{
		public final RatedTrack track;
		public final File destFile;

		public SyncedTrack(RatedTrack track, File destFile)
		{
			this.track = track;
			this.destFile = destFile;
		}
		
		public static Comparator<SyncedTrack> orderByDateAdded = (t1, t2) -> t1
				.getDateAdded().compareTo(t2.getDateAdded());

		private Date getDateAdded()
		{
			return track.getDateAdded();
		}
	}
	
	private final static IOFileFilter NO_HIDDEN_FILES = new NotFileFilter(
			new PrefixFileFilter("."));

	public List<SyncedTrack> sync() throws IOException
	{
		System.out.println("Calculating changes to destination file system.");
		List<SyncedTrack> expectedTracks = fillMap();

		System.out.println("Deleting dispensable files");
		deleteDispensableFiles();
		deleteEmptyFolders();

		System.out.println("Copying missing files");
		copyMissingFiles();
		
		System.out.println("Finished sync");
		
		return expectedTracks;
	}

	private List<SyncedTrack> fillMap()
	{
		List<SyncedTrack> expectedTracks = new ArrayList<>(tracks.size());
		for (RatedTrack track : tracks)
		{
			if (track.getFile() != null)
			{
				final File destFile = getDestFile(track);
				
				expectedTracks.add(new SyncedTrack(track, destFile));
				trackFiles.put(destFile, track);
			}
		}
		
		return expectedTracks;
	}

	/**
	 * Calculates a corresponding file name for the given track. The format will
	 * be like 'DESTDIR/A/Artist/Album/Song.mp3'
	 * 
	 * @param track
	 * @return
	 */
	public File getDestFile(RatedTrack track)
	{
		List<String> pathElements = new ArrayList<>();

		final String artist = track.getArtistPreferred();
		final String album = track.getAlbum();

		if (Objects.nonNull(artist) && artist.length() > 0)
		{
			pathElements.add(getInitialCharacterFolder(artist));
			pathElements.add(artist);

			if (Objects.nonNull(album))
			{
				pathElements.add(album);
			}
		}

		pathElements.add(track.getFile().getName());

		Path p = Paths.get(destDir.getAbsolutePath(),
				pathElements.toArray(new String[] {}));

		return p.toFile();
	}

	private String getInitialCharacterFolder(final String artist)
	{
		if (Character.isDigit(artist.charAt(0)))
		{
			return "0-9";
		}

		String firstLetter = artist.substring(0, 1);
		
		// Normalize special characters like "o with diaeresis" to a simple "o".
		// Note: This will most likely lead to suboptimal results with Asian
		// languages
		firstLetter = Normalizer.normalize(firstLetter, Form.NFD).substring(0, 1);
		
		return firstLetter.toUpperCase();
	}

	private void deleteDispensableFiles() throws IOException
	{
		Collection<File> files = FileUtils.listFiles(destDir, NO_HIDDEN_FILES,
				NO_HIDDEN_FILES);

		for (File destFile : files)
		{
			if (!trackFiles.containsKey(destFile))
			{
				FileUtils.forceDelete(destFile);
			}
		}
	}

	private void deleteEmptyFolders() throws IOException
	{
		Collection<File> folders = FileUtils.listFilesAndDirs(destDir,
				FalseFileFilter.INSTANCE, NO_HIDDEN_FILES);

		// order folders by path depth to also detect orphaned parents
		List<File> folderList = new ArrayList<>(folders);
		Collections.sort(folderList, orderByPathDepth.reversed());
		
		for (File folder : folderList)
		{
			// do not delete the destination root directory
			if (folder.equals(destDir))
			{
				continue;
			}

			if (folder.isDirectory())
			{
				if (folder.list().length == 0)
				{
					FileUtils.deleteDirectory(folder);
				}
			}
		}
	}

	private static Comparator<File> orderByPathDepth = new Comparator<File>() {
		@Override
		public int compare(File o1, File o2)
		{
			int depth1 = o1.toPath().getNameCount();
			int depth2 = o2.toPath().getNameCount();
			
			return Integer.compare(depth1, depth2);
		}
	};
	
	private void copyMissingFiles() throws IOException
	{
		for (Entry<File, RatedTrack> entry : trackFiles.entrySet())
		{
			RatedTrack track = entry.getValue();
			File destFile = entry.getKey();

			if (!destFile.exists())
			{
				if (destFile.getParentFile().exists())
				{
					FileUtils.forceMkdir(destFile.getParentFile());
				}

				copyFile(track, destFile);
			}
		}
	}

	/**
	 * Copy the given track to its destination path. Copying will be performed
	 * via temporary file. In case of errors during copying, this temporary file
	 * will be deleted. In case of sudden program exit, the temporary file will
	 * stay and will be deleted by the next run of sync().
	 * 
	 * @param track
	 * @param destFile
	 * @throws IOException
	 */
	private void copyFile(RatedTrack track, File destFile) throws IOException
	{
		TempFile tempFile = new TempFile(destFile);
		try
		{
			FileUtils.copyFile(track.getFile(), tempFile);
			tempFile.renameToOriginal();
		}
		catch (IOException e)
		{
			tempFile.delete();
			throw e;
		}
	}
}
