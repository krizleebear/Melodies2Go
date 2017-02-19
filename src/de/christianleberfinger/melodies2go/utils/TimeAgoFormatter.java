package de.christianleberfinger.melodies2go.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeAgoFormatter
{
	public static String formatToDaysAgo(Date d)
	{
		Date now = new Date();
		return TimeUnit.MILLISECONDS.toDays(now.getTime() - d.getTime())
				+ " days ago";
	}
}
