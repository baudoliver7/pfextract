package com.ipci.ngs.datacleaner.commonlib.reads.Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ipci.ngs.datacleaner.commonlib.reads.ExtensionOfFile;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFile;

public class ExtensionOfFileTest {

	@Test
	public void testValue() {
		
		final String expected1 = "txt";
		final String actual1 = new ExtensionOfFile(new NameOfFile("test.txt")).value();
		assertEquals(expected1, actual1);
		
		final String expected2 = "txt.gz";
		final String actual2 = new ExtensionOfFile(new NameOfFile("test.txt.gz")).value();
		assertEquals(expected2, actual2);
	}

}
