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

	public File getDestFile(RatedTrack track)
	{
		List<String> pathElements = new ArrayList<>();
		if (Objects.nonNull(track.getArtist()))
		{
			pathElements.add(track.getArtist());
		}
		if (Objects.nonNull(track.getAlbum()))
		{
			pathElements.add(track.getAlbum());
		}

		pathElements.add(track.getFile().getName());

		Path p = Paths.get(destDir.getAbsolutePath(),
				pathElements.toArray(new String[] {}));

		return p.toFile();
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

	private void copyMissingFiles() throws IOException
	{
		for (Entry<File, RatedTrack> entry : trackFiles.entrySet())
		{
			RatedTrack track = entry.getValue();
			File destFile = entry.getKey();

			if (!destFile.exists())
			{
				FileUtils.forceMkdir(destFile.getParentFile());
				FileUtils.copyFile(track.getFile(), destFile);
			}
		}
	}
}
