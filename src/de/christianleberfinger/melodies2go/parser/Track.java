package de.christianleberfinger.melodies2go.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import de.christianleberfinger.melodies2go.utils.TimeAgoFormatter;

public class Track implements ITrack
{
	private final String artist;
	private final String album;
	private final String albumArtist;
	private final String year;
	private final String genre;
	private final boolean isRatingComputed;
	private final int playCount;
	private final Date dateAdded;
	private final String title;
	private final int trackNumber;
	private final File file;
	private final boolean isDisabled;
	private final boolean hasVideo;
	private final int trackRating;

	private Track(TrackBuilder builder)
	{
		artist = builder.artist;
		album = builder.album;
		albumArtist = builder.albumArtist;
		year = builder.year;
		genre = builder.genre;
		isRatingComputed = builder.isRatingComputed;
		playCount = builder.playCount;
		dateAdded = builder.dateAdded;
		title = builder.title;
		trackNumber = builder.trackNumber;
		file = builder.file;
		isDisabled = builder.isDisabled;
		hasVideo = builder.hasVideo;
		trackRating = builder.trackRating;
	}

	@Override
	public String getArtist()
	{
		return artist;
	}

	@Override
	public String getAlbum()
	{
		return album;
	}

	@Override
	public String getAlbumArtist()
	{
		return albumArtist;
	}

	@Override
	public String getYear()
	{
		return year;
	}

	@Override
	public String getGenre()
	{
		return genre;
	}
	
	@Override
	public int getTrackRating()
	{
		return trackRating;
	}

	@Override
	public boolean isTrackRatingComputed()
	{
		return isRatingComputed;
	}

	@Override
	public int getPlayCount()
	{
		return playCount;
	}

	@Override
	public Date getDateAdded()
	{
		return dateAdded;
	}

	@Override
	public String getTitle()
	{
		return title;
	}

	@Override
	public int getTrackNumber()
	{
		return trackNumber;
	}

	@Override
	public File getFile()
	{
		return file;
	}

	@Override
	public boolean isDisabled()
	{
		return isDisabled;
	}

	@Override
	public boolean hasVideo()
	{
		return hasVideo;
	}

	public static class TrackBuilder
	{
		public int trackRating;
		private String artist;
		private String album = "";
		private String albumArtist;
		private String year;
		private String genre;
		private boolean isRatingComputed;
		private int playCount;
		private Date dateAdded;
		private String title;
		private int trackNumber = 0;
		private File file;
		private boolean isDisabled;
		private boolean hasVideo;

		public TrackBuilder artist(String artist)
		{
			this.artist = artist;
			return this;
		}

		public TrackBuilder album(String album)
		{
			this.album = album;
			return this;
		}

		public TrackBuilder albumArtist(String albumArtist)
		{
			this.albumArtist = albumArtist;
			return this;
		}

		public TrackBuilder year(String year)
		{
			this.year = year;
			return this;
		}

		public TrackBuilder genre(String genre)
		{
			this.genre = genre;
			return this;
		}

		public TrackBuilder isRatingComputed(boolean isRatingComputed)
		{
			this.isRatingComputed = isRatingComputed;
			return this;
		}
		
		public TrackBuilder trackRating(int trackRating)
		{
			this.trackRating = trackRating;
			return this;
		}

		public TrackBuilder playCount(int playCount)
		{
			this.playCount = playCount;
			return this;
		}

		public TrackBuilder dateAdded(Date dateAdded)
		{
			this.dateAdded = dateAdded;
			return this;
		}

		public TrackBuilder title(String title)
		{
			this.title = title;
			return this;
		}

		public TrackBuilder trackNumber(int trackNumber)
		{
			this.trackNumber = trackNumber;
			return this;
		}

		public TrackBuilder fileLocation(String fileLocation)
		{
			// URLs with authority component (e.g. localhost) can't be
			// parsed.
			// NOK: file://localhost/Users
			// OK: file:///Users/
			try
			{
				URL url = new URL(fileLocation);
				if (url.getAuthority() != null
						&& !"".equals(url.getAuthority()))
				{
					fileLocation = fileLocation.replace("file://localhost/",
							"file:///");
					url = new URL(fileLocation);
				}

				if (!"file".equalsIgnoreCase(url.getProtocol()))
				{
					throw new MalformedURLException();
				}

				Path path = Paths.get(url.toURI());
				File trackFile = path.toFile();
				this.file = trackFile;

			}
			catch (MalformedURLException | URISyntaxException e)
			{
				this.file = null;
			}

			return this;
		}

		public TrackBuilder isDisabled(boolean isDisabled)
		{
			this.isDisabled = isDisabled;
			return this;
		}

		public Track build()
		{
			return new Track(this);
		}

		public void hasVideo(boolean hasVideo)
		{
			this.hasVideo = hasVideo;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Track [");
		if (getArtist() != null)
			builder.append("Artist=").append(getArtist()).append(", ");
		if (getTitle() != null)
			builder.append("Title=").append(getTitle()).append(", ");
		builder.append("Rating=").append(getRating())
				.append(", PlayCount=").append(getPlayCount())
				.append(", ");
		if (getDateAdded() != null)
		{
			String daysAgo = TimeAgoFormatter.formatToDaysAgo(getDateAdded());
			builder.append("DateAdded=").append(daysAgo);
		}
		builder.append("]");
		return builder.toString();
	}
}
