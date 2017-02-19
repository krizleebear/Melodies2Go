package de.christianleberfinger.melodies2go.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ThreadLocalDateParser
{
	public ThreadLocalDateParser(String format)
	{
		parserThreadLocal = new ThreadLocal<SimpleDateFormat>() {
			protected SimpleDateFormat initialValue()
			{
				return new SimpleDateFormat(format);
			}
		};
	}

	private ThreadLocal<SimpleDateFormat> parserThreadLocal;

	public Date parse(String text) throws ParseException
	{
		return parserThreadLocal.get().parse(text);
	}
}