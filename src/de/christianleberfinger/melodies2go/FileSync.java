package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
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

import de.christianleberfinger.melodies2go.parser.ITrack;

public class FileSync
{
	private final List<ITrack> tracks;
	private final File destDir;

	private Map<File, ITrack> trackFiles = new TreeMap<>();

	public FileSync(List<ITrack> tracks, File destDir)
	{
		this.tracks = tracks;
		this.destDir = destDir;
	}

	public static class SyncedTrack
	{
		public final ITrack track;
		private final File destFile;

		public SyncedTrack(ITrack track, File destFile)
		{
			this.track = track;
			this.destFile = destFile;
		}
		
		public File getDestFile()
		{
			return destFile;
		}
		
		public static Comparator<SyncedTrack> orderByDateAdded = (t1, t2) -> t1
				.getDateAdded().compareTo(t2.getDateAdded());
		
		public Date getDateAdded()
		{
			return track.getDateAdded();
		}
		
		public String getAlbum()
		{
			return track.getAlbum();
		}
		
		public int getTrackNumber()
		{
			return track.getTrackNumber();
		}
		
		public String toString()
		{
			return getDestFile().toString();
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((getDestFile() == null) ? 0 : getDestFile().hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SyncedTrack other = (SyncedTrack) obj;
			if (getDestFile() == null)
			{
				if (other.getDestFile() != null)
					return false;
			}
			else if (!getDestFile().equals(other.getDestFile()))
				return false;
			return true;
		}
	}
	
	private final static IOFileFilter NO_HIDDEN_FILES = new NotFileFilter(
			new PrefixFileFilter("."));

	public List<SyncedTrack> sync() throws IOException
	{
		System.out.println("Calculating changes to destination file system.");
		List<SyncedTrack> expectedTracks = getDestinationFiles();

		System.out.println("Deleting dispensable files");
		deleteDispensableFiles();
		deleteEmptyFolders();

		System.out.println("Copying missing files");
		copyMissingFiles();
		
		System.out.println("Finished sync");
		
		return expectedTracks;
	}

	private List<SyncedTrack> getDestinationFiles()
	{
		List<SyncedTrack> expectedTracks = new ArrayList<>(tracks.size());
		for (ITrack track : tracks)
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
	public File getDestFile(ITrack track)
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

		// replace invalid characters for the target file system (such as /)
		sanitizePathElements(pathElements);

		Path p = Paths.get(destDir.getAbsolutePath(),
				pathElements.toArray(new String[] {}));

		return p.toFile();
	}

	public static void sanitizePathElements(List<String> pathElements) {

		pathElements.replaceAll(FileSync::sanitizeFilename);
		
	}
	
	public static String sanitizeFilename(String name)
	{
		return name.replaceAll("[/\\\\]", "_");
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
		for (Entry<File, ITrack> entry : trackFiles.entrySet())
		{
			ITrack track = entry.getValue();
			File destFile = entry.getKey();

			if (!destFile.exists())
			{
				try {
					copyFile(track, destFile);
				} catch (FileSystemException fse) {
					// sometimes, music files just can't be copied, even with 'cp' on terminal.
					// just ignore those files and print an error, as this should be a rare case.
					System.err.println("Filesystem Error copying " + destFile);
				}
			}
		}
	}

	/**
	 * Copy the given track to its destination path. Copying will be performed
	 * via temporary file. In case of errors during copying, this temporary file
	 * will be deleted. In case of sudden program exit, the temporary file might
	 * stay and will be deleted by the next run of sync().
	 * 
	 * All needed parent directories will be created.
	 * 
	 * @param track
	 * @param destFile
	 * @throws IOException
	 */
	private void copyFile(ITrack track, File destFile) throws IOException
	{
		FileUtils.forceMkdir(destFile.getParentFile());
		
		try(TempFile tempFile = new TempFile(destFile))
		{
			System.out.println("Copying " + destFile);
			FileUtils.copyFile(track.getFile(), tempFile);
			tempFile.renameToOriginal();
		}
	}
}
