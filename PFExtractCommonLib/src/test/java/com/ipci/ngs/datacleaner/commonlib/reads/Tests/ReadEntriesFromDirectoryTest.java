package com.ipci.ngs.datacleaner.commonlib.reads.Tests;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntriesFromDirectory;
import com.ipci.ngs.datacleaner.commonlib.reads.ReadEntry;

public class ReadEntriesFromDirectoryTest {

	@Test
	public void testItems() throws IOException {
		final String expected = "ind1.fastq";
		final ReadEntry entry = new ReadEntriesFromDirectory(Arrays.asList(new File("c:/ind1_1.fastq"), new File("c:/ind1_2.fastq"))).items().get(0);
		assertEquals(expected, entry.name()); 
		assertEquals("PE", entry.type().name());
	}

}
