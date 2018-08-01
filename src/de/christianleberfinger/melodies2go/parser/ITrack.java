package de.christianleberfinger.melodies2go.parser;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public interface ITrack
{
	String getArtist();

	String getAlbum();

	String getAlbumArtist();

	String getYear();

	String getGenre();

	/**
	 * Get track rating in percent (100% = best). <br/>
	 * Special treatment for computed ratings: <br/>
	 * Computed rankings are ratings that were not specifically assigned by the
	 * user to this track, but calculated from the album rating.
	 * 
	 * The algorithm credits this fact by reducing the rating.
	 * 
	 * @see #isTrackRatingComputed()
	 */
	public default int getRating()
	{
		if (isTrackRatingComputed())
		{
			return getTrackRating() / 5;
		}

		return getTrackRating();
	}

	int getTrackRating();

	boolean isTrackRatingComputed();

	int getPlayCount();

	/**
	 * Get date added (or Date(0) if not available)
	 * 
	 * @return
	 */
	Date getDateAdded();

	String getTitle();

	int getTrackNumber();

	/**
	 * Return the track's file location. If the track's location isn't valid
	 * (e.g. an URL), NULL will be returned.
	 * 
	 * @return
	 */
	File getFile();

	boolean isDisabled();

	/**
	 * @return Returns Album Artist if available. Returns artist if album artist
	 *         isn't available.
	 */
	public default String getArtistPreferred()
	{
		String artist = getAlbumArtist();
		if (Objects.isNull(artist))
		{
			return getArtist();
		}

		return artist;
	}

	boolean hasVideo();
}