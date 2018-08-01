package de.christianleberfinger.melodies2go.parser;

import java.util.ArrayList;

public class Tracks extends ArrayList<ITrack> implements ITrackListener
{
	private static final long serialVersionUID = 6635745058176901826L;

	@Override
	public void nextTrack(ITrack track)
	{
		add(track);
	}
}
