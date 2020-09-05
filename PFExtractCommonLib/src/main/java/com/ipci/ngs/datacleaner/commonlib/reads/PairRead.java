package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class PairRead implements ReadEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final File directFile;
	private final File reverseFile;
	private final String name;
	
	public PairRead(final String name, final File directFile, final File reverseFile) {
		this.directFile = directFile;
		this.reverseFile = reverseFile;
		this.name = name;
	}
	
	public PairRead(final File directFile, final File reverseFile) {
		this(
			new PairedReadName(
				new NameOfFile(directFile)
			).value(), 
			directFile,
			reverseFile
		);
	}
	
	public String name() {
		return name;
	}

	public ReadEntryType type() {
		return ReadEntryType.PE;
	}

	public List<File> files() {
		return Arrays.asList(directFile, reverseFile);
	}
	
	@Override
	public String toString() {
		return name();
	}

	@Override
	public File first() {
		return files().get(0);
	}

	@Override
	public File second() {
		return files().get(1);
	}
}
