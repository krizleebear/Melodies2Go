package de.christianleberfinger.melodies2go;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import de.christianleberfinger.melodies2go.parser.ITrack;
import de.christianleberfinger.melodies2go.parser.ITunesXMLParser;
import de.christianleberfinger.melodies2go.parser.Tracks;

public class TestITunesXMLParser
{
	private ITrack parseTrack(String xml) {
		Tracks tracks;
		try {
			tracks = ITunesXMLParser.parseLibrary(xml);
		} catch (SAXException | IOException e) {
			throw new RuntimeException(e);
		}
		assertEquals(1, tracks.size());
		return tracks.get(0);
	}
	
	@Test
	public void testNoPlayCount()
	{
		String xml = "<dict>\n" + 
				"	<key>Tracks</key>\n" + 
				"	<dict>\n" + 
				"		<key>4445</key>\n" + 
				"		<dict>\n" + 
				"			<key>Location</key><string>file:///Users/me/bla.mp3</string>\n" + 
				"		</dict>\n" + 
				"	</dict>\n" + 
				"</dict>";
		
		ITrack track = parseTrack(xml);
		assertEquals(0, track.getPlayCount());
	}

	@Test
	public void testRating()
	{
		String xml = "<dict>\n" + 
				"	<key>Tracks</key>\n" + 
				"	<dict>\n" + 
				"		<key>4445</key>\n" + 
				"		<dict>\n" + 
				"			<key>Rating</key><integer>60</integer>" + 
				"			<key>Location</key><string>file:///Users/me/bla.mp3</string>\n" + 
				"		</dict>\n" + 
				"	</dict>\n" + 
				"</dict>";
		
		ITrack track = parseTrack(xml);
		assertEquals(60, track.getRating());
		assertEquals(0, track.getPlayCount());
	}
	
	@Test
	public void testYear()
	{
		String xml = "<dict>\n" + 
				"	<key>Tracks</key>\n" + 
				"	<dict>\n" + 
				"		<key>4445</key>\n" + 
				"		<dict>\n" + 
				"			<key>Year</key><integer>2002</integer>" + 
				"			<key>Location</key><string>file:///Users/me/bla.mp3</string>\n" + 
				"		</dict>\n" + 
				"	</dict>\n" + 
				"</dict>";
		
		ITrack track = parseTrack(xml);
		assertEquals("2002", track.getYear());
	}
	
	@Test
	public void testPersistentID()
	{
		String xml = "<dict>\n" + 
				"	<key>Tracks</key>\n" + 
				"	<dict>\n" + 
				"		<key>4445</key>\n" + 
				"		<dict>\n" + 
				"			<key>Persistent ID</key><string>12345678DE9AE008</string>" + 
				"			<key>Location</key><string>file:///Users/me/bla.mp3</string>\n" + 
				"		</dict>\n" + 
				"	</dict>\n" + 
				"</dict>";
		
		ITrack track = parseTrack(xml);
//		assertEquals("12345678DE9AE008", track.getPersistentID());
	}

}
