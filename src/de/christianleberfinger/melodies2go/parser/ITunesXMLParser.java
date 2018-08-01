package de.christianleberfinger.melodies2go.parser;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.christianleberfinger.melodies2go.parser.Track.TrackBuilder;

public class ITunesXMLParser extends DefaultHandler
{
	private int dictLevel = 0;
	private boolean inTracks = false;
	private String currentKey = "";
	private StringBuilder buffer = new StringBuilder();

	// example: 2010-04-15T21:22:32Z
	private SimpleDateFormat dateParser = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss'Z'");

	private Track.TrackBuilder trackBuilder = new TrackBuilder();

	private final ITrackListener trackListener;

	public ITunesXMLParser(ITrackListener trackListener)
	{
		this.trackListener = trackListener;
	}

	@Override
	public void startElement(String namespaceURI,
			String localName,
			String qName,
			Attributes atts)
			throws SAXException
	{
		switch (qName)
		{
		case "string":
			break;
		case "integer":
			break;
		case "date":
			break;
		case "key":
			startKey(atts);
			break;
		case "dict":
			dictLevel++;
			break;
		}
	}

	private void startKey(Attributes atts)
	{
		clearBuffer();
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException
	{
		if (inTracks && dictLevel == 3 && "dict".equals(qName))
		{
			endTrack();
		}
		else if (inTracks && dictLevel == 2 && "dict".equals(qName))
		{
			inTracks = false;
		}

		switch (qName)
		{
		case "key":
			currentKey = getBuffer();

			if (dictLevel == 1 && "Tracks".equals(currentKey))
			{
				inTracks = true;
			}
			break;
		case "string":
			handleString(getBuffer());
			break;
		case "integer":
			handleInteger(getBuffer());
			break;
		case "date":
			handleDate(getBuffer());
			break;
		case "true":
			handleBoolean(true);
			break;
		case "false":
			handleBoolean(false);
			break;
		case "dict":
			dictLevel--;
			break;
		}

		clearBuffer();
	}

	public String getBuffer()
	{
		String s = buffer.toString();
		clearBuffer();
		return s;
	}

	private void handleBoolean(boolean value)
	{
		switch (currentKey)
		{
		case "Disabled":
			trackBuilder.isDisabled(value);
		case "Has Video":
			trackBuilder.hasVideo(value);
		}
	}

	private void handleInteger(String numericString)
	{
		long value = Long.parseLong(numericString);

		switch (currentKey)
		{
		case "Track Number":
			trackBuilder.trackNumber((int) value);
			break;
		case "Rating":
			trackBuilder.trackRating((int) value);
		case "Play Count":
			trackBuilder.playCount((int) value);
		}
	}

	private void handleString(String value)
	{
		switch (currentKey)
		{
		case "Artist":
			trackBuilder.artist(value);
			break;
		case "Album":
			trackBuilder.album(value);
			break;
		case "Album Artist":
			trackBuilder.albumArtist(value);
			break;
		case "Year":
			trackBuilder.year(value);
			break;
		case "Genre":
			trackBuilder.genre(value);
			break;
		case "Name":
			trackBuilder.title(value);
			break;
		case "Location":
			trackBuilder.fileLocation(value);
			break;
		}
	}

	private void handleDate(String dateString)
	{
		try
		{
			Date date = dateParser.parse(dateString);

			switch (currentKey)
			{
			case "Date Added":
				trackBuilder.dateAdded(date);
				break;
			}
		}
		catch (ParseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		buffer.append(ch, start, length);
	}

	private void clearBuffer()
	{
		buffer.setLength(0);
	}

	private void endTrack()
	{
		Track track = trackBuilder.build();
		if (track.getFile() != null)
		{
			trackListener.nextTrack(track);
		}

		trackBuilder = new TrackBuilder();
	}

	public static Tracks parseLibrary(File f)
			throws SAXException, IOException
	{
		Tracks tracks = new Tracks();
		DefaultHandler handler = new ITunesXMLParser(tracks);
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser;
		try
		{
			saxParser = factory.newSAXParser();
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}
		
		saxParser.parse(f, handler);

		return tracks;
	}
}
