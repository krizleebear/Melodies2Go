package de.christianleberfinger.melodies2go;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.FileUtils;

public class TempFile extends File implements AutoCloseable
{
	private static final long serialVersionUID = -1829816855361332838L;
	private final File originalFile;

	public TempFile(File originalFile)
	{
		super(originalFile.getParentFile(),
				appendSuffix(originalFile.getName()));
		this.originalFile = originalFile;
	}

	private static String appendSuffix(String originalName)
	{
		return originalName + ".tmp";
	}

	public TempFile(Path path)
	{
		this(path.toFile());
	}

	public void renameToOriginal() throws IOException
	{
		StandardCopyOption replaceExisting = StandardCopyOption.REPLACE_EXISTING;
		Files.move(this.toPath(), originalFile.toPath(), replaceExisting);
	}

	@Override
	public void close() throws IOException {
		FileUtils.forceDelete(this);
	}
}
