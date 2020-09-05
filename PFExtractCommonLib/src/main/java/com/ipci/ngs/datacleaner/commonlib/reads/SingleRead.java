package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class SingleRead implements ReadEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final File file;
	
	public SingleRead(final File file) {
		this.file = file;
	}
	
	public String name() {
		return file.getName();
	}

	public ReadEntryType type() {
		return ReadEntryType.SE;
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
		return file;
	}

	@Override
	public File second() {
		throw new UnsupportedOperationException("SingleRead : second file doesn't exist !");
	}
}
