package com.ipci.ngs.datacleaner.commonlib.reads.Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFile;

public class NameOfFileTest {

	@Test
	public void testValue() {
		final String expected = "test.txt";
		final String actual = new NameOfFile("test.txt").value();
		assertEquals(expected, actual);
	}

}
