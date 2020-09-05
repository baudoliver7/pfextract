package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class SamRead implements ReadEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final File file;
	
	public SamRead(final File file) {
		this.file = file;
	}
	
	public String name() {
		return file.getName();
	}

	public ReadEntryType type() {
		return ReadEntryType.SAM;
	}

	public List<File> files() {
		return Arrays.asList(file);
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
		throw new UnsupportedOperationException("SingleRead : second file doesn't exist !");
	}
}
