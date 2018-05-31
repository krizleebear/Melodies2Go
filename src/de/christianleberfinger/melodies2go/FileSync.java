package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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

	private final static IOFileFilter NO_HIDDEN_FILES = new NotFileFilter(
			new PrefixFileFilter("."));

	public void sync() throws IOException
	{
		fillMap();

		deleteDispensableFiles();
		deleteEmptyFolders();

		copyMissingFiles();
	}

	private void fillMap()
	{
		for (RatedTrack track : tracks)
		{
			if (track.getFile() != null)
			{
				trackFiles.put(getDestFile(track), track);
			}
		}
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
		char firstChar = artist.charAt(0);
		if (Character.isDigit(firstChar))
		{
			return "0-9";
		}

		firstChar = Character.toUpperCase(firstChar);

		return Character.toString(firstChar);
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

		for (File folder : folders)
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

	private void copyMissingFiles() throws IOException
	{
		for (Entry<File, RatedTrack> entry : trackFiles.entrySet())
		{
			RatedTrack track = entry.getValue();
			File destFile = entry.getKey();

			if (!destFile.exists())
			{
				FileUtils.forceMkdir(destFile.getParentFile());

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
