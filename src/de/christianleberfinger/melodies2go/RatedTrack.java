package de.christianleberfinger.melodies2go;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

import com.gps.itunes.lib.items.tracks.Track;

import de.christianleberfinger.melodies2go.utils.ThreadLocalDateParser;
import de.christianleberfinger.melodies2go.utils.TimeAgoFormatter;

public class RatedTrack extends Track
{
	private static final String FIELD_DATE_ADDED = "Date Added";
	private static final String FIELD_PLAY_COUNT = "Play Count";
	private static final String FIELD_TRACK_RATING = "Rating";
	private static final String FIELD_TRACK_NUMBER = "Track Number";
	
	// example: Fri Feb 19 17:28:39 CET 2016
	private static ThreadLocalDateParser dateParser = new ThreadLocalDateParser(
			"EEE MMM d HH:mm:ss zzz yyyy");

	private final Date dateAdded;
	
	public RatedTrack(Track track)
	{
		super(track.getTrackId(), track.getTrackName(), track.getLocation(),
				track.getAdditionalTrackInfo());
		
		String dateAddedString = getAdditionalInfo(FIELD_DATE_ADDED);
		dateAdded = parseDateString(dateAddedString);
	}

	public String getArtist()
	{
		return getAdditionalInfo("Artist");
	}
	
	public String getAlbum()
	{
		return getAdditionalInfo("Album", "");
	}
	
	public String getAlbumArtist()
	{
		return getAdditionalInfo("Album Artist");
	}
	
	public String getYear()
	{
		return getAdditionalInfo("Year");
	}
	
	public String getGenre()
	{
		return getAdditionalInfo("Genre");
	}
	
	/**
	 * Get track rating in percent (100% = best). <br/>
	 * Special treatment for computed ratings: <br/>
	 * Computed rankings are ratings that were not specifically assigned by the
	 * user to this track, but calculated from the album rating.
	 * 
	 * The algorithm credits this fact by reducing the rating.
	 * 
	 * @return
	 */
	public int getRating()
	{
		if (isTrackRatingComputed())
		{
			return getAdditionalInfoInteger(FIELD_TRACK_RATING, 0) / 5;
		}
		
		return getAdditionalInfoInteger(FIELD_TRACK_RATING, 0);
	}
	
	public int getAlbumRating()
	{
		if (isAlbumRatingComputed())
		{
			return 0;
		}

		return getAdditionalInfoInteger("Album Rating", 0);
	}

	public boolean isTrackRatingComputed()
	{
		return "true".equals(
				getAdditionalInfo("Rating Computed"));
	}

	public boolean isAlbumRatingComputed()
	{
		return "true".equals(getAdditionalTrackInfo()
				.getAdditionalInfo("Album Rating Computed"));
	}

	public int getPlayCount()
	{
		return getAdditionalInfoInteger(FIELD_PLAY_COUNT, 0);
	}

	public String getAdditionalInfo(String key)
	{
		return getAdditionalTrackInfo().getAdditionalInfo(key);
	}
	
	public String getAdditionalInfo(String key, String defaultValue)
	{
		String value = getAdditionalInfo(key);
		if (value == null)
		{
			return defaultValue;
		}

		return value;
	}

	public int getAdditionalInfoInteger(String key, int defaultValue)
	{
		String value = getAdditionalInfo(key);
		if (value == null || value.length() < 1)
		{
			return defaultValue;
		}

		try
		{
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			return 0;
		}
	}

	public static Date parseDateString(String dateString)
	{
		if (dateString != null)
		{
			try
			{
				return dateParser.parse(dateString);
			}
			catch (ParseException e)
			{
				return new Date(0);
			}
		}

		return new Date(0);
	}

	/**
	 * Get date added (or Date(0) if not available)
	 * 
	 * @return
	 */
	public Date getDateAdded()
	{
		return dateAdded;
	}

	public String getTitle()
	{
		return getTrackName();
	}
	
	public int getTrackNumber()
	{
		return getAdditionalInfoInteger(FIELD_TRACK_NUMBER, 0);
	}

	/**
	 * Return the track's file location. If the track's location isn't valid
	 * (e.g. an URL), NULL will be returned.
	 * 
	 * @return
	 */
	public File getFile()
	{
		String fileLocation = getLocation();
		try
		{
			// URLs with authority component (e.g. localhost) can't be
			// parsed.
			// NOK: file://localhost/Users
			// OK: file:///Users/
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
				return null;
			}

			Path path = Paths.get(url.toURI());
			File trackFile = path.toFile();
			return trackFile;
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
		catch (URISyntaxException e)
		{
			return null;
		}
		catch (MalformedURLException e)
		{
			return null;
		}
	}

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ComparableTrack [");
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

	public boolean isDisabled()
	{
		String additionalInfo = getAdditionalInfo("Disabled");
		return "true".equals(additionalInfo);
	}

	/**
	 * @return Returns Album Artist if available. Returns artist if album artist
	 *         isn't available.
	 */
	public String getArtistPreferred()
	{
		String artist = getAlbumArtist();
		if(Objects.isNull(artist))
		{
			return getArtist();
		}
		
		return artist;
	}
}