package com.ipci.ngs.datacleaner.commonlib.reads.Tests;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ipci.ngs.datacleaner.commonlib.reads.Name;
import com.ipci.ngs.datacleaner.commonlib.reads.NameOfFile;
import com.ipci.ngs.datacleaner.commonlib.reads.PairedReadName;

public class PairedReadNameTest {

	@Test
	public void testValue() {
		
		final String expected = "test.txt";
		
		final String actual11 = new PairedReadName(nameOfFile("test_1.txt")).value();
		assertEquals(expected, actual11);
		
		final String actual12 = new PairedReadName(nameOfFile("test_2.txt")).value();
		assertEquals(expected, actual12);
		
		final String actual11bis = new PairedReadName(nameOfFile("test1.txt")).value();
		assertEquals(expected, actual11bis);
		
		final String actual12bis = new PairedReadName(nameOfFile("test2.txt")).value();
		assertEquals(expected, actual12bis);
		
		final String actual13 = new PairedReadName(nameOfFile("test_R1.txt")).value();
		assertEquals(expected, actual13);
		
		final String actual14 = new PairedReadName(nameOfFile("test_R2.txt")).value();
		assertEquals(expected, actual14);
		
		final String actual13bis = new PairedReadName(nameOfFile("testR1.txt")).value();
		assertEquals(expected, actual13bis);
		
		final String actual14bis = new PairedReadName(nameOfFile("testR2.txt")).value();
		assertEquals(expected, actual14bis);
	}
	
	private Name nameOfFile(String value) {
		return new NameOfFile(value);
	}
	

}
