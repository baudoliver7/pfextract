package com.ipci.ngs.datacleaner.commonlib.reads;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class CachedReadEntry implements ReadEntry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String name;
	private final ReadEntryType type;
	private final File first;
	private final File second;
	
	public CachedReadEntry(final ReadEntry entry) {
		this.name = entry.name();
		this.type = entry.type();
		this.first = new CachedFile(entry.first());
		if(entry.type() == ReadEntryType.PE) {
			this.second = new CachedFile(entry.second());
		} else {
			this.second = null;
		}
		
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public ReadEntryType type() {
		return type;
	}

	@Override
	public List<File> files() {
		if(second == null) {
			return Arrays.asList(first);
		} else {
			return Arrays.asList(first, second);
		}		
	}

	@Override
	public File first() {
		return first;
	}

	@Override
	public File second() {
		if(second == null)
			new UnsupportedOperationException("CachedReadEntry#second");
		
		return second;
	}

}
