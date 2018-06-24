package de.christianleberfinger.melodies2go;

import static org.junit.Assert.*;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import org.junit.Before;
import org.junit.Test;

public class TestStringNormalizer
{

	@Before
	public void setUp() throws Exception
	{
	}

	@Test
	public void test()
	{
		String normalized = Normalizer.normalize("Étienne De Crécy", Form.NFKD);
		assertEquals("E", normalized.substring(0, 1));
	}

}
