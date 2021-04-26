package de.christianleberfinger.melodies2go;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import static org.junit.jupiter.api.Assertions.*;

public class TestStringNormalizer
{

	@BeforeEach
	public void setUp()
	{
	}

	@Test
	public void test()
	{
		String normalized = Normalizer.normalize("Étienne De Crécy", Form.NFKD);
		assertEquals("E", normalized.substring(0, 1));
		
		String decomposed = "Prügelknabe";
		assertNotEquals("Prügelknabe".length(), decomposed.length());
		
		assertFalse(decomposed.contentEquals("Prügelknabe"));
		
		assertNotEquals("Prügelknabe", decomposed);
		
		assertEquals("\u00FC", "\u0075\u0308");
	}
	
	

}
