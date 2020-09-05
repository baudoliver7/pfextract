package com.ipci.ngs.datacleaner.commonlib.reads.Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFile;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFileWithoutExtension;

public class NameOfFileWithoutExtensionTest {

	@Test
	public void testValue() {
		final String expected = "test";
		final String actual1 = new NameOfFileWithoutExtension(new NameOfFile("test.txt")).value();
		assertEquals(expected, actual1);
		
		final String actual2 = new NameOfFileWithoutExtension(new NameOfFile("test.txt.gz")).value();
		assertEquals(expected, actual2);
		
		final String actual3 = new NameOfFileWithoutExtension(new NameOfFile("test")).value();
		assertEquals(expected, actual3);
	}

}
