package de.christianleberfinger.melodies2go;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

class TestTrackCompilation {

	@Test
	void testLimitTrackNumber() {
		List<?> unfiltered = Lists.newArrayList(1, 2, 3);
		assertEquals(0, Melodies2Go.limitTrackNumber(unfiltered, 0).size());
		assertEquals(1, Melodies2Go.limitTrackNumber(unfiltered, 1).size());
		assertEquals(2, Melodies2Go.limitTrackNumber(unfiltered, 2).size());
		assertEquals(3, Melodies2Go.limitTrackNumber(unfiltered, 10).size());
	}

}
