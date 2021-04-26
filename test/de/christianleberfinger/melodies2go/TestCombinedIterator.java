package de.christianleberfinger.melodies2go;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import de.christianleberfinger.melodies2go.utils.CombinedIterator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCombinedIterator
{
	public static String collectElements(CombinedIterator<String> ci)
	{
		StringBuilder sb = new StringBuilder();
		while (ci.hasNext())
		{
			String c = ci.next();
			sb.append(c);
		}
		return sb.toString();
	}

	@Test
	public void testEqualListLengths()
	{
		ArrayList<String> list1 = Lists.newArrayList("A");
		ArrayList<String> list2 = Lists.newArrayList("B");
		ArrayList<String> list3 = Lists.newArrayList("C");
		CombinedIterator<String> ci = new CombinedIterator<>(list1, list2,
				list3);

		String elements = collectElements(ci);
		assertEquals("ABC", elements);
	}

	@Test
	public void testDifferntListLengths()
	{
		ArrayList<String> list1 = Lists.newArrayList("A", "C", "D");
		ArrayList<String> list2 = Lists.newArrayList("B");
		ArrayList<String> list3 = Lists.newArrayList("");
		CombinedIterator<String> ci = new CombinedIterator<>(list1, list2,
				list3);

		String elements = collectElements(ci);
		assertEquals("ABCD", elements);
	}

	@Test
	public void testDifferntListLengths2()
	{
		ArrayList<String> list1 = Lists.newArrayList("A", "D");
		ArrayList<String> list2 = Lists.newArrayList("B");
		ArrayList<String> list3 = Lists.newArrayList("C");
		CombinedIterator<String> ci = new CombinedIterator<>(list1, list2,
				list3);

		String elements = collectElements(ci);
		assertEquals("ABCD", elements);
	}
	
	@Test
	public void testDifferntListLengths3()
	{
		ArrayList<String> list1 = Lists.newArrayList();
		ArrayList<String> list2 = Lists.newArrayList("A", "C");
		ArrayList<String> list3 = Lists.newArrayList("B", "D");
		CombinedIterator<String> ci = new CombinedIterator<>(list1, list2,
				list3);

		String elements = collectElements(ci);
		assertEquals("ABCD", elements);
	}
}
